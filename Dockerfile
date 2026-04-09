FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml /workspace/pom.xml
RUN mvn -q -DskipTests package || true

COPY src /workspace/src

RUN mvn -q -DskipTests package

RUN rm -f /workspace/target/*.jar.original

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/target/*-exec.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-jar", "/app/app.jar"]
