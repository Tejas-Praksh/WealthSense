FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S wealthsense && \
    adduser -S wealthsense -G wealthsense

COPY target/*.jar app.jar
RUN chown wealthsense:wealthsense app.jar

USER wealthsense
EXPOSE 8081

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget -q -O- http://localhost:8081/actuator/health || exit 1

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
