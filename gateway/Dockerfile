FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY gateway/pom.xml gateway/
COPY processing-instance/pom.xml processing-instance/
COPY generator/pom.xml generator/
RUN mvn dependency:go-offline -pl gateway -am -q

COPY gateway/src gateway/src
RUN mvn package -pl gateway -am -DskipTests -q

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/gateway/target/gateway-1.0.0.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
