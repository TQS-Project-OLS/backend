FROM maven:3.9-eclipse-temurin-21
WORKDIR /app

# Install curl for healthchecks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copy everything
COPY . .

# Expose your Spring Boot port
EXPOSE 8080

# Run Spring Boot using Maven
CMD ["mvn", "spring-boot:run", "-Dspring-boot.run.profiles=postgres"]
