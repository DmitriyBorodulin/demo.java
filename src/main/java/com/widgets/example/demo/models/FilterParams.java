package com.widgets.example.demo.models;

public class FilterParams {
    public long x = 0;
    public long y = 0;
    public long height = 0;
    public long width = 0;

    public FilterParams(ReadonlyWidget widget)
    {
        this.x = widget.x;
        this.y = widget.y;
        this.width = widget.width;
        this.height = widget.height;
    }

    public FilterParams()     {}

    public FilterParams(Widget widget)
    {
        this.x = widget.x;
        this.y = widget.y;
        this.width = widget.width;
        this.height = widget.height;
    }
}
