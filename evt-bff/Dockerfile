# ---------- BUILD ----------
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY evt-bff/pom.xml evt-bff/pom.xml
COPY evt-bff/src evt-bff/src

RUN mvn -f evt-bff/pom.xml clean package -DskipTests


# ---------- RUN ----------
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/evt-bff/target/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]