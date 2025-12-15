#!/bin/bash

echo "Starting OLSHEETS load tests with K6..."

# Check if K6 is installed
if ! command -v k6 &> /dev/null; then
    echo "K6 is not installed. Installing..."
    # Install K6 based on your OS
    if [[ "$OSTYPE" == "linux-gnu"* ]]; then
        # Linux
        sudo gpg -k /usr/share/keyrings/k6-archive-keyring.gpg --no-default-keyring --keyring /usr/share/keyrings/k6-archive-keyring.gpg --import
        echo "deb [signed-by=/usr/share/keyrings/k6-archive-keyring.gpg] https://dl.k6.io/deb stable main" | sudo tee /etc/apt/sources.list.d/k6.list
        sudo apt-get update
        sudo apt-get install k6
    elif [[ "$OSTYPE" == "darwin"* ]]; then
        # macOS
        brew install k6
    else
        echo "Unsupported OS. Please install K6 manually from https://k6.io/docs/getting-started/installation"
        exit 1
    fi
fi

# Check if the application is running
if ! curl -s http://localhost:8080/actuator/health | grep -q "UP"; then
    echo "Application is not running. Please start the observability stack first with 'docker compose up -d'"
    exit 1
fi

echo "Running K6 load test against http://localhost:8080"
echo "This will generate traffic that should be captured by Prometheus and Loki..."
echo ""

# Create results directory if it doesn't exist
mkdir -p results

# Run the K6 test
k6 run \
  --out json=results/k6-report.json \
  --out csv=results/k6-report.csv \
  k6/load-test.js

echo ""
echo "Load test completed!"
echo "Check Grafana at http://localhost:3000 to see the metrics and logs from the test"
echo ""
echo "Key metrics to check in Grafana:"
echo "1. HTTP Request Rate - Should show a spike during the test"
echo "2. Response Time - Should show increased percentiles"
echo "3. JVM Memory/Threads - Should show increased usage during load"
echo "4. Database Connections - Should show increased activity"
echo "5. Logs in Loki - Should show increased log volume"