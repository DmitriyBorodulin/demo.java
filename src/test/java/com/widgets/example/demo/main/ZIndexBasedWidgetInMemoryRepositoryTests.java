package com.widgets.example.demo.main;

import com.widgets.example.demo.repositories.ZIndexBasedInMemoryWidgetRepository;

public class ZIndexBasedWidgetInMemoryRepositoryTests extends ZIndexBasedWidgetRepositoryTests {

    public ZIndexBasedWidgetInMemoryRepositoryTests() {
        repository = new ZIndexBasedInMemoryWidgetRepository();
        getWidgetsNTimes = 2000;
        addWidgetsNTimes = 2000;
    }

}
