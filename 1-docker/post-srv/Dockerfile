FROM openjdk:17-alpine
ARG JAR_FILE=build/libs/post-service-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} post-service.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/post-service.jar"]
