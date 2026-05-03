# eclipse-temurin:25-jre — Java 25 LTS base image
FROM eclipse-temurin:25-jre
MAINTAINER CoderNoOne firelight.code@gmail.com

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", \
  "-jar", "/app.jar", \
  "-Dspring.profiles.active=docker"]
