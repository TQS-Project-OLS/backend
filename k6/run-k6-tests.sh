#!/bin/bash

# OLSHEETS K6 Performance Tests
# Usage: ./run-k6-tests.sh [smoke|load|spike] [base_url]

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default values
TEST_TYPE="${1:-load}"
BASE_URL="${2:-http://localhost:8080}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
RESULTS_DIR="${SCRIPT_DIR}/results"

echo -e "${BLUE}==========================================${NC}"
echo -e "${BLUE}   OLSHEETS K6 Performance Tests${NC}"
echo -e "${BLUE}==========================================${NC}"
echo ""

# Validate test type
if [[ ! "$TEST_TYPE" =~ ^(smoke|load|spike)$ ]]; then
    echo -e "${RED}Invalid test type: $TEST_TYPE${NC}"
    echo "Usage: $0 [smoke|load|spike] [base_url]"
    echo ""
    echo "Test types:"
    echo "  smoke  - Quick validation test (1 VU, 1 minute)"
    echo "  load   - Standard load test (up to 20 VUs, 8 minutes)"
    echo "  spike  - Spike test (up to 50 VUs, 4 minutes)"
    exit 1
fi

echo -e "${YELLOW}Test Type:${NC} ${TEST_TYPE^^}"
echo -e "${YELLOW}Base URL:${NC} $BASE_URL"
echo ""

# Check if K6 is installed
if ! command -v k6 &> /dev/null; then
    echo -e "${YELLOW}K6 is not installed. Installing...${NC}"
    
    if [[ "$OSTYPE" == "linux-gnu"* ]]; then
        # Linux installation
        if command -v apt-get &> /dev/null; then
            sudo mkdir -p /etc/apt/keyrings
            curl -sS https://dl.k6.io/key.gpg | sudo gpg --dearmor -o /etc/apt/keyrings/k6-archive-keyring.gpg
            echo "deb [signed-by=/etc/apt/keyrings/k6-archive-keyring.gpg] https://dl.k6.io/deb stable main" | sudo tee /etc/apt/sources.list.d/k6.list
            sudo apt-get update
            sudo apt-get install -y k6
        elif command -v dnf &> /dev/null; then
            sudo dnf install -y https://dl.k6.io/rpm/repo.rpm
            sudo dnf install -y k6
        elif command -v snap &> /dev/null; then
            sudo snap install k6
        else
            echo -e "${RED}Could not find a package manager to install K6${NC}"
            echo "Please install K6 manually: https://k6.io/docs/getting-started/installation"
            exit 1
        fi
    elif [[ "$OSTYPE" == "darwin"* ]]; then
        # macOS installation
        if command -v brew &> /dev/null; then
            brew install k6
        else
            echo -e "${RED}Homebrew not found. Please install K6 manually.${NC}"
            exit 1
        fi
    else
        echo -e "${RED}Unsupported OS: $OSTYPE${NC}"
        echo "Please install K6 manually: https://k6.io/docs/getting-started/installation"
        exit 1
    fi
    
    echo -e "${GREEN}K6 installed successfully!${NC}"
fi

# Display K6 version
echo -e "${BLUE}K6 Version:${NC} $(k6 version)"
echo ""

# Check if the application is running
echo -e "${YELLOW}Checking application health...${NC}"
HEALTH_CHECK=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/actuator/health" 2>/dev/null || echo "000")

if [[ "$HEALTH_CHECK" != "200" ]]; then
    echo -e "${RED}Application is not running or not healthy!${NC}"
    echo "Health check returned: $HEALTH_CHECK"
    echo ""
    echo "Please ensure the application is running:"
    echo "  cd $(dirname "$SCRIPT_DIR")"
    echo "  mvn spring-boot:run"
    echo ""
    echo "Or with Docker:"
    echo "  docker compose up -d"
    exit 1
fi

echo -e "${GREEN}Application is healthy!${NC}"
echo ""

# Create results directory
mkdir -p "$RESULTS_DIR"

# Generate timestamp for results
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
RESULT_PREFIX="${TEST_TYPE}_${TIMESTAMP}"

echo -e "${BLUE}Starting ${TEST_TYPE^^} test...${NC}"
echo -e "${YELLOW}Results will be saved to: ${RESULTS_DIR}/${RESULT_PREFIX}_*${NC}"
echo ""

# Run the K6 test
k6 run \
    --env TEST_TYPE="$TEST_TYPE" \
    --env BASE_URL="$BASE_URL" \
    --out json="${RESULTS_DIR}/${RESULT_PREFIX}_report.json" \
    "${SCRIPT_DIR}/load-test.js" 2>&1 | tee "${RESULTS_DIR}/${RESULT_PREFIX}_output.log"

EXIT_CODE=${PIPESTATUS[0]}

echo ""
echo -e "${BLUE}==========================================${NC}"

if [[ $EXIT_CODE -eq 0 ]]; then
    echo -e "${GREEN}✓ ${TEST_TYPE^^} test completed successfully!${NC}"
else
    echo -e "${RED}✗ ${TEST_TYPE^^} test failed with exit code: $EXIT_CODE${NC}"
fi

echo -e "${BLUE}==========================================${NC}"
echo ""

# Display results location
echo -e "${YELLOW}Results saved to:${NC}"
echo "  - JSON Report: ${RESULTS_DIR}/${RESULT_PREFIX}_report.json"
echo "  - Output Log: ${RESULTS_DIR}/${RESULT_PREFIX}_output.log"
if [[ -f "${RESULTS_DIR}/summary.json" ]]; then
    echo "  - Summary: ${RESULTS_DIR}/summary.json"
fi
echo ""

# If Grafana is available, show link
if curl -s -o /dev/null -w "%{http_code}" "http://localhost:3000" 2>/dev/null | grep -q "200"; then
    echo -e "${BLUE}Grafana dashboard available at:${NC} http://localhost:3000"
    echo ""
fi

exit $EXIT_CODE
echo "4. Database Connections - Should show increased activity"
echo "5. Logs in Loki - Should show increased log volume"