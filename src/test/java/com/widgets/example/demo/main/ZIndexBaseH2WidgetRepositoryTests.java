package com.widgets.example.demo.main;

import com.widgets.example.demo.repositories.ZIndexBaseH2WidgetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import java.sql.SQLException;

@SpringBootTest
public class ZIndexBaseH2WidgetRepositoryTests extends ZIndexBasedWidgetRepositoryTests {

    @Autowired
    public ZIndexBaseH2WidgetRepositoryTests(Environment environment) throws SQLException {
        repository = new ZIndexBaseH2WidgetRepository(environment);
        getWidgetsNTimes = 100;
        addWidgetsNTimes = 100;
    }
}
