package com.widgets.example.demo.main;

import com.widgets.example.demo.controllers.WidgetsController;
import com.widgets.example.demo.operations.IZIndexBasedWidgetRepository;
import com.widgets.example.demo.repositories.ZIndexBasedWidgetRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public IZIndexBasedWidgetRepository getOperations()	{
		return new ZIndexBasedWidgetRepository();
	}

	@Bean
	public WidgetsController getWidgetsController()	{
		return new WidgetsController();
	}


}
