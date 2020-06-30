package com.widgets.example.demo.main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.widgets.example.demo.controllers.WidgetsController;
import com.widgets.example.demo.exceptions.InvalidPaginationParamsException;
import com.widgets.example.demo.models.*;
import com.widgets.example.demo.operations.IWidgetOperations;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static com.widgets.example.demo.main.CommonLogic.isEqualWidgets;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@AutoConfigureMockMvc
public class WidgetsControllerIntegrationTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IWidgetOperations operations;

    @Autowired
    private WidgetsController controller;

    Widget widget = new Widget(10,10,100,100,0);
    ObjectMapper objectMapper = new ObjectMapper();
    PaginationParams paginationParams = new PaginationParams();
    GetWidgetsViewModel viewModel = new GetWidgetsViewModel();
    private int pageSize = 10;

    private UUID AddWidgets(Widget _widget, int count)
    {
        UUID result = null;
        for (int i=0;i<count;i++)
            result = operations.addWidget(_widget).id;
        return result;
    }

    private void setDefaultViewModelWithPaginationParams()
    {
        paginationParams.pageSize = pageSize;
        viewModel.paginationParams = paginationParams;
    }

    private void CheckPagination(GetWidgetsViewModel viewModel,int expectedResultLength,int expectedPageCount) throws Exception {
        var result = mockMvc.perform(
                get("/widgets")
                        .content(objectMapper.writeValueAsString(viewModel))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
        result.andExpect(mvcResult ->
        {
            var responce = mvcResult.getResponse();
            var isValidHeader = responce.containsHeader(WidgetsController.PageCountHeaderName);
            if (isValidHeader)
                isValidHeader = responce.getHeader(WidgetsController.PageCountHeaderName).equals(Integer.toString(expectedPageCount));
            assertThat(isValidHeader).isTrue();
        });
        result.andExpect(mvcResult ->
                {
                    var content = mvcResult.getResponse()
                            .getContentAsString();
                    var list = objectMapper.readValue(content, POCOWidget[].class);
                    assertThat(list.length == expectedResultLength
                    ).isTrue();
                }
        );
    }

    private void checkThatThrowInvalidPaginationParams()
    {
        assertThrows(InvalidPaginationParamsException.class,() -> {
            controller.GetWidgets(viewModel);
        });
    }

    @Test
    public void shouldWorkCRUDOperations() throws Exception {
        //Add widget
        var jsonContent = objectMapper.writeValueAsString(widget);
        mockMvc.perform(
                post("/widgets")
                        .content(jsonContent)
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
        var widgets = operations.getWidgets(null);
        assertThat(widgets.size() == 1);
        assertThat(isEqualWidgets(widget,widgets.get(0),false));
        //Update widget
        widget = new Widget(widgets.get(0));
        widget.x = widget.x+10;
        jsonContent = objectMapper.writeValueAsString(widget);
        mockMvc.perform(
                put("/widgets")
                        .content(jsonContent)
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
        widgets = operations.getWidgets(null);
        assertThat(widgets.size() == 1);
        assertThat(isEqualWidgets(widget,widgets.get(0),true));
        //Remove widget
        jsonContent = objectMapper.writeValueAsString(widget.getId());
        mockMvc.perform(
                delete("/widgets")
                        .content(jsonContent)
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
        widgets = operations.getWidgets(null);
        assertThat(widgets.size() == 0);
    }

    @Test
    public void shouldGetWidgetsWorkWithPagination() throws Exception {
        setDefaultViewModelWithPaginationParams();
        for (int i = 1; i < 5;i++)
        {
            widget.x = widget.x-100;
            AddWidgets(widget,pageSize/2);
        }
        //check without filters
        CheckPagination(viewModel,pageSize,2);
        //check with filters
        viewModel.filterParams = new FilterParams(widget);
        CheckPagination(viewModel,pageSize/2,1);
    }

    @Test
    public void shouldGetWidgetWork() throws Exception
    {
        var id = AddWidgets(widget,1);
        mockMvc.perform(
                get("/widgets/"+id.toString())
        ).andExpect(status().isOk());
    }

    @Test
    public void shouldThrowGetWidgetsWithInvalidPaginationParams() throws Exception {
        setDefaultViewModelWithPaginationParams();

        paginationParams.pageSize = WidgetsController.minPageSize-1;
        checkThatThrowInvalidPaginationParams();

        paginationParams.pageSize = WidgetsController.maxPageSize+1;
        checkThatThrowInvalidPaginationParams();

        paginationParams.pageSize = pageSize;
        paginationParams.pageOffset = -1;
        checkThatThrowInvalidPaginationParams();
    }


}
