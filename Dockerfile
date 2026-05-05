# eclipse-temurin:21-jre replaces eclipse-temurin:17-jre
FROM eclipse-temurin:21-jre
MAINTAINER CoderNoOne firelight.code@gmail.com

ARG DEPENDENCY=target/dependency
COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app

# --add-opens flags are required for BlockHound and Reactor instrumentation
# to work correctly under Java 21's strong encapsulation
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

# Layer structure (faster rebuilds):
# -------------------------------------------
# CLASSES     <- changes on every build
# -------------------------------------------
# DEPENDENCIES <- cached unless pom.xml changes
# -------------------------------------------
# JRE
