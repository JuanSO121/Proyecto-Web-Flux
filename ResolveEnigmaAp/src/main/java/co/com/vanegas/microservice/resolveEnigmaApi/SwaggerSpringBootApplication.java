package co.com.vanegas.microservice.resolveEnigmaApi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

//import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@ComponentScan(basePackages = { "co.com.vanegas.microservice.resolveEnigmaApi", "co.com.vanegas.microservice.resolveEnigmaApi.api" , "co.com.solis.microservice.resolveEnigmaApi.config"})
public class SwaggerSpringBootApplication  {

    

    public static void main(String[] args) throws Exception {
        new SpringApplication(SwaggerSpringBootApplication.class).run(args);
    }

    
}
