package com.widgets.example.demo.main;

import com.widgets.example.demo.exceptions.InvalidFilterParamsException;
import com.widgets.example.demo.models.FilterParams;
import com.widgets.example.demo.models.Widget;
import com.widgets.example.demo.repositories.IZIndexBasedWidgetRepository;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

abstract class ZIndexBasedWidgetRepositoryTests {

    protected IZIndexBasedWidgetRepository repository;

    protected int getWidgetsNTimes = 2000;
    protected int addWidgetsNTimes = 2000;

    private Widget widget = new Widget(0,0,100,100,0);

    @Test
    public void shouldAddThreeWidgetsWithRightZIndex() throws InvalidFilterParamsException, SQLException {
        repository.addWidget(widget);
        repository.addWidget(widget);
        repository.addWidget(widget);
        var widgets = repository.getWidgets(null);
        assertThat(widgets.size() == 3).isTrue();
        assertThat(widgets.get(0).zLevel == 0).isTrue();
        assertThat(widgets.get(1).zLevel == 1).isTrue();
        assertThat(widgets.get(2).zLevel == 2).isTrue();
    }

    @Test
    public void shouldUpdateNWidgetsWithRightZIndexAndGetWidgetsNTimes() throws InvalidFilterParamsException, SQLException {
        for (var i =1 ; i< addWidgetsNTimes;i++)
            repository.addWidget(widget);
        widget = new Widget(repository.addWidget(widget));
        widget.zLevel = addWidgetsNTimes / 2;
        repository.updateWidget(widget);
        var widgets = repository.getWidgets(null);
        assertThat(widgets.size() == addWidgetsNTimes).isTrue();
        assertThat(widgets.get(0).zLevel == 1).isTrue();
        assertThat(widgets.get(1).zLevel == 2).isTrue();
        assertThat(widgets.get(2).zLevel == 3).isTrue();
        repository.removeWidget(widget.getId());
        widget.zLevel = 0;
        repository.addWidget(widget);
        for (var i =0 ; i< getWidgetsNTimes;i++)
            widgets = repository.getWidgets(null);
        assertThat(widgets.size() == addWidgetsNTimes).isTrue();
        assertThat(widgets.get(0).zLevel == 0).isTrue();
        assertThat(widgets.get(1).zLevel == 1).isTrue();
        assertThat(widgets.get(2).zLevel == 2).isTrue();
    }

    @Test
    public void shouldWorkWithValidFilterParams() throws InvalidFilterParamsException, SQLException {
        repository.addWidget(widget);
        repository.addWidget(widget);
        widget.x = 100;
        var rWidget = repository.addWidget(widget);
        var allWidgets = repository.getWidgets(null);
        var filteredWidgets = repository.getWidgets(new FilterParams(rWidget));
        assertThat(allWidgets.size() == 3).isTrue();
        assertThat(filteredWidgets.size() == 1).isTrue();
    }

    @Test
    public void shouldThrowWithInvalidFilterParams()
    {
        var filterParams = new FilterParams(widget);
        filterParams.width = -1;
        FilterParams finalFilterParam1 = filterParams;
        assertThrows(InvalidFilterParamsException.class,() -> repository.getWidgets(finalFilterParam1));
        filterParams = new FilterParams(widget);
        filterParams.height = -1;
        FilterParams finalFilterParam2 = filterParams;
        assertThrows(InvalidFilterParamsException.class,() -> repository.getWidgets(finalFilterParam2));
    }

}
