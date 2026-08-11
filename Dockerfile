# ---- Build stage ----
# Compiles the app inside a container, so the host machine doesn't need
# Maven or a JDK installed at all.
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -q -B dependency:go-offline
COPY src ./src
RUN ./mvnw -q -B clean package -DskipTests

# ---- Run stage ----
# A slim JRE-only image; only the built jar is copied in, not the whole
# Maven project or the JDK used to compile it.
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/spark_mart-*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
