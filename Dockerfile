FROM --platform=linux/x86_64 eclipse-temurin:17-alpine@sha256:06e31b7e02c379a8a8a91241497d2860859a42e10c72fe20b52fee8e67fd5df3
VOLUME /tmp
ARG JAR_FILE
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
EXPOSE 8083
