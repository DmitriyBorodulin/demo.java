package com.widgets.example.demo.main;

import com.widgets.example.demo.controllers.WidgetsController;
import com.widgets.example.demo.operations.IZIndexBasedWidgetRepository;
import com.widgets.example.demo.repositories.IWidgetRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SmokeTests {

	@Autowired
	WidgetsController widgetsController;

	@Autowired
	IWidgetRepository repository;

	@Autowired
    IZIndexBasedWidgetRepository operations;

	@Test
	void contextLoads() throws Exception {
		assertThat(widgetsController).isNotNull().as("WidgetsController is null");
		assertThat(repository).isNotNull().as("WidgetsRepository is null");
		assertThat(operations).isNotNull().as("WidgetsOperations is null");
	}

}
