FROM openjdk:15 AS build-env

ADD . /app
WORKDIR /app
RUN ./gradlew build

FROM openjdk:15
WORKDIR /
COPY --from=build-env /app/build/libs/ws-web-1.0-SNAPSHOT.jar /app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]