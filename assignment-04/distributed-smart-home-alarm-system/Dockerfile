FROM sbtscala/scala-sbt:eclipse-temurin-25.0.1_8_1.12.11_3.8.3 AS builder

WORKDIR /app

COPY . .

ENV MAIN_CLASS=it.unibo.SmartHomeClusterNodeApp

RUN sbt assembly

FROM eclipse-temurin:25.0.3_9-jre

WORKDIR /app

COPY --from=builder /app/target/cluster.jar /app/cluster.jar

ENTRYPOINT ["java", "-jar", "cluster.jar"]