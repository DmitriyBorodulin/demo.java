package com.widgets.example.demo.repositories;

import com.widgets.example.demo.exceptions.InvalidFilterParamsException;
import com.widgets.example.demo.models.FilterParams;
import com.widgets.example.demo.models.ReadonlyWidget;
import com.widgets.example.demo.models.Widget;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;


public interface IZIndexBasedWidgetRepository {
    ReadonlyWidget removeWidget(UUID widgetId) throws SQLException;
    ReadonlyWidget addWidget(Widget widget) throws SQLException;
    ReadonlyWidget updateWidget(Widget widget) throws SQLException;
    List<ReadonlyWidget> getWidgets(FilterParams filterParams ) throws InvalidFilterParamsException, SQLException;

    ReadonlyWidget getWidget(UUID id) throws SQLException;
}
