package no.obje.sse_poc

import org.springframework.http.codec.ServerSentEvent
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class EventRepository {

    private val events = mutableListOf<ServerSentEvent<String>>()

    fun store(event: ServerSentEvent<String>) {
        events.add(event)
    }

    fun findGreaterThan(lastEventId: UUID): List<ServerSentEvent<String>> {
        val indexOfLast = events.indexOfLast { it.id() == lastEventId.toString() }
        if (indexOfLast == -1) {
            throw RuntimeException("No event with id: $lastEventId")
        }
        return events.subList(indexOfLast, events.size)
    }

    fun findAll(): List<ServerSentEvent<String>> {
        return events
    }
}