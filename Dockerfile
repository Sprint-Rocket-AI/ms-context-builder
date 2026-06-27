# ---------- BUILD ----------
FROM eclipse-temurin:25-jdk AS builder

WORKDIR /build

COPY .mvn/ .mvn/
COPY mvnw .
RUN chmod +x mvnw

COPY pom.xml .
RUN ./mvnw -B -q -e -DskipTests dependency:go-offline

COPY src ./src
RUN ./mvnw -B -DskipTests package

# ---------- RUNTIME ----------
FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=builder /build/target/*.jar app.jar

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --server.port=$PORT"]