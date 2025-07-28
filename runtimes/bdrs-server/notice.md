# Notice for Docker image

Eclipse Tractus-X product(s) installed within the image:

## BDRS

Launch:

> docker run -p 8181:8181 -p 8282:8282 -e EDC_API_AUTH_KEY="1234" server

- Repository: [eclipse-tractusx/bpn-did-resolution-service](https://github.com/eclipse-tractusx/bpn-did-resolution-service)
- Dockerfile: [bdrs-server](https://github.com/eclipse-tractusx/bpn-did-resolution-service/blob/main/runtimes/bdrs-server/src/main/docker/Dockerfile)
- Project license: [Apache License, Version 2.0](https://github.com/eclipse-tractusx/tractusx-edc/blob/main/LICENSE)

## Used base image

- [eclipse-temurin:21.0.2_13-jre-alpine](https://github.com/adoptium/containers)
- Official Eclipse Temurin DockerHub page: <https://hub.docker.com/_/eclipse-temurin>
- Eclipse Temurin Project: <https://projects.eclipse.org/projects/adoptium.temurin>
- Additional information about the Eclipse Temurin
  images: <https://github.com/docker-library/repo-info/tree/master/repos/eclipse-temurin>

## Third-Party Software

- OpenTelemetry Agent v1.32.0: <https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/tag/v1.32.0>

As with all Docker images, these likely also contain other software which may be under other licenses (such as Bash, etc
from the base distribution, along with any direct or indirect dependencies of the primary software being contained).

As for any pre-built image usage, it is the image user's responsibility to ensure that any use of this image complies
with any relevant licenses for all software contained within.
