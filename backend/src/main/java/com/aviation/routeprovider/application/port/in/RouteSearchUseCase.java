package com.aviation.routeprovider.application.port.in;

import com.aviation.routeprovider.api.rest.dto.response.RouteResponse;

import java.util.List;

public interface RouteSearchUseCase {
    List<RouteResponse> searchRoutes(RouteSearchQuery query);
}
