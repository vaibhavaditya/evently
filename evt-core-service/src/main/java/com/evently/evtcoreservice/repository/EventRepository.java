package com.evently.evtcoreservice.repository;

import com.evently.evtcoreservice.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface EventRepository
        extends JpaRepository<Event, UUID>,
        JpaSpecificationExecutor<Event> {

    boolean existsByOrganizerMobile(String organizerMobile);
}