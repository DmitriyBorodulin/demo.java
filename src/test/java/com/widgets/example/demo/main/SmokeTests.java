package com.widgets.example.demo.main;

import com.widgets.example.demo.controllers.WidgetsController;
import com.widgets.example.demo.repositories.IZIndexBasedWidgetRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SmokeTests {

	@Autowired
	WidgetsController widgetsController;

	@Autowired
    IZIndexBasedWidgetRepository operations;

	@Test
	void contextLoads() {
		assertThat(widgetsController).as("WidgetsController is null").isNotNull();
		assertThat(operations).as("WidgetsOperations is null").isNotNull();
	}

}
