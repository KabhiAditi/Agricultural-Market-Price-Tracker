#!/usr/bin/env bash
# ==============================================================================
# Agricultural Market Price Tracker (AMPT) - Service Health Check Script
# ==============================================================================
# Usage:
#   ./scripts/health-check.sh [HEALTH_URL] [MAX_RETRIES] [RETRY_DELAY]
# Example:
#   ./scripts/health-check.sh http://localhost:8080/actuator/health 15 2
# ==============================================================================

set -eo pipefail

HEALTH_URL="${1:-${AMPT_HEALTH_URL:-http://127.0.0.1:8080/actuator/health}}"
MAX_RETRIES="${2:-${AMPT_HEALTH_RETRIES:-15}}"
RETRY_DELAY="${3:-${AMPT_HEALTH_DELAY:-2}}"

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}======================================================${NC}"
echo -e "${BLUE}  AMPT Health Check Monitor${NC}"
echo -e "${BLUE}  Target:  ${HEALTH_URL}${NC}"
echo -e "${BLUE}  Retries: ${MAX_RETRIES} (Delay: ${RETRY_DELAY}s)${NC}"
echo -e "${BLUE}======================================================${NC}"

attempt=1
while [ "$attempt" -le "$MAX_RETRIES" ]; do
    echo -n "Attempt ${attempt}/${MAX_RETRIES}: Checking endpoint... "
    
    # Perform HTTP request capturing body and status code
    response=$(curl -s -w "\n%{http_code}" --connect-timeout 5 --max-time 10 "$HEALTH_URL" 2>/dev/null || echo -e "\n000")
    
    # Separate body and HTTP status code
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" -eq 200 ] && echo "$body" | grep -qi '"status"[[:space:]]*:[[:space:]]*"UP"'; then
        echo -e "${GREEN}[UP] HTTP ${http_code}${NC}"
        echo -e "${GREEN}Response Payload: ${body}${NC}"
        echo -e "${GREEN}>>> Health Check PASSED: AMPT Application is healthy!${NC}"
        exit 0
    else
        echo -e "${YELLOW}[DOWN/WAITING] HTTP ${http_code} (attempt ${attempt}/${MAX_RETRIES})${NC}"
    fi
    
    attempt=$((attempt + 1))
    if [ "$attempt" -le "$MAX_RETRIES" ]; then
        sleep "$RETRY_DELAY"
    fi
done

echo -e "\n${RED}======================================================${NC}"
echo -e "${RED}>>> Health Check FAILED after ${MAX_RETRIES} attempts.${NC}"
echo -e "${RED}Last HTTP Status Code: ${http_code}${NC}"
echo -e "${RED}Last Response Body:    ${body}${NC}"
echo -e "${RED}======================================================${NC}"
exit 1
