package no.obje.sse_poc

import org.slf4j.LoggerFactory
import org.springframework.http.codec.ServerSentEvent
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks
import reactor.core.publisher.Sinks.EmitResult.FAIL_NON_SERIALIZED
import java.util.*

@Service
class EventService(
    private val eventRepository: EventRepository,
    private val distributedCacheService: DistributedCacheService
) {

    private val logger = LoggerFactory.getLogger(EventService::class.java)

    private val sink = Sinks.many().multicast().onBackpressureBuffer<ServerSentEvent<String>>()

    init {
        distributedCacheService.listenToChannel().subscribe {
            // Retry on FAIL_NON_SERIALIZED to facilitate concurrency
            sink.emitNext(it) { _, result -> result == FAIL_NON_SERIALIZED }
        }
    }

    fun streamFrom(lastEventId: String?): Flux<ServerSentEvent<String>> {
        val replayEvents: List<ServerSentEvent<String>> = if (lastEventId != null) {
            eventRepository.findGreaterThan(UUID.fromString(lastEventId))
        } else {
            eventRepository.findAll()
        }
        if (replayEvents.isNotEmpty()) {
            logger.info("Found ${replayEvents.size} events to replay")
        }
        val replayFlux: Flux<ServerSentEvent<String>> = Flux.fromIterable(replayEvents)
        val liveFlux: Flux<ServerSentEvent<String>> = sink.asFlux()
        return Flux.concat(replayFlux, liveFlux)
    }


    fun store(event: String, data: String) {
        val id = UUID.randomUUID().toString()
        val storedEvent = ServerSentEvent.builder<String>()
            .id(id)
            .event(event)
            .data(data)
            .build()
        // Every new event has to be stored in order to facilitate replayability
        // For simplicity we're using an in-memory implementation, but in a production system this would be persistent storage
        // In a production system this operation should also be transactional
        eventRepository.store(storedEvent)
        // Every new event has to be notified to other instances to facilitate streaming
        // For simplicity we're using an im-memory implementation, but in production redis would typically be used
        // Also, in a production system this could be done in a separate process/application
        distributedCacheService.addToCache(storedEvent)
        logger.info("Event with ID: $id was stored and added to cache")
    }
}