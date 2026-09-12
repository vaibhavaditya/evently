package com.evently.evtopenservice.mapper;

import com.evently.evtopenservice.dto.CategoryCountHttpResponse;
import com.evently.evtopenservice.dto.EventStatsHttpResponse;
import com.evently.evtopenservice.dto.StatusCountHttpResponse;
import com.evently.grpc.CategoryCount;
import com.evently.grpc.GetEventStatsResponse;
import com.evently.grpc.StatusCount;

import java.util.List;

public class EventStatsResponseMapper {

    public static EventStatsHttpResponse toHttp(GetEventStatsResponse response) {
        List<StatusCountHttpResponse> byStatus =
                response.getByStatusList()
                        .stream()
                        .map(EventStatsResponseMapper::toHttp)
                        .toList();

        List<CategoryCountHttpResponse> byCategory =
                response.getByCategoryList()
                        .stream()
                        .map(EventStatsResponseMapper::toHttp)
                        .toList();

        EventStatsHttpResponse httpResponse = new EventStatsHttpResponse();

        httpResponse.setTotalEvents(response.getTotalEvents());
        httpResponse.setByStatus(byStatus);
        httpResponse.setByCategory(byCategory);

        return httpResponse;
    }

    private static StatusCountHttpResponse toHttp(StatusCount statusCount) {
        StatusCountHttpResponse httpResponse = new StatusCountHttpResponse();

        httpResponse.setStatus(statusCount.getStatus().name());
        httpResponse.setCount(statusCount.getCount());
        return httpResponse;
    }

    private static CategoryCountHttpResponse toHttp(CategoryCount categoryCount) {
        CategoryCountHttpResponse httpResponse = new CategoryCountHttpResponse();

        httpResponse.setCategory(categoryCount.getCategory().name());
        httpResponse.setCount(categoryCount.getCount());
        return httpResponse;
    }
}
