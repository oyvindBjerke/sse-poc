# Proof of concept - Server sent events

Demonstrerer hvordan Spring Boot med Webflux kan brukes til å lage en [SSE](https://html.spec.whatwg.org/multipage/server-sent-events.html) applikasjon.

Features:
* Streaming av events
* Replay av events fra et kjent punkt ([Last-Event-Id](https://html.spec.whatwg.org/multipage/server-sent-events.html#the-last-event-id-header))

Koden er lagt opp til å bruke en database for persistent lagring og redis som distribuert cache.

Tanken er at nye events oppstår ved f.eks. konsumering av en kafka topic, men for denne POC-en så er det opprettet et POST endepunkt som kan brukes til å opprette events.

## Testing

Det er lagt til noen filer i `/http` mappen som kan brukes til testing.