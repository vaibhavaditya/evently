
package com.evently.evtcoreservice.service;

import com.evently.evtcoreservice.dto.EventRequest;
import com.evently.evtcoreservice.dto.EventResponse;
import com.evently.evtcoreservice.dto.StatusUpdateRequest;
import com.evently.evtcoreservice.entity.Event;
import com.evently.evtcoreservice.enums.EventStatus;
import com.evently.evtcoreservice.exception.BadRequestException;
import com.evently.evtcoreservice.exception.ConflictException;
import com.evently.evtcoreservice.mapper.EventMapper;
import com.evently.evtcoreservice.repository.EventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventMapper eventMapper;

    @InjectMocks
    private EventServiceImpl eventService;

    @Test
    void createEvent_validRequest_savesEventAndReturnsResponse() {

        // Arrange: prepare the test
        EventRequest request = mock(EventRequest.class);
        when(request.getOrganizerMobile()).thenReturn("9876543210");

        when(eventRepository.existsByOrganizerMobile("9876543210"))
                .thenReturn(false);

        Event event = mock(Event.class);
        Event savedEvent = mock(Event.class);
        EventResponse expectedResponse = mock(EventResponse.class);

        when(eventMapper.toEntity(request)).thenReturn(event);
        when(eventRepository.save(event)).thenReturn(savedEvent);
        when(eventMapper.toResponse(savedEvent)).thenReturn(expectedResponse);

        // Act: call the method being tested
        EventResponse actualResponse = eventService.createEvent(request);

        // Assert: verify the result and repository interaction
        assertSame(expectedResponse, actualResponse);
        verify(eventRepository).save(event);
    }

    @Test
    void createEvent_capturesEventPassedToSave(){
        EventRequest request = mock(EventRequest.class);
        when(request.getOrganizerMobile()).thenReturn("9876543210");

        when(eventRepository.existsByOrganizerMobile("9876543210"))
                .thenReturn(false);

        Event event = mock(Event.class);
        Event savedEvent = mock(Event.class);
        EventResponse expectedResponse = mock(EventResponse.class);

        when(eventMapper.toEntity(request)).thenReturn(event);
        when(eventRepository.save(event)).thenReturn(savedEvent);
        when(eventMapper.toResponse(savedEvent)).thenReturn(expectedResponse);

        EventResponse actualResponse = eventService.createEvent(request);

        // Capture the event passed to save()
        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(captor.capture());

        Event capturedEvent = captor.getValue();

        // Assert
        assertSame(event, capturedEvent);

    }
    @Test
    void createEvent_duplicateMobile_throwsConflictException(){
        EventRequest request = mock(EventRequest.class);
        when(request.getOrganizerMobile()).thenReturn("9876543210");

        when(eventRepository.existsByOrganizerMobile("9876543210")).thenReturn(true);

        assertThrows(
                ConflictException.class,
                () -> eventService.createEvent(request)
        );

        verify(eventRepository,never()).save(any(Event.class));
    }

    @Test
    void updateStatus_illegalTransition_throwsBadRequestException(){
        UUID eventId = UUID.randomUUID();
        Event event = mock(Event.class);

        when(event.getStatus()).thenReturn(EventStatus.DRAFT);
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        StatusUpdateRequest request = mock(StatusUpdateRequest.class);
        when(request.getStatus()).thenReturn(EventStatus.CANCELLED);

        assertThrows(
                BadRequestException.class,
                () -> eventService.updateStatus(eventId, request)
        );

        verify(eventRepository,never()).save(any(Event.class));
    }

    @Test
    void updateStatus_legalTransition_updatesAndReturnsResponse(){
        UUID eventId = UUID.randomUUID();

        Event event = mock(Event.class);
        when(event.getStatus()).thenReturn(EventStatus.DRAFT);

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        StatusUpdateRequest request = mock(StatusUpdateRequest.class);
        when(request.getStatus()).thenReturn(EventStatus.PUBLISHED);
        Event savedEvent = mock(Event.class);
        EventResponse expectedResponse = mock(EventResponse.class);

        when(eventRepository.save(event)).thenReturn(savedEvent);
        when(eventMapper.toResponse(savedEvent)).thenReturn(expectedResponse);

        // Act
        EventResponse actualResponse = eventService.updateStatus(eventId, request);
        assertSame(expectedResponse, actualResponse);
        verify(event).setStatus(EventStatus.PUBLISHED);
        verify(eventRepository).save(event);
    }
}
