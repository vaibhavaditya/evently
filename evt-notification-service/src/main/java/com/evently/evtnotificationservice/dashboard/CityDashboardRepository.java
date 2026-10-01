package com.evently.evtnotificationservice.dashboard;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface CityDashboardRepository
        extends MongoRepository<CityDashboard, String> {
}