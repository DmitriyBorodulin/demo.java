package com.widgets.example.demo.main;

import com.widgets.example.demo.controllers.WidgetsController;
import com.widgets.example.demo.operations.IWidgetOperations;
import com.widgets.example.demo.operations.ZIndexBasedWidgetOperations;
import com.widgets.example.demo.repositories.IWidgetRepository;
import com.widgets.example.demo.repositories.InMemoryWidgetRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public IWidgetRepository getRepository()
	{
		return new InMemoryWidgetRepository();
	}

	@Bean
	public IWidgetOperations getOperations()
	{
		return new ZIndexBasedWidgetOperations();
	}

	@Bean
	public WidgetsController getWidgetsController()
	{
		return new WidgetsController();
	}


}
