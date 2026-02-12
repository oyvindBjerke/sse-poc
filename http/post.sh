seq 1000 | xargs -n1 -P10 -I{} curl -s -X POST http://localhost:8080/events \
  -H "Content-Type: application/json" \
  -d "{\"event\":\"my-event\",\"data\":\"Some data {}\"}"