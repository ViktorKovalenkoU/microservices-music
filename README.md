# Microservices Music (resource-service + song-service)

## How to run

1. Start Postgres databases:
   ```bash
   docker compose -f docker-compose.yaml up -d
   ```
   (compose.yaml is included in repo root)

2. Import project in IntelliJ as a Maven project (pom.xml in root).

3. Run modules:
   - resource-service: run ResourceServiceApplication (port 8080)
   - song-service: run SongServiceApplication (port 8081)

4. Use Postman collection `introduction_to_microservices.postman_collection.json` included to run tests.

