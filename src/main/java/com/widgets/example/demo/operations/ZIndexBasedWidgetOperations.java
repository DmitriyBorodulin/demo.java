package com.widgets.example.demo.operations;


import com.widgets.example.demo.exceptions.InvalidFilterParamsException;
import com.widgets.example.demo.models.FilterParams;
import com.widgets.example.demo.models.ReadonlyWidget;
import com.widgets.example.demo.models.Widget;
import com.widgets.example.demo.repositories.IWidgetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ZIndexBasedWidgetOperations implements IZIndexBasedWidgetRepository {

    @Autowired
    private IWidgetRepository repository;

    @Override
    public ReadonlyWidget removeWidget(@NonNull UUID widgetId) {
        return repository.bulkUpdateWidgets(null, null, List.of(widgetId)).get(0);
    }

    private ReadonlyWidget addOrUpdateWidget(Widget widget, boolean isAdd)
    {
        var widgets = repository.getWidgets();
        var upwardWidgets = new HashMap<Long,Widget>();
        var context = new Object() {
            long maxZLevel = 0;
        };
        widgets.asIterator().forEachRemaining(w -> {
            if (widget.zLevel <= w.zLevel)
            {
                upwardWidgets.put(w.zLevel,new Widget(w));
                if (context.maxZLevel < w.zLevel)
                    context.maxZLevel = w.zLevel;
            }
        });
        var updatedWidgets = new ArrayList<Widget>();
        if (!isAdd)
            updatedWidgets.add(widget);
        for (long i = widget.zLevel;i<=context.maxZLevel;i++)
        {
            if (upwardWidgets.containsKey(i))
            {
                var w = upwardWidgets.get(i);
                w.zLevel++;
                updatedWidgets.add(w);
            }
            else
                break;
        }
        var addedWidgets = isAdd ? List.of(widget) : null;
        var updateResult = repository.bulkUpdateWidgets(addedWidgets, updatedWidgets,null);
        return updateResult.get(0);
    }

    @Override
    public ReadonlyWidget addWidget(@NonNull Widget widget) {
        return addOrUpdateWidget(widget,true);
    }

    @Override
    public ReadonlyWidget updateWidget(@NonNull Widget widget) {
        return addOrUpdateWidget(widget,false);
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

    private boolean IsFilterParamsValid(FilterParams params)
    {
        return params == null || (params.height > 0 && params.width > 0);
    }

    @Override
    public List<ReadonlyWidget> getWidgets(FilterParams filterParams) throws InvalidFilterParamsException {
        if (!IsFilterParamsValid(filterParams))
            throw new InvalidFilterParamsException("Invalid filter params");
        var result = new ArrayList<ReadonlyWidget>();
        repository.getWidgets().asIterator().forEachRemaining(widget -> {
            if (isFiltered(widget,filterParams))
                result.add(widget);
        });
        Collections.sort(result,(w1,w2) -> w1.zLevel >= w2.zLevel ? 1 : -1);
        return result;
    }

    @Override
    public ReadonlyWidget getWidget(UUID id) {
        return repository.getWidget(id);
    }
}
