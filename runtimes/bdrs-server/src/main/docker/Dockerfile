# -buster is required to have apt available
FROM eclipse-temurin:24.0.1_9-jre-alpine

# Optional JVM arguments, such as memory settings
ARG JVM_ARGS=""
ARG JAR
ARG ADDITIONAL_FILES

ARG APP_USER=docker
ARG APP_UID=10100

RUN addgroup --system "$APP_USER"

RUN adduser \
     --shell /sbin/nologin \
     --disabled-password \
     --gecos "" \
     --ingroup "$APP_USER" \
     --no-create-home \
     --uid "$APP_UID" \
     "$APP_USER"

# run "apk add" first, because user "docker" is not allowed to do that

RUN apk --no-cache add curl

USER "$APP_USER"

WORKDIR /app

COPY ${JAR} server.jar
COPY ${ADDITIONAL_FILES} ./

EXPOSE 8080

ENV WEB_HTTP_PORT="8181"
ENV WEB_HTTP_PATH="/"
ENV EDC_PARTICIPANT_ID="BDRS"
ENV EDC_CONNECTOR_NAME="BDRS"

HEALTHCHECK --interval=5s --timeout=5s --retries=10 CMD curl --fail http://localhost:8080/api/check/health

# ARG can not be used in ENTRYPOINT so storing value in an ENV variable
ENV ENV_JVM_ARGS=$JVM_ARGS
ENTRYPOINT ["java", "-Djava.util.logging.config.file=/app/logging.properties", "-Djava.security.egd=file:/dev/urandom", "-jar", "server.jar"]
