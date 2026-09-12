# ---------- BUILD ----------
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy gRPC contracts
COPY evt-grpc-contracts/pom.xml evt-grpc-contracts/pom.xml
COPY evt-grpc-contracts/src evt-grpc-contracts/src

# Install gRPC contracts
RUN mvn -f evt-grpc-contracts/pom.xml clean install -DskipTests

# Copy Open Service
COPY evt-open-service/pom.xml evt-open-service/pom.xml
COPY evt-open-service/src evt-open-service/src

# Build Open Service
RUN mvn -f evt-open-service/pom.xml clean package -DskipTests


# ---------- RUN ----------
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/evt-open-service/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]