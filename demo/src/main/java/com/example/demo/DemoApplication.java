package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication
@ComponentScan(basePackages = "com.example", excludeFilters = {
		@ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.example\\.config\\..*"),
		@ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.example\\.dao\\..*")
})
public class DemoApplication {
	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}
}
