package com.widgets.example.demo.main;

import com.widgets.example.demo.exceptions.InvalidFilterParamsException;
import com.widgets.example.demo.models.FilterParams;
import com.widgets.example.demo.models.ReadonlyWidget;
import com.widgets.example.demo.models.Widget;
import com.widgets.example.demo.operations.ZIndexBasedWidgetOperations;
import com.widgets.example.demo.repositories.IWidgetRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

@SpringBootTest
public class ZIndexBasedWidgetOperationsTests {

    @InjectMocks
    private ZIndexBasedWidgetOperations operations;

    @Mock
    private IWidgetRepository repository;

    private Widget widget = new Widget(0,0,100,100,0);

    private Hashtable<UUID,ReadonlyWidget> repositoryList;

    public void initMocks(){
        repositoryList = new Hashtable<UUID,ReadonlyWidget>();
        MockitoAnnotations.initMocks(this);
        Mockito.when(repository.getWidgets()).then(x ->{
            return repositoryList.elements();
        });
        Mockito.when(repository.getWidget(Mockito.argThat(arg -> true))).then(arg -> repositoryList.get(arg));
        Mockito.when(repository.bulkUpdateWidgets(Mockito.argThat(arg -> true),Mockito.argThat(arg -> true),Mockito.argThat(arg -> true))).
                then((args) -> {
                    var result = new ArrayList<ReadonlyWidget>();
                    Collection<Widget> added = args.getArgument(0);
                    Collection<Widget> updated = args.getArgument(1);
                    Collection<UUID> removed = args.getArgument(2);
                    if (added != null)
                        added.forEach(w ->
                        {
                            var widget = new ReadonlyWidget(w, null, UUID.randomUUID());
                            result.add(widget);
                            repositoryList.put(widget.id,widget);
                        });
                    if (updated != null)
                        updated.forEach(w ->
                        {
                            var widget = new ReadonlyWidget(w, null, w.getId());
                            result.add(widget);
                            repositoryList.replace(widget.id,widget);
                        });
                    if (removed != null)
                        removed.forEach(id ->
                        {
                            result.add(repositoryList.get(id));
                            repositoryList.remove(id);
                        });
                    return result;
                });

    }

    @Test
    public void shouldAddThreeWidgetsWithRightZIndex() throws InvalidFilterParamsException {
        initMocks();
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
    public void shouldUpdateThreeWidgetsWithRightZIndex() throws InvalidFilterParamsException {
        initMocks();
        operations.addWidget(widget);
        operations.addWidget(widget);
        widget = new Widget(operations.addWidget(widget));
        widget.zLevel = 1;
        operations.updateWidget(widget);
        var widgets = operations.getWidgets(null);
        assertThat(widgets.size() == 3).isTrue();
        assertThat(widgets.get(0).zLevel == 1).isTrue();
        assertThat(widgets.get(1).zLevel == 2).isTrue();
        assertThat(widgets.get(2).zLevel == 3).isTrue();
        operations.removeWidget(widget.getId());
        widget.zLevel = 0;
        operations.addWidget(widget);
        widgets = operations.getWidgets(null);
        assertThat(widgets.size() == 3).isTrue();
        assertThat(widgets.get(0).zLevel == 0).isTrue();
        assertThat(widgets.get(1).zLevel == 2).isTrue();
        assertThat(widgets.get(2).zLevel == 3).isTrue();
    }

    @Test
    public void shouldWorkWithValidFilterParams() throws InvalidFilterParamsException {
        initMocks();
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
    public void shouldThrowWithInvalidFilterParamX() throws InvalidFilterParamsException {
        initMocks();
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
