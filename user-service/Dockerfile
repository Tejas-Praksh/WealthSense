FROM maven:3.9.11-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY common-library/pom.xml common-library/
COPY security-lib/pom.xml security-lib/
COPY api-gateway/pom.xml api-gateway/
COPY user-service/pom.xml user-service/
COPY transaction-service/pom.xml transaction-service/
COPY fraud-detection-service/pom.xml fraud-detection-service/
COPY decision-engine-service/pom.xml decision-engine-service/
COPY notification-service/pom.xml notification-service/
COPY ai-advisor-service/pom.xml ai-advisor-service/
COPY investment-service/pom.xml investment-service/
RUN mkdir -p api-gateway/src/main/java \
    transaction-service/src/main/java \
    fraud-detection-service/src/main/java \
    decision-engine-service/src/main/java \
    notification-service/src/main/java \
    ai-advisor-service/src/main/java \
    investment-service/src/main/java
COPY common-library/src common-library/src
COPY security-lib/src security-lib/src
COPY user-service/src user-service/src
ENV MAVEN_OPTS="-Xmx256m -XX:MaxMetaspaceSize=128m"
RUN mvn clean package -pl user-service -am -DskipTests --no-transfer-progress
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY --from=builder /app/user-service/target/*.jar app.jar
RUN chown appuser:appgroup app.jar
USER appuser
EXPOSE 8081
ENTRYPOINT ["java","-XX:+UseContainerSupport","-Xmx256m","-Xms256m","-XX:MaxMetaspaceSize=128m","-Xss512k","-jar","app.jar"]
