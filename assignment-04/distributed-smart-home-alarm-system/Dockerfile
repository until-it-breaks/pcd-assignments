FROM sbtscala/scala-sbt:eclipse-temurin-25.0.1_8_1.12.11_3.8.3 AS builder

WORKDIR /app

COPY . .

ENV MAIN_CLASS=it.unibo.SmartHomeClusterNodeApp

RUN sbt assembly

FROM eclipse-temurin:25.0.3_9-jre

WORKDIR /app

COPY --from=builder /app/target/app.jar /app/app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]