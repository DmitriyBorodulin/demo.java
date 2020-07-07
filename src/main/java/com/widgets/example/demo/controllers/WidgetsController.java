package com.widgets.example.demo.controllers;

import com.widgets.example.demo.exceptions.InvalidFilterParamsException;
import com.widgets.example.demo.exceptions.InvalidPaginationParamsException;
import com.widgets.example.demo.models.FilterParams;
import com.widgets.example.demo.models.PaginationParams;
import com.widgets.example.demo.models.ReadonlyWidget;
import com.widgets.example.demo.models.Widget;
import com.widgets.example.demo.operations.IZIndexBasedWidgetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/widgets")
public class WidgetsController {

    @Autowired
    private IZIndexBasedWidgetRepository operations;
    public static final String PageCountHeaderName = "DEMO_PAGE_COUNT";
    public static final int minPageSize = 10;
    public static final int maxPageSize = 500;

    @GetMapping
    public ResponseEntity<List<ReadonlyWidget>> getWidgets(Integer pageSize,
                                                           Integer pageOffset,
                                                           Long filterX,
                                                           Long filterY,
                                                           Long filterWidth,
                                                           Long filterHeight)
            throws InvalidFilterParamsException, InvalidPaginationParamsException {
        FilterParams filterParams = null;
        PaginationParams paginationParams = null;
        if (filterX != null || filterY != null || filterWidth != null || filterHeight != null) {
            if (filterX != null && filterY != null && filterWidth != null && filterHeight != null) {
                filterParams = new FilterParams();
                filterParams.x = filterX;
                filterParams.y = filterY;
                filterParams.width = filterWidth;
                filterParams.height = filterHeight;
            }
            else
                throw new InvalidFilterParamsException("All or none params for filter must be set");
        }
        if (pageSize != null || pageOffset != null)
        {
            if (pageSize != null && pageOffset != null)
            {
                paginationParams = new PaginationParams();
                paginationParams.pageOffset = pageOffset;
                paginationParams.pageSize = pageSize;
            }
            else
                throw new InvalidPaginationParamsException("All or none params for pagination must be set");
        }
        HttpHeaders responseHeaders = new HttpHeaders();
        var result = operations.getWidgets(filterParams);
        if (paginationParams != null)
        {
            if (isValidPaginationParams(paginationParams))
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
    public ResponseEntity<ReadonlyWidget> getWidget(@PathVariable UUID id)
    {
        return ResponseEntity.ok().body(operations.getWidget(id));
    }

    private boolean isValidPaginationParams(PaginationParams paginationParams) {
        return (paginationParams != null) && paginationParams.pageOffset >=0
                && paginationParams.pageSize >=minPageSize && paginationParams.pageSize <= maxPageSize;
    }

    @PostMapping
    public ResponseEntity<ReadonlyWidget> addWidget(@RequestBody Widget widget)
    {
        return ResponseEntity.ok().body(operations.addWidget(widget));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReadonlyWidget> updateWidget(@RequestBody Widget widget, @PathVariable UUID id)
    {
        var rWidget = operations.getWidget(id);
        if (rWidget != null)
        {
            var updatedWidget = new Widget(rWidget);
            updatedWidget.copyFrom(widget);
            return ResponseEntity.ok(operations.updateWidget(updatedWidget));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity removeWidget(@PathVariable UUID id)
    {
        var rWidget = operations.getWidget(id);
        if (rWidget != null)
        {
            operations.removeWidget(id);
            return ResponseEntity.ok().body(null);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }
}
