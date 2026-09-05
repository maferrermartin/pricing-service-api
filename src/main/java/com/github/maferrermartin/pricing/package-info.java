/**
 * Módulo {@code pricing}: núcleo de negocio, aislado e independiente del resto de la
 * aplicación, organizado con arquitectura hexagonal (puertos y adaptadores) y visión DDD.
 * <p>
 * Es el único módulo de aplicación en este Spring Modulith monolito; sus límites se
 * verifican automáticamente en tiempo de test (ver {@code ApplicationModules.verify()}).
 * <p>
 * Estructura interna:
 * <ul>
 *   <li>{@code domain} — modelo de dominio: entidades, value objects y reglas de negocio,
 *       sin dependencias de frameworks.</li>
 *   <li>{@code application} — casos de uso y puertos (entrada/salida) que definen el
 *       contrato entre el dominio y el mundo exterior.</li>
 *   <li>{@code infrastructure} — adaptadores: entrada web (REST) y salida de persistencia
 *       (JPA/H2), implementan los puertos de {@code application}.</li>
 * </ul>
 */
package com.github.maferrermartin.pricing;
