FROM maven:3.9.11-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Copy parent pom
COPY pom.xml .

# Copy ALL module pom files (parent needs them)
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

# Create empty src dirs for modules we dont need
RUN mkdir -p api-gateway/src/main/java \
    transaction-service/src/main/java \
    fraud-detection-service/src/main/java \
    decision-engine-service/src/main/java \
    notification-service/src/main/java \
    ai-advisor-service/src/main/java \
    investment-service/src/main/java

# Copy source for modules we need
COPY common-library/src common-library/src
COPY security-lib/src security-lib/src
COPY user-service/src user-service/src

# Build only user-service
RUN mvn clean package -pl user-service -am -DskipTests -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S wealthsense && \
    adduser -S wealthsense -G wealthsense
COPY --from=builder /app/user-service/target/*.jar app.jar
RUN chown wealthsense:wealthsense app.jar
USER wealthsense
EXPOSE 8081
ENTRYPOINT ["java","-XX:+UseContainerSupport","-XX:MaxRAMPercentage=75.0","-jar","app.jar"]
