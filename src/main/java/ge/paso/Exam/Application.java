package ge.paso.Exam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Documentation;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;

@SpringBootApplication
@EnableSwagger2
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public Docket swaggerConfiguration() {
		return new Docket(DocumentationType.SWAGGER_2)
				.select()
				.apis(RequestHandlerSelectors.basePackage("ge.paso"))
				.build()
				.apiInfo(apiDetails());
	}

	private ApiInfo apiDetails() {
		return new ApiInfo("Create/Modify Server API",
						   "Simple API for create or modify servers",
							"1.2",
							"Free to use",
							new springfox.documentation.service.Contact("David Pasikashvili","https://www.linkedin.com/in/davit-pasikashvili-1a1bb8237/", "dpasi17@freeuni.edu.ge"),
							"API License",
							"https://www.linkedin.com/in/davit-pasikashvili-1a1bb8237/",
				             Collections.emptyList());
	}

}
