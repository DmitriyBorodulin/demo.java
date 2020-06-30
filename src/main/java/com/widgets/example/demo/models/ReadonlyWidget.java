package com.widgets.example.demo.models;

import java.util.Calendar;
import java.util.UUID;

public class ReadonlyWidget {
    public final long x;
    public final long y;
    public final long zLevel;
    public final long width;
    public final long height;
    public final Calendar changeDate;
    public final UUID id;

    public ReadonlyWidget(Widget widget, Calendar changeDate, UUID id)
    {
        this.x = widget.x;
        this.y = widget.y;
        this.zLevel = widget.zLevel;
        this.width = widget.width;
        this.height = widget.height;
        this.changeDate = changeDate;
        this.id = id;
    }
}
