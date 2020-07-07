package com.widgets.example.demo.repositories;

import com.widgets.example.demo.models.ReadonlyWidget;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

class InnerStore {
    ConcurrentHashMap<UUID, ReadonlyWidget> widgetsById ;
    ConcurrentSkipListMap<Long,ReadonlyWidget> widgetsByZIndex;

    InnerStore(ConcurrentHashMap<UUID, ReadonlyWidget> widgetsById, ConcurrentSkipListMap<Long,ReadonlyWidget> widgetsByZIndex)
    {
        this.widgetsById = widgetsById;
        this.widgetsByZIndex = widgetsByZIndex;
    }
}
