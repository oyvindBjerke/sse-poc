package no.obje.sse_poc

import org.springframework.http.codec.ServerSentEvent
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks
import reactor.core.publisher.Sinks.EmitResult.FAIL_NON_SERIALIZED

@Service
class DistributedCacheService {

    private val sink = Sinks.many().multicast().onBackpressureBuffer<ServerSentEvent<String>>()

    // This is to simulate a redis cache where an update is distributed to subscribers
    fun addToCache(event: ServerSentEvent<String>) {
        sink.emitNext(event) { _, result -> result == FAIL_NON_SERIALIZED }
    }

    fun listenToChannel(): Flux<ServerSentEvent<String>> {
        return sink.asFlux()
    }
}