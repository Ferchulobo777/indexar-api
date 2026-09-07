package ar.com.ferrodriguez.indexar.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * {@code @EnableCaching}/{@code @EnableScheduling} viven acá, separados de
 * {@link ar.com.ferrodriguez.indexar.IndexarApiApplication}, a propósito:
 * {@code @WebMvcTest} usa la clase de aplicación principal como fuente de
 * configuración y respeta cualquier anotación declarada directamente sobre
 * ella (no solo las auto-configuraciones) — si {@code @EnableCaching}
 * estuviera ahí, cada test de slice web exigiría un {@code CacheManager}
 * aunque el controller bajo test no use caching. Separándolo en una
 * {@code @Configuration} aparte, los slice tests no lo ven.
 */
@Configuration
@EnableCaching
@EnableScheduling
public class AsyncConfig {
}
