package com.widgets.example.demo.operations;

import com.widgets.example.demo.exceptions.InvalidFilterParamsException;
import com.widgets.example.demo.models.FilterParams;
import com.widgets.example.demo.models.ReadonlyWidget;
import com.widgets.example.demo.models.Widget;

import java.util.List;
import java.util.UUID;


public interface IWidgetOperations {
    void removeWidget(UUID widgetId);
    ReadonlyWidget addWidget(Widget widget);
    ReadonlyWidget updateWidget(Widget widget);
    List<ReadonlyWidget> getWidgets(FilterParams filterParams ) throws InvalidFilterParamsException;

    ReadonlyWidget getWidget(UUID id);
}
