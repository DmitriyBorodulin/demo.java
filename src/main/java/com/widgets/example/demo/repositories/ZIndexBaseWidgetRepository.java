package com.widgets.example.demo.repositories;

import com.widgets.example.demo.exceptions.InvalidFilterParamsException;
import com.widgets.example.demo.models.FilterParams;
import com.widgets.example.demo.models.ReadonlyWidget;

import java.sql.SQLException;
import java.util.List;

public abstract class ZIndexBaseWidgetRepository {

    private boolean IsFilterParamsValid(FilterParams params)    {
        return params == null || (params.height > 0 && params.width > 0);
    }

    public List<ReadonlyWidget> getWidgets(FilterParams filterParams) throws InvalidFilterParamsException, SQLException {
        if (!IsFilterParamsValid(filterParams))
            throw new InvalidFilterParamsException("Invalid filter params");
        return null;
    }
}
