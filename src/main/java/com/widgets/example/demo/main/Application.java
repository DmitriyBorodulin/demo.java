package com.widgets.example.demo.main;

import com.widgets.example.demo.controllers.WidgetsController;
import com.widgets.example.demo.repositories.IZIndexBasedWidgetRepository;
import com.widgets.example.demo.repositories.ZIndexBaseH2WidgetRepository;
import com.widgets.example.demo.repositories.ZIndexBasedInMemoryWidgetRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import java.sql.SQLException;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public IZIndexBasedWidgetRepository getOperations(Environment environment) throws SQLException {
		if (environment.getProperty("app.useH2","").equals("true")) {
			return new ZIndexBaseH2WidgetRepository(environment);
		}
		return new ZIndexBasedInMemoryWidgetRepository();
	}

	@Bean
	public WidgetsController getWidgetsController()	{
		return new WidgetsController();
	}

}
