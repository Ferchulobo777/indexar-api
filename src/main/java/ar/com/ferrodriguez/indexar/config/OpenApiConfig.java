package ar.com.ferrodriguez.indexar.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI indexarOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Indexar API")
                        .description("Indicadores económicos de Argentina en tiempo real — cotizaciones del dólar "
                                + "(oficial, blue, MEP, CCL, cripto) ingeridas desde dolarapi.com cada 15 minutos, "
                                + "con historial consultable. Sin autenticación, de uso público.")
                        .version("v1")
                        .contact(new Contact()
                                .name("Fernando Rodríguez")
                                .url("https://portfolio-2026-fernando.vercel.app")
                                .email("ferchulobo2015@gmail.com"))
                        .license(new License().name("MIT")));
    }
}
