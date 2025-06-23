FROM docker.io/eclipse-temurin:21-jre

LABEL authors="sirhpitar"

COPY target/budget-0.0.1-SNAPSHOT.jar /budget.jar

ENTRYPOINT ["java", "-jar", "/budget.jar"]