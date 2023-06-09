FROM openjdk:17-alpine
ARG JAR_FILE=build/libs/user-service-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} user-service.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/user-service.jar"]
