FROM eclipse-temurin:17-jre-alpine AS runtime
WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY target/analytics-service-1.0.0.jar app.jar
RUN chown appuser:appgroup app.jar
USER appuser
EXPOSE 8084
HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD wget -qO- http://localhost:8084/api/v1/analytics/health || exit 1
ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-jar", "app.jar"]
