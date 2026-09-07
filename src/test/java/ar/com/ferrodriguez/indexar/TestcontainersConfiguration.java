package ar.com.ferrodriguez.indexar;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

	@Bean
	@ServiceConnection
	PostgreSQLContainer postgresContainer() {
		// Version fijada a proposito (no "latest"): reproducibilidad entre corridas locales y CI.
		return new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"));
	}

}
