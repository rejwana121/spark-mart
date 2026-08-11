# Running Spark Mart with Docker (Chapter 7.2)

Two ways to containerize the app — both are set up in this project:

## Option A — Cloud Native Buildpacks (no Dockerfile needed)

```bash
./mvnw spring-boot:build-image
docker run -p 8081:8081 --env DB_HOST=host.docker.internal sparkmart/spark_mart:0.0.1-SNAPSHOT
```

## Option B — Plain Dockerfile + Docker Compose (app + MySQL together)

```bash
docker compose up --build
```

This starts MySQL and the app together, wires the app's `DB_HOST`/`DB_PORT`/etc.
environment variables to point at the `mysql` service, and waits for MySQL's
healthcheck before starting the app. Visit `http://localhost:8081` once both
containers are up.

## GraalVM Native Images (Chapter 7.2.2)

`spring-boot-starter-parent` already ships the Maven `native` profile, so
`./mvnw -Pnative native:compile` is available out of the box. This was **not**
run/verified as part of this change (it needs the GraalVM `native-image` tool
installed locally) — noted here as a known next step rather than claimed as done.
