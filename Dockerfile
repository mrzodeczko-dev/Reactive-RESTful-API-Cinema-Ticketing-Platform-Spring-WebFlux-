FROM eclipse-temurin:25-jre
MAINTAINER CoderNoOne firelight.code@gmail.com

ARG DEPENDENCY=target/dependency
COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app


ENTRYPOINT ["java", \
  "-cp", "app:app/lib/*", \
  "-Xdebug", \
  "-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=*:5005", \
  "--add-opens", "java.base/java.lang=ALL-UNNAMED", \
  "--add-opens", "java.base/java.util=ALL-UNNAMED", \
  "--add-opens", "java.base/java.lang.reflect=ALL-UNNAMED", \
  "-Dspring.profiles.active=docker", \
  "-Djava.net.preferIPv4Stack=true", \
  "com.app.CinemaApplication"]
