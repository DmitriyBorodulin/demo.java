package com.widgets.example.demo.main;

import com.widgets.example.demo.exceptions.InvalidFilterParamsException;
import com.widgets.example.demo.models.FilterParams;
import com.widgets.example.demo.models.ReadonlyWidget;
import com.widgets.example.demo.models.Widget;
import com.widgets.example.demo.repositories.ZIndexBasedWidgetRepository;
import org.junit.jupiter.api.Test;

import java.util.Hashtable;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

public class ZIndexBasedWidgetRepositoryTests {

    private ZIndexBasedWidgetRepository operations = new ZIndexBasedWidgetRepository();

    private Widget widget = new Widget(0,0,100,100,0);

    private Hashtable<UUID, ReadonlyWidget> repositoryList;

    @Test
    public void shouldAddThreeWidgetsWithRightZIndex() throws InvalidFilterParamsException
    {
        operations.addWidget(widget);
        operations.addWidget(widget);
        operations.addWidget(widget);
        var widgets = operations.getWidgets(null);
        assertThat(widgets.size() == 3).isTrue();
        assertThat(widgets.get(0).zLevel == 0).isTrue();
        assertThat(widgets.get(1).zLevel == 1).isTrue();
        assertThat(widgets.get(2).zLevel == 2).isTrue();
    }

    @Test
    public void shouldUpdate2000WidgetsWithRightZIndexAndGetWidgets2000Times() throws InvalidFilterParamsException
    {
        for (var i =0 ; i< 2000;i++)
            operations.addWidget(widget);
        widget = new Widget(operations.addWidget(widget));
        widget.zLevel = 200;
        operations.updateWidget(widget);
        var widgets = operations.getWidgets(null);
        assertThat(widgets.size() == 2001).isTrue();
        assertThat(widgets.get(0).zLevel == 1).isTrue();
        assertThat(widgets.get(1).zLevel == 2).isTrue();
        assertThat(widgets.get(2).zLevel == 3).isTrue();
        operations.removeWidget(widget.getId());
        widget.zLevel = 0;
        operations.addWidget(widget);
        for (var i =0 ; i< 2000;i++)
            widgets = operations.getWidgets(null);
        assertThat(widgets.size() == 2001).isTrue();
        assertThat(widgets.get(0).zLevel == 0).isTrue();
        assertThat(widgets.get(1).zLevel == 1).isTrue();
        assertThat(widgets.get(2).zLevel == 2).isTrue();
    }

    @Test
    public void shouldWorkWithValidFilterParams() throws InvalidFilterParamsException
    {
        operations.addWidget(widget);
        operations.addWidget(widget);
        widget.x = 100;
        var rWidget = operations.addWidget(widget);
        var allWidgets = operations.getWidgets(null);
        var filteredWidgets = operations.getWidgets(new FilterParams(rWidget));
        assertThat(allWidgets.size() == 3).isTrue();
        assertThat(filteredWidgets.size() == 1).isTrue();
    }

    @Test
    public void shouldThrowWithInvalidFilterParams()
    {
        var filterParams = new FilterParams(widget);
        filterParams.width = -1;
        FilterParams finalFilterParam1 = filterParams;
        assertThrows(InvalidFilterParamsException.class,() -> operations.getWidgets(finalFilterParam1));
        filterParams = new FilterParams(widget);
        filterParams.height = -1;
        FilterParams finalFilterParam2 = filterParams;
        assertThrows(InvalidFilterParamsException.class,() -> operations.getWidgets(finalFilterParam2));
    }

}
