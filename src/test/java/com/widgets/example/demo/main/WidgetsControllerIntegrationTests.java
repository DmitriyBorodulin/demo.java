package com.widgets.example.demo.main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.widgets.example.demo.controllers.WidgetsController;
import com.widgets.example.demo.exceptions.InvalidPaginationParamsException;
import com.widgets.example.demo.models.*;
import com.widgets.example.demo.repositories.IZIndexBasedWidgetRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.sql.SQLException;
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
    private IZIndexBasedWidgetRepository operations;

    @Autowired
    private WidgetsController controller;

    Widget widget = new Widget(10,10,100,100,0);
    ObjectMapper objectMapper = new ObjectMapper();
    PaginationParams paginationParams = new PaginationParams();
    private int pageSize = 10;

    private UUID addWidgets(Widget _widget, int count) throws SQLException {
        UUID result = null;
        for (int i=0;i<count;i++)
            result = operations.addWidget(_widget).id;
        return result;
    }

    private void setDefaults()
    {
        paginationParams.pageSize = pageSize;
    }

    private void checkPagination(PaginationParams paginationParams, int expectedResultLength, int expectedPageCount) throws Exception
    {
        checkPagination(paginationParams,null,expectedResultLength,expectedPageCount);
    }

    private void checkPagination(PaginationParams paginationParams, FilterParams filterParams, int expectedResultLength, int expectedPageCount) throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        if (paginationParams != null)
        {
            params.add("pageOffset",Long.toString(paginationParams.pageOffset));
            params.add("pageSize",Long.toString(paginationParams.pageSize));
        }
        if (filterParams != null)
        {
            params.add("filterHeight",Long.toString(filterParams.height));
            params.add("filterWidth",Long.toString(filterParams.width));
            params.add("filterX",Long.toString(filterParams.x));
            params.add("filterY",Long.toString(filterParams.y));
        }
        UriComponents uriComponents =     UriComponentsBuilder.fromPath("/widgets").queryParams(params).build();
        var result = mockMvc.perform(
                get(uriComponents.toUriString())
        ).andExpect(status().isOk());
        result.andExpect(mvcResult ->
        {
            var response = mvcResult.getResponse();
            var isValidHeader = response.containsHeader(WidgetsController.PageCountHeaderName);
            if (isValidHeader)
                isValidHeader = response.getHeader(WidgetsController.PageCountHeaderName).equals(Integer.toString(expectedPageCount));
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
        assertThrows(InvalidPaginationParamsException.class,() -> controller.getWidgets(
                paginationParams.pageSize,paginationParams.pageOffset,null,null,null,null));
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
                put("/widgets/"+widget.getId())
                        .content(jsonContent)
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
        widgets = operations.getWidgets(null);
        assertThat(widgets.size() == 1);
        assertThat(isEqualWidgets(widget,widgets.get(0),true));
        //Remove widget
        mockMvc.perform(
                delete("/widgets/"+widgets.get(0).id)
        ).andExpect(status().isOk());
        widgets = operations.getWidgets(null);
        assertThat(widgets.size() == 0);
    }

    @Test
    public void shouldGetWidgetsWorkWithPagination() throws Exception {
        setDefaults();
        for (int i = 1; i < 5;i++)
        {
            widget.x = widget.x-100;
            addWidgets(widget,pageSize/2);
        }
        //check without filters
        checkPagination(paginationParams,pageSize,2);
        //check with filters
        var filterParams = new FilterParams(widget);
        checkPagination(paginationParams,filterParams,pageSize/2,1);
    }

    @Test
    public void shouldGetWidgetWork() throws Exception
    {
        var id = addWidgets(widget,1);
        mockMvc.perform(
                get("/widgets/"+id.toString())
        ).andExpect(status().isOk());
    }

    @Test
    public void shouldThrowGetWidgetsWithInvalidPaginationParams() {
        setDefaults();

        paginationParams.pageSize = WidgetsController.minPageSize-1;
        checkThatThrowInvalidPaginationParams();

        paginationParams.pageSize = WidgetsController.maxPageSize+1;
        checkThatThrowInvalidPaginationParams();

        paginationParams.pageSize = pageSize;
        paginationParams.pageOffset = -1;
        checkThatThrowInvalidPaginationParams();
    }

}
