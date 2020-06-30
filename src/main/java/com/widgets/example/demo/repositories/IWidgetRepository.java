package com.widgets.example.demo.repositories;

import com.widgets.example.demo.models.ReadonlyWidget;
import com.widgets.example.demo.models.Widget;

import java.util.*;

public interface IWidgetRepository {
    Enumeration<ReadonlyWidget> getWidgets();
    ReadonlyWidget getWidget(UUID widgetId);
    ArrayList<ReadonlyWidget> bulkUpdateWidgets(Collection<Widget> addedWidgets, Collection<Widget> updatedWidgets, Collection<UUID> removedWidgetIds);
}

