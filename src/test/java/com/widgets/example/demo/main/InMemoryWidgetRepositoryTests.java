package com.widgets.example.demo.main;

import com.widgets.example.demo.models.ReadonlyWidget;
import com.widgets.example.demo.models.Widget;
import com.widgets.example.demo.repositories.InMemoryWidgetRepository;
import org.assertj.core.api.AbstractAssert;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;
import java.util.List;

import static com.widgets.example.demo.main.CommonLogic.isEqualWidgets;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class InMemoryWidgetRepositoryTests {

    InMemoryWidgetRepository repository = new InMemoryWidgetRepository();

    Widget widget = new Widget(10,10,100,100,0);


    private List<ReadonlyWidget> AddWidgetInner(Widget widget)
    {
        return repository.bulkUpdateWidgets(List.of(widget),null,null);
    }

    private List<ReadonlyWidget> GetWidgets()
    {
        return Collections.list(repository.getWidgets());
    }

    private void assertThatListIsOneElementAndEqualToWidget(List<ReadonlyWidget> list, Widget widget, boolean isCheckId)
    {
        assertThatListIsSizeOf(list,1);
        assertThatElementsAreEqual(list.get(0),widget,isCheckId);
    }

    private void assertThatListIsOneElementAndEqualToWidget(List<ReadonlyWidget> list, Widget widget)
    {
        assertThatListIsOneElementAndEqualToWidget(list,widget,true);
    }

    private AbstractAssert assertThatElementsAreEqual(ReadonlyWidget readonlyWidget, Widget widget, boolean isCheckId)
    {
        return assertThat(isEqualWidgets(widget,readonlyWidget,isCheckId)).isTrue().as("List and source widget is not equal");
    }

    private AbstractAssert assertThatElementsAreEqual(ReadonlyWidget readonlyWidget, Widget widget)
    {
        return assertThatElementsAreEqual(readonlyWidget,widget,true);
    }

    private AbstractAssert assertThatListIsSizeOf(List<ReadonlyWidget> list, int size)
    {
        return assertThat(list.size() == size).isTrue().as("List must contain "+size+" element(s)");
    }

    @Test
    void shouldAddWidget() throws Exception
    {
        var list = AddWidgetInner(widget);
        assertThatListIsOneElementAndEqualToWidget(list,widget,false);
        assertThatListIsOneElementAndEqualToWidget(GetWidgets(),widget,false);
    }

    @Test
    void shouldUpdateWidgetAndChangeDate() throws Exception
    {
        var list = AddWidgetInner(widget);
        var addChangeDate = list.get(0).changeDate;
        widget = new Widget(list.get(0));
        widget.x = widget.x+10;
        widget.y = widget.y+10;
        widget.height = widget.height+50;
        widget.width = widget.width+50;
        list = repository.bulkUpdateWidgets(null,List.of(widget),null);
        var updateChangeDate = list.get(0).changeDate;
        assertThatListIsOneElementAndEqualToWidget(list, widget);
        assertThatListIsOneElementAndEqualToWidget(GetWidgets(), widget);
        assertThat(addChangeDate.before(updateChangeDate)).as("Add date must be before update date");
    }

    @Test
    void shouldAddTwoWidgetAndUpdateOneWidget() throws Exception
    {
        AddWidgetInner(widget);
        var list = AddWidgetInner(widget);
        widget = new Widget(list.get(0));
        widget.x = widget.x+10;
        widget.y = widget.y+10;
        widget.height = widget.height+50;
        widget.width = widget.width+50;
        list = repository.bulkUpdateWidgets(null,List.of(widget),null);
        assertThatListIsSizeOf(GetWidgets(),2);
        assertThatListIsSizeOf(list,1);
    }

    @Test
    void shouldRemoveWidget() throws Exception
    {
        var list = AddWidgetInner(widget);
        widget = new Widget(list.get(0));
        list = repository.bulkUpdateWidgets(null,null,List.of(widget.getId()));
        assertThatListIsOneElementAndEqualToWidget(list,widget);
        assertThatListIsSizeOf(GetWidgets(),0);
    }

    @Test
    void shouldGetWidgetAndGetWidgets() throws Exception
    {
        var list = AddWidgetInner(widget);
        var newWidget = new Widget(list.get(0));
        var getResult = repository.getWidget(list.get(0).id);
        assertThatElementsAreEqual(getResult,widget,false);
        assertThatElementsAreEqual(getResult,newWidget);
        assertThatListIsOneElementAndEqualToWidget(GetWidgets(), newWidget);
    }

}
