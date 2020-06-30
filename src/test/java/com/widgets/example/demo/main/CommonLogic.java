package com.widgets.example.demo.main;

import com.widgets.example.demo.models.ReadonlyWidget;
import com.widgets.example.demo.models.Widget;

public class CommonLogic {
    public static boolean isEqualWidgets(Widget widget1, ReadonlyWidget widget2, boolean isCheckId)
    {
        if (widget1 == null || widget2 == null)
            return false;
        return widget1.width == widget2.width
                && widget1.height == widget2.height
                && widget1.x == widget2.x
                && widget1.y == widget2.y
                && (!isCheckId || widget1.getId() == widget2.id);
    }
}
