package com.github.maferrermartin.pricing;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import com.github.maferrermartin.pricing.application.port.in.FindApplicablePriceQuery;

@Tag("performance")
@Testcontainers
@SpringBootTest

@TestPropertySource(properties = "spring.sql.init.mode=never")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PricingModulePerformanceIT {

	private static final int BRAND_COUNT = 20;
	private static final int PRODUCT_COUNT = 500;
	private static final int MIN_ROWS_PER_COMBINATION = 5;
	private static final int MAX_EXTRA_ROWS_PER_COMBINATION = 30;
	private static final int HOT_COMBINATION_ROW_COUNT = 50;
	private static final int BATCH_SIZE = 1_000;
	private static final long PERFORMANCE_THRESHOLD_MILLIS = 200;
	private static final long RANDOM_SEED = 42L;

	private static final LocalDateTime WINDOW_START = LocalDateTime.of(2022, 1, 1, 0, 0);
	private static final LocalDateTime WINDOW_END = LocalDateTime.of(2025, 1, 1, 0, 0);

	@Container
	@ServiceConnection
	@SuppressWarnings("unused")
	static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

	@Autowired
	private FindApplicablePriceQuery findApplicablePriceQuery;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private long totalRowsSeeded;
	private Long hotBrandId;
	private Long hotProductId;
	private LocalDateTime hotApplicationDate;
	private BigDecimal hotExpectedPrice;

	@BeforeAll
	void seedDatabaseWithARandomizedLargeDataset() {
		var random = new Random(RANDOM_SEED);
		long windowDays = ChronoUnit.DAYS.between(WINDOW_START, WINDOW_END);
		var batch = new ArrayList<Object[]>(BATCH_SIZE);

		for (long brandId = 1; brandId <= BRAND_COUNT; brandId++) {
			for (long productId = 1; productId <= PRODUCT_COUNT; productId++) {
				List<LocalDateTime> anchorDays = List.of(
						randomDayWithin(random, windowDays),
						randomDayWithin(random, windowDays));

				int rowCount = MIN_ROWS_PER_COMBINATION + random.nextInt(MAX_EXTRA_ROWS_PER_COMBINATION);
				for (int i = 0; i < rowCount; i++) {
					LocalDateTime start = random.nextBoolean()
							? anchorDays.get(random.nextInt(anchorDays.size())).plusHours(random.nextInt(24))
							: randomDayWithin(random, windowDays).plusHours(random.nextInt(24));
					LocalDateTime end = start.plusHours(1 + random.nextInt(24 * 60));
					long priceList = 1 + random.nextInt(4);
					int priority = random.nextInt(6);
					BigDecimal price = BigDecimal.valueOf(1000 + random.nextInt(9000), 2);

					batch.add(row(brandId, start, end, priceList, productId, priority, price));
					totalRowsSeeded++;
					if (batch.size() == BATCH_SIZE) {
						insertBatch(batch);
					}
				}
			}
		}
		insertBatch(batch);

		seedHotOverlappingCombination(random, windowDays);
	}

	private void seedHotOverlappingCombination(Random random, long windowDays) {
		hotBrandId = 1L;
		hotProductId = 1L;
		hotApplicationDate = randomDayWithin(random, windowDays).withHour(12).withMinute(0);

		var hotBatch = new ArrayList<Object[]>(HOT_COMBINATION_ROW_COUNT);
		for (int priority = 0; priority < HOT_COMBINATION_ROW_COUNT; priority++) {
			BigDecimal price = BigDecimal.valueOf(1000 + priority, 2);
			hotBatch.add(row(hotBrandId, hotApplicationDate.minusHours(2), hotApplicationDate.plusHours(2),
					1 + (priority % 4), hotProductId, priority, price));
			if (priority == HOT_COMBINATION_ROW_COUNT - 1) {
				hotExpectedPrice = price;
			}
		}
		insertBatch(hotBatch);
		totalRowsSeeded += HOT_COMBINATION_ROW_COUNT;
	}

	private LocalDateTime randomDayWithin(Random random, long windowDays) {
		return WINDOW_START.plusDays(random.nextLong(windowDays));
	}

	private Object[] row(long brandId, LocalDateTime start, LocalDateTime end, long priceList,
			long productId, int priority, BigDecimal price) {
		return new Object[] { brandId, start, end, priceList, productId, priority, price, "EUR" };
	}

	private void insertBatch(List<Object[]> batch) {
		if (batch.isEmpty()) {
			return;
		}
		jdbcTemplate.batchUpdate("""
				INSERT INTO PRICES (BRAND_ID, START_DATE, END_DATE, PRICE_LIST, PRODUCT_ID, PRIORITY, PRICE, CURR)
				VALUES (?, ?, ?, ?, ?, ?, ?, ?)
				""", batch);
		batch.clear();
	}

	@Test
	void resolvesTheHighestPriorityRateQuicklyAmongFiftyOverlappingCandidates() {
		findApplicablePriceQuery.find(hotApplicationDate, hotBrandId, hotProductId);

		long start = System.nanoTime();
		var result = findApplicablePriceQuery.find(hotApplicationDate, hotBrandId, hotProductId);
		long elapsedMillis = (System.nanoTime() - start) / 1_000_000;

		assertThat(result).isPresent();
		assertThat(result.get().price()).isEqualByComparingTo(hotExpectedPrice);
		assertThat(elapsedMillis)
				.as("tiempo de respuesta contra %d filas (combinacion con %d candidatos solapados)",
						totalRowsSeeded, HOT_COMBINATION_ROW_COUNT)
				.isLessThan(PERFORMANCE_THRESHOLD_MILLIS);
	}

	@Test
	void resolvesSeveralRandomLookupsConsistentlyWithinTheThreshold() {
		findApplicablePriceQuery.find(WINDOW_START, 1L, 1L); // warm-up

		var random = new Random(RANDOM_SEED + 1);
		for (int i = 0; i < 20; i++) {
			long brandId = 1 + random.nextInt(BRAND_COUNT);
			long productId = 1 + random.nextInt(PRODUCT_COUNT);
			LocalDateTime date = randomDayWithin(random, ChronoUnit.DAYS.between(WINDOW_START, WINDOW_END));

			long start = System.nanoTime();
			findApplicablePriceQuery.find(date, brandId, productId);
			long elapsedMillis = (System.nanoTime() - start) / 1_000_000;

			assertThat(elapsedMillis)
					.as("lookup #%d (brandId=%d, productId=%d) contra %d filas", i, brandId, productId, totalRowsSeeded)
					.isLessThan(PERFORMANCE_THRESHOLD_MILLIS);
		}
	}

}
