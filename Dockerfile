# syntax=docker/dockerfile:1
FROM --platform=linux/x86_64 eclipse-temurin:17-alpine@sha256:06e31b7e02c379a8a8a91241497d2860859a42e10c72fe20b52fee8e67fd5df3
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:resolve
COPY src ./src
CMD ["./mvnw", "spring-boot:run"]
EXPOSE 8083

