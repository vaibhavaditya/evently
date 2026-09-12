package com.evently.evtcoreservice.grpc;

import com.evently.evtcoreservice.dto.*;
import com.evently.evtcoreservice.enums.EventCategory;
import com.evently.evtcoreservice.enums.EventStatus;
import com.evently.evtcoreservice.service.EventService;
import com.evently.grpc.*;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventGrpcService extends EventServiceGrpc.EventServiceImplBase {

    private final EventService eventService;

    // =====================================================
    // ENUM MAPPERS
    // =====================================================

    private EventCategory toJavaCategory(
            com.evently.grpc.EventCategory category) {

        return switch (category) {
            case MUSIC -> EventCategory.MUSIC;
            case SPORTS -> EventCategory.SPORTS;
            case COMEDY -> EventCategory.COMEDY;
            case WORKSHOP -> EventCategory.WORKSHOP;
            case OTHER -> EventCategory.OTHER;

            case EVENT_CATEGORY_UNSPECIFIED ->
                    throw new IllegalArgumentException(
                            "Event category is required"
                    );

            case UNRECOGNIZED ->
                    throw new IllegalArgumentException(
                            "Invalid event category"
                    );
        };
    }

    private EventStatus toJavaStatus(
            com.evently.grpc.EventStatus status) {

        return switch (status) {
            case DRAFT -> EventStatus.DRAFT;
            case PUBLISHED -> EventStatus.PUBLISHED;
            case CANCELLED -> EventStatus.CANCELLED;
            case SOLD_OUT -> EventStatus.SOLD_OUT;

            case EVENT_STATUS_UNSPECIFIED ->
                    throw new IllegalArgumentException(
                            "Event status is required"
                    );

            case UNRECOGNIZED ->
                    throw new IllegalArgumentException(
                            "Invalid event status"
                    );
        };
    }

    private com.evently.grpc.EventCategory toGrpcCategory(
            EventCategory category) {

        return switch (category) {
            case MUSIC -> com.evently.grpc.EventCategory.MUSIC;
            case SPORTS -> com.evently.grpc.EventCategory.SPORTS;
            case COMEDY -> com.evently.grpc.EventCategory.COMEDY;
            case WORKSHOP -> com.evently.grpc.EventCategory.WORKSHOP;
            case OTHER -> com.evently.grpc.EventCategory.OTHER;
        };
    }

    private com.evently.grpc.EventStatus toGrpcStatus(
            EventStatus status) {

        return switch (status) {
            case DRAFT -> com.evently.grpc.EventStatus.DRAFT;
            case PUBLISHED -> com.evently.grpc.EventStatus.PUBLISHED;
            case CANCELLED -> com.evently.grpc.EventStatus.CANCELLED;
            case SOLD_OUT -> com.evently.grpc.EventStatus.SOLD_OUT;
        };
    }

    // =====================================================
    // REQUEST MAPPERS
    // =====================================================

    private EventRequest toJavaEventRequest(
            CreateEventRequest request) {

        EventRequest eventRequest = new EventRequest();

        eventRequest.setEventName(request.getEventName());
        eventRequest.setOrganizerName(request.getOrganizerName());
        eventRequest.setOrganizerMobile(request.getOrganizerMobile());
        eventRequest.setCity(request.getCity());
        eventRequest.setCategory(
                toJavaCategory(request.getCategory())
        );

        return eventRequest;
    }

    private StatusUpdateRequest toJavaStatusUpdateRequest(
            UpdateEventStatusRequest request) {

        StatusUpdateRequest statusUpdateRequest =
                new StatusUpdateRequest();

        statusUpdateRequest.setStatus(
                toJavaStatus(request.getStatus())
        );

        return statusUpdateRequest;
    }

    // =====================================================
    // RESPONSE MAPPERS
    // =====================================================

    private Event toGrpcEvent(EventResponse result) {

        return Event.newBuilder()
                .setId(result.getId().toString())
                .setEventName(result.getEventName())
                .setOrganizerName(result.getOrganizerName())
                .setOrganizerMobile(result.getOrganizerMobile())
                .setCity(result.getCity())
                .setCategory(toGrpcCategory(result.getCategory()))
                .setStatus(toGrpcStatus(result.getStatus()))
                .build();
    }

    private Event toGrpcEvent(EventDTO result) {

        return Event.newBuilder()
                .setId(result.getId().toString())
                .setEventName(result.getEventName())
                .setOrganizerName(result.getOrganizerName())
                .setOrganizerMobile(result.getOrganizerMobile())
                .setCity(result.getCity())
                .setCategory(toGrpcCategory(result.getCategory()))
                .setStatus(toGrpcStatus(result.getStatus()))
                .build();
    }

    private GetEventStatsResponse toGrpcStats(
            EventStatsResponse result) {

        GetEventStatsResponse.Builder response =
                GetEventStatsResponse.newBuilder();

        response.setTotalEvents(result.getTotalEvents());

        result.getByStatus().forEach((status, count) -> {

            StatusCount statusCount =
                    StatusCount.newBuilder()
                            .setStatus(toGrpcStatus(status))
                            .setCount(count)
                            .build();

            response.addByStatus(statusCount);
        });

        result.getByCategory().forEach((category, count) -> {

            CategoryCount categoryCount =
                    CategoryCount.newBuilder()
                            .setCategory(toGrpcCategory(category))
                            .setCount(count)
                            .build();

            response.addByCategory(categoryCount);
        });

        return response.build();
    }

    // =====================================================
    // CREATE EVENT
    // =====================================================

    @Override
    public void createEvent(
            CreateEventRequest request,
            StreamObserver<CreateEventResponse> responseObserver) {

        try {

            EventRequest eventRequest =
                    toJavaEventRequest(request);

            EventResponse result =
                    eventService.createEvent(eventRequest);

            Event grpcEvent =
                    toGrpcEvent(result);

            CreateEventResponse response =
                    CreateEventResponse.newBuilder()
                            .setEvent(grpcEvent)
                            .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {

            responseObserver.onError(
                    GrpcExceptionMapper.toGrpcException(e)
            );
        }
    }

    // =====================================================
    // GET EVENT
    // =====================================================

    @Override
    public void getEvent(
            GetEventRequest request,
            StreamObserver<GetEventResponse> responseObserver) {

        try {

            UUID id = UUID.fromString(request.getId());

            EventResponse result =
                    eventService.getEventById(id);

            Event grpcEvent =
                    toGrpcEvent(result);

            GetEventResponse response =
                    GetEventResponse.newBuilder()
                            .setEvent(grpcEvent)
                            .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {

            responseObserver.onError(
                    GrpcExceptionMapper.toGrpcException(e)
            );
        }
    }

    // =====================================================
    // LIST EVENTS
    // =====================================================

    @Override
    public void listEvents(
            ListEventsRequest request,
            StreamObserver<ListEventsResponse> responseObserver) {

        try {

            String city = request.getCity();

            EventCategory category = null;

            if (request.getCategory()
                    != com.evently.grpc.EventCategory.EVENT_CATEGORY_UNSPECIFIED) {

                category =
                        toJavaCategory(request.getCategory());
            }

            EventStatus status = null;

            if (request.getStatus()
                    != com.evently.grpc.EventStatus.EVENT_STATUS_UNSPECIFIED) {

                status =
                        toJavaStatus(request.getStatus());
            }

            int page = request.getPage();
            int size = request.getSize();

            EventListResponse result =
                    eventService.getEvents(
                            city,
                            category,
                            status,
                            page,
                            size
                    );

            List<Event> grpcEvents =
                    result.getEvents()
                            .stream()
                            .map(this::toGrpcEvent)
                            .toList();

            ListEventsResponse response =
                    ListEventsResponse.newBuilder()
                            .addAllEvents(grpcEvents)
                            .setTotalElements(
                                    result.getTotalElements()
                            )
                            .setTotalPages(
                                    result.getTotalPages()
                            )
                            .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {

            responseObserver.onError(
                    GrpcExceptionMapper.toGrpcException(e)
            );
        }
    }

    // =====================================================
    // UPDATE EVENT STATUS
    // =====================================================

    @Override
    public void updateEventStatus(
            UpdateEventStatusRequest request,
            StreamObserver<UpdateEventStatusResponse> responseObserver) {

        try {

            UUID id =
                    UUID.fromString(request.getId());

            StatusUpdateRequest statusUpdateRequest =
                    toJavaStatusUpdateRequest(request);

            EventResponse result =
                    eventService.updateStatus(
                            id,
                            statusUpdateRequest
                    );

            Event grpcEvent =
                    toGrpcEvent(result);

            UpdateEventStatusResponse response =
                    UpdateEventStatusResponse.newBuilder()
                            .setEvent(grpcEvent)
                            .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {

            responseObserver.onError(
                    GrpcExceptionMapper.toGrpcException(e)
            );
        }
    }

    // =====================================================
    // GET EVENT STATS
    // =====================================================

    @Override
    public void getEventStats(
            GetEventStatsRequest request,
            StreamObserver<GetEventStatsResponse> responseObserver) {

        try {

            EventStatsResponse result =
                    eventService.getStats();

            GetEventStatsResponse response =
                    toGrpcStats(result);

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {

            responseObserver.onError(
                    GrpcExceptionMapper.toGrpcException(e)
            );
        }
    }
}