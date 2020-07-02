package com.widgets.example.demo.repositories;

import com.widgets.example.demo.models.ReadonlyWidget;
import com.widgets.example.demo.models.Widget;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class InMemoryWidgetRepository implements IWidgetRepository {

    private ConcurrentHashMap<UUID, ReadonlyWidget> widgets = new ConcurrentHashMap<>();
    private final ReentrantLock updateLock = new ReentrantLock();

    @Override
    public Enumeration<ReadonlyWidget> getWidgets() {
        return widgets.elements();
    }

    @Override
    public ReadonlyWidget getWidget(UUID widgetId) {
        return widgets.get(widgetId);
    }

    @Override
    public ArrayList<ReadonlyWidget> bulkUpdateWidgets(Collection<Widget> addedWidgets, Collection<Widget> updatedWidgets, Collection<UUID> removedWidgetIds) {
        var result = new ArrayList<ReadonlyWidget>();
        var newWidgets = new ConcurrentHashMap<UUID,ReadonlyWidget>();
        var updateFlag = true;
        while (updateFlag)
        {
            if (updateLock.tryLock());
                try
                {
                    var changedDate = Calendar.getInstance();
                    if (addedWidgets != null)
                        addedWidgets.forEach(widget -> {
                            var id = UUID.randomUUID();
                            var newWidget = new ReadonlyWidget(widget, changedDate, id);
                            result.add(newWidget);
                            newWidgets.put(id,newWidget);
                        });
                    if (updatedWidgets != null)
                        updatedWidgets.forEach(widget -> {
                            var id = widget.getId();
                            var newWidget = new ReadonlyWidget(widget, changedDate, id);
                            result.add(newWidget);
                            newWidgets.put(id,newWidget);
                        });
                    widgets.forEach((id,widget) -> {
                        var context = new Object() {
                            boolean canAdd = true;
                        };
                        if (removedWidgetIds != null)
                        {
                            context.canAdd = context.canAdd && !removedWidgetIds.contains(id);
                            result.add(widget);
                        }
                        if (updatedWidgets != null)
                            updatedWidgets.forEach(w ->
                            {
                                if (w.getId() == id)
                                    context.canAdd = false;
                            });
                        if (context.canAdd)
                            newWidgets.put(id,widget);
                    });
                    widgets = newWidgets;
                    updateFlag = false;
                }
                finally {
                    updateLock.unlock();
                }
        }
        return result;
    }
}
