package no.obje.sse_poc

import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux

@RestController
@RequestMapping("/events")
class EventController(private val eventService: EventService) {

    @GetMapping(produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun getEvents(
        @RequestHeader(
            name = "Last-Event-Id",
            required = false
        ) lastEventId: String?
    ): Flux<ServerSentEvent<String>> {
        return eventService.streamFrom(lastEventId)
    }

    @PostMapping
    fun postEvent(@RequestBody dto: EventDto) {
        eventService.store(dto.event, dto.data)
    }

    data class EventDto(val event: String, val data: String)
}