package com.example.springOne.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class OpenApiConfig {


    @Bean
    public OpenAPI libraryApi(){
        return new OpenAPI()
                .info(new Info()
                        .title("Library API")
                        .description("API documentation for the Library application")
                        .version("1.0.0"));
    }
}