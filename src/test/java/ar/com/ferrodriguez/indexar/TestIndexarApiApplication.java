package ar.com.ferrodriguez.indexar;

import org.springframework.boot.SpringApplication;

public class TestIndexarApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(IndexarApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
