package trongnv.restful_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// @SpringBootAppliation annotation add:
// @configuration: Set the class a a source of bean definitions for the application context
// @EnableAutoConfiguration: Tells Spring Boot to start adding beans based on classpath settings
// @ComponentScan: Tells Spring to look for other components, configurations, and services in the com/example package, letting it find the controllers.
@SpringBootApplication
public class RestfulServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(RestfulServiceApplication.class, args);
	}

}
