package com.widgets.example.demo.controllers;

import com.widgets.example.demo.exceptions.InvalidFilterParamsException;
import com.widgets.example.demo.exceptions.InvalidPaginationParamsException;
import com.widgets.example.demo.models.*;
import com.widgets.example.demo.operations.IWidgetOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/widgets")
public class WidgetsController {

    @Autowired
    private IWidgetOperations operations;
    public static final String PageCountHeaderName = "DEMO_PAGE_COUNT";
    public static final int minPageSize = 10;
    public static final int maxPageSize = 500;

    @GetMapping
    public ResponseEntity<List<ReadonlyWidget>> GetWidgets(@RequestBody(required = false) GetWidgetsViewModel viewModel)
            throws InvalidFilterParamsException, InvalidPaginationParamsException {
        FilterParams filterParams = null;
        PaginationParams paginationParams = null;
        if (viewModel != null)
        {
            filterParams = viewModel.filterParams;
            paginationParams = viewModel.paginationParams;
        }
        HttpHeaders responseHeaders = new HttpHeaders();
        var result = operations.getWidgets(filterParams);
        if (paginationParams != null)
        {
            if (IsValidPaginationParams(paginationParams))
            {
                var newResult = new ArrayList<ReadonlyWidget>();
                var size = result.size();
                for (int i = paginationParams.pageSize*paginationParams.pageOffset; i < size && i < paginationParams.pageSize*(paginationParams.pageOffset+1); i++)
                {
                    newResult.add(result.get(i));
                }
                var pagesCount = size / paginationParams.pageSize;
                if (size % paginationParams.pageSize > 0)
                    pagesCount++;
                responseHeaders.add(PageCountHeaderName,String.valueOf(pagesCount));
                result = newResult;
            }
            else
                throw new InvalidPaginationParamsException("Invalid pagination params");
        }
        return ResponseEntity.ok().headers(responseHeaders).body(result) ;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReadonlyWidget> GetWidget(@PathVariable UUID id)
    {
        return ResponseEntity.ok().body(operations.getWidget(id));
    }

    private boolean IsValidPaginationParams(PaginationParams paginationParams) {
        return (paginationParams != null) && paginationParams.pageOffset >=0
                && paginationParams.pageSize >=minPageSize && paginationParams.pageSize <= maxPageSize;
    }

    @PostMapping
    public ReadonlyWidget AddWidget(@RequestBody Widget widget)
    {
        return operations.addWidget(widget);
    }

    @PutMapping
    public ReadonlyWidget UpdateWidget(@RequestBody Widget widget)
    {
        return operations.updateWidget(widget);
    }

    @DeleteMapping
    public void RemoveWidget(@RequestBody UUID widgetId)
    {
        operations.removeWidget(widgetId);
    }
}