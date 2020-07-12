package com.widgets.example.demo.repositories;

import com.widgets.example.demo.exceptions.InvalidFilterParamsException;
import com.widgets.example.demo.models.FilterParams;
import com.widgets.example.demo.models.ReadonlyWidget;
import com.widgets.example.demo.models.Widget;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

@Component
public class ZIndexBasedInMemoryWidgetRepository extends ZIndexBaseWidgetRepository implements IZIndexBasedWidgetRepository {

    private volatile InnerStore store = new InnerStore(new ConcurrentHashMap<>(), new ConcurrentSkipListMap<>());

    private ConcurrentHashMap<UUID,ReadonlyWidget> newWidgetsById = null;
    private ConcurrentSkipListMap<Long,ReadonlyWidget> newWidgetsByZIndex = null;

    private final ReentrantLock updateLock = new ReentrantLock();

    private ReadonlyWidget execBlockingOperation(Function<Widget,ReadonlyWidget> func, Widget widget)    {
        ReadonlyWidget result;
        try
        {
            updateLock.lock();
            newWidgetsById = new ConcurrentHashMap<>(store.widgetsById);
            newWidgetsByZIndex = new ConcurrentSkipListMap<>(store.widgetsByZIndex);
            result = func.apply(widget);
            store = new InnerStore(newWidgetsById,newWidgetsByZIndex);
        }
        finally
        {
            updateLock.unlock();
        }
        return result;
    }

    @Override
    public ReadonlyWidget removeWidget(UUID widgetId) {
        var rWidget = newWidgetsById.get(widgetId);
        if (rWidget == null)
            return null;
        var removedWidget = new Widget(rWidget);
        return execBlockingOperation((w) ->
        {
            newWidgetsByZIndex.remove(w.zLevel);
            newWidgetsById.remove(w.getId());
            return rWidget;
        },removedWidget);
    }

    private ReadonlyWidget UpdateByNextZLevelWidget(ReadonlyWidget rWidget, Calendar changedDate )    {
        var tmpWidget = new Widget(rWidget);
        tmpWidget.zLevel++;
        var result = newWidgetsByZIndex.get(tmpWidget.zLevel);
        rWidget = new ReadonlyWidget(tmpWidget,changedDate,tmpWidget.getId());
        if (result != null)
            newWidgetsByZIndex.replace(rWidget.zLevel,rWidget);
        else
            newWidgetsByZIndex.put(rWidget.zLevel,rWidget);
        newWidgetsById.replace(rWidget.id,rWidget);
        return result;
    }

    private void moveZIndexTo1(ReadonlyWidget fromWidget,Calendar changedDate)    {
        var tmpRWidget = newWidgetsByZIndex.get(fromWidget.zLevel);
        if (tmpRWidget != null && tmpRWidget.id != fromWidget.id)
        {
            while (tmpRWidget != null)
                tmpRWidget = UpdateByNextZLevelWidget(tmpRWidget,changedDate);
        }
    }

    private ReadonlyWidget updateOrAddWidget(Widget widget, boolean isAdd)    {
        var changedDate = Calendar.getInstance();
        var result = new ReadonlyWidget(widget, changedDate,isAdd ? UUID.randomUUID() : widget.getId());
        moveZIndexTo1(result,changedDate);
        if (isAdd)
        {
            newWidgetsByZIndex.put(result.zLevel, result);
            newWidgetsById.put(result.id, result);
        }
        else {
            var oldWidgetZLevel = newWidgetsById.get(result.id).zLevel;
            if (oldWidgetZLevel != result.zLevel)
                newWidgetsByZIndex.remove(oldWidgetZLevel);
            if (newWidgetsByZIndex.containsKey(result.zLevel))
                newWidgetsByZIndex.replace(result.zLevel, result);
            else
                newWidgetsByZIndex.put(result.zLevel, result);
            newWidgetsById.replace(result.id, result);
        }
        return result;
    }

    @Override
    public ReadonlyWidget addWidget(Widget widget) {
        return execBlockingOperation((w) ->
                updateOrAddWidget(w,true),widget);
    }

    @Override
    public ReadonlyWidget updateWidget(Widget widget) {
        return execBlockingOperation((w) ->
                updateOrAddWidget(w,false),widget);
    }

    private boolean IsFilterParamsValid(FilterParams params)    {
        return params == null || (params.height > 0 && params.width > 0);
    }

    private boolean isFiltered(ReadonlyWidget el, FilterParams filterParams)
    {
        if (filterParams != null)
        {
            return el.x >= filterParams.x && el.y >= filterParams.y &&
                    el.x + el.width <= filterParams.x + filterParams.width &&
                    el.y + el.height <= filterParams.y + filterParams.height;
        }
        return true;
    }

    @Override
    public List<ReadonlyWidget> getWidgets(FilterParams filterParams) throws InvalidFilterParamsException, SQLException {
        super.getWidgets(filterParams);
        if (filterParams == null)
            return List.copyOf(store.widgetsByZIndex.values());
        var result = new ArrayList<ReadonlyWidget>();
        store.widgetsByZIndex.forEach((zLevel, widget) -> {
            if (isFiltered(widget,filterParams))
                result.add(widget);
        });
        return result;
    }

    @Override
    public ReadonlyWidget getWidget(UUID id) {
        return store.widgetsById.get(id);
    }
}
