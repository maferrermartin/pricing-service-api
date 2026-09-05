package com.github.maferrermartin;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/**
 * Verifica que la estructura de módulos (Spring Modulith) respeta los límites
 * declarados: el módulo {@code pricing} debe permanecer aislado, sin ciclos ni
 * fugas de sus paquetes internos (domain/application/infrastructure).
 */
class ModularityTests {

	private static final ApplicationModules MODULES = ApplicationModules.of(MaferrermartinApplication.class);

	@Test
	void verifiesModularStructure() {
		MODULES.verify();
	}

}
