FROM maven:3.9.11-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY common-library/pom.xml common-library/
COPY security-lib/pom.xml security-lib/
COPY user-service/pom.xml user-service/
RUN mvn dependency:go-offline -pl user-service -am -q
COPY common-library/src common-library/src
COPY security-lib/src security-lib/src
COPY user-service/src user-service/src
RUN mvn clean package -pl user-service -am -DskipTests -q
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S wealthsense && adduser -S wealthsense -G wealthsense
COPY --from=builder /app/user-service/target/*.jar app.jar
RUN chown wealthsense:wealthsense app.jar
USER wealthsense
EXPOSE 8081
ENTRYPOINT ["java","-XX:+UseContainerSupport","-XX:MaxRAMPercentage=75.0","-jar","app.jar"]
