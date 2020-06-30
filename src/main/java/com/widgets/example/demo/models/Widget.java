package com.widgets.example.demo.models;

import java.util.UUID;

public class Widget {

    private final UUID id;

    public Widget(ReadonlyWidget widget)
    {
        this.x = widget.x;
        this.y = widget.y;
        this.zLevel = widget.zLevel;
        this.width = widget.width;
        this.height = widget.height;
        this.id = widget.id;
    }

    public Widget()
    {
        id = null;
    }

    public long x;
    public long y;
    public long zLevel;
    public long width;
    public long height;
    public UUID getId(){
        return id;
    }

    public Widget(long x, long y, long width, long height, long zLevel)
    {
        this();
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.zLevel = zLevel;
    }

}
