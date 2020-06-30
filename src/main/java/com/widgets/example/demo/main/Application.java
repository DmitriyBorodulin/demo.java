package com.widgets.example.demo.main;

import com.widgets.example.demo.controllers.WidgetsController;
import com.widgets.example.demo.operations.IWidgetOperations;
import com.widgets.example.demo.operations.ZIndexBasedWidgetOperations;
import com.widgets.example.demo.repositories.IWidgetRepository;
import com.widgets.example.demo.repositories.InMemoryWidgetRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	private IWidgetRepository repository;
	@Bean
	public IWidgetRepository GetRepository()
	{
		if (repository == null)
			repository = new InMemoryWidgetRepository();
		return repository;
	}

	private IWidgetOperations operations;
	@Bean
	public IWidgetOperations GetOperations()
	{
		if (operations == null)
			operations = new ZIndexBasedWidgetOperations();
		return operations;
	}

	private WidgetsController widgetsController;
	@Bean
	public WidgetsController GetWidgetsController()
	{
		if (widgetsController == null)
			widgetsController = new WidgetsController();
		return widgetsController;
	}


}
