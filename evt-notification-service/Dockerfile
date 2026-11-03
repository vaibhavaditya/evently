# ---------- BUILD ----------
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY evt-notification-service/pom.xml evt-notification-service/pom.xml
COPY evt-notification-service/src evt-notification-service/src

RUN mvn -f evt-notification-service/pom.xml clean package -DskipTests


# ---------- RUN ----------
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/evt-notification-service/target/*.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]