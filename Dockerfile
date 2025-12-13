FROM maven:3.9-eclipse-temurin-21
WORKDIR /app

# Install curl for healthchecks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copy everything
COPY . .

# Build the application
RUN mvn clean package -DskipTests

# Create logs directory for Promtail
RUN mkdir -p /app/logs

# Expose your Spring Boot port
EXPOSE 8080

# Run the application with docker profile to enable Loki logging
CMD ["java", "-jar", "target/OLSHEETS-0.0.1-SNAPSHOT.jar", "--spring.profiles.active=docker,postgres"]
