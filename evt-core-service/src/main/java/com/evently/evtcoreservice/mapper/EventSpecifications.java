package com.evently.evtcoreservice.repository.specification;

import com.evently.evtcoreservice.entity.Event;
import com.evently.evtcoreservice.enums.EventCategory;
import com.evently.evtcoreservice.enums.EventStatus;
import org.springframework.data.jpa.domain.Specification;

public final class EventSpecifications {

    private EventSpecifications() {
    }

    public static Specification<Event> hasCity(String city) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("city"), city);
    }

    public static Specification<Event> hasCategory(
            EventCategory category) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("category"), category);
    }

    public static Specification<Event> hasStatus(
            EventStatus status) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), status);
    }
}