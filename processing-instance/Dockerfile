FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY gateway/pom.xml gateway/
COPY processing-instance/pom.xml processing-instance/
COPY generator/pom.xml generator/
RUN mvn dependency:go-offline -pl processing-instance -am -q
COPY processing-instance/src processing-instance/src
RUN mvn package -pl processing-instance -am -DskipTests -q

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/processing-instance/target/processing-instance-1.0.0.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
