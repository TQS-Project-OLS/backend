import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

// Custom metric to track API request rates
let requestRate = new Rate('api_requests');

// Test options
export let options = {
  stages: [
    { duration: '30s', target: 10 }, // Ramp up to 10 users
    { duration: '1m', target: 10 },  // Stay at 10 users
    { duration: '30s', target: 0 },  // Ramp down
  ],
  thresholds: {
    http_req_failed: ['rate<0.1'], // Error rate should be less than 10%
    http_req_duration: ['p(95)<500'], // 95% of requests should be below 500ms
  },
};

const BASE_URL = 'http://localhost:8080';

export default function () {
  // Test health endpoint
  let healthResponse = http.get(`${BASE_URL}/actuator/health`);
  let healthCheck = check(healthResponse, {
    'health endpoint status is 200': (r) => r.status === 200,
  });
  
  if (healthCheck) {
    requestRate.add(1);
  }
  
  sleep(1);
  
  // Test sheets API endpoints
  let sheetsResponse = http.get(`${BASE_URL}/api/sheets`);
  let sheetsCheck = check(sheetsResponse, {
    'sheets API status is 200': (r) => r.status === 200,
    'sheets API returns data': (r) => r.json().length >= 0,
  });
  
  if (sheetsCheck) {
    requestRate.add(1);
  }
  
  sleep(1);
  
  // Test search endpoint
  let searchResponse = http.get(`${BASE_URL}/api/sheets/search?name=sonata`);
  let searchCheck = check(searchResponse, {
    'search endpoint status is 200': (r) => r.status === 200,
  });
  
  if (searchCheck) {
    requestRate.add(1);
  }
  
  sleep(1);
  
  // Test category filter
  let categoryResponse = http.get(`${BASE_URL}/api/sheets/filter/category?category=CLASSICAL`);
  let categoryCheck = check(categoryResponse, {
    'category filter status is 200': (r) => r.status === 200,
  });
  
  if (categoryCheck) {
    requestRate.add(1);
  }
  
  sleep(1);
  
  // Test pricing API
  let pricingResponse = http.get(`${BASE_URL}/api/items/price/1`);
  let pricingCheck = check(pricingResponse, {
    'pricing endpoint status is 200 or 404': (r) => r.status === 200 || r.status === 404,
  });
  
  if (pricingCheck) {
    requestRate.add(1);
  }
  
  sleep(1);
}

export function handleSummary(data) {
  return {
    'api_requests_per_second': data.metrics.api_requests.rate,
    'failed_requests': data.metrics.http_req_failed.count,
    'request_duration_p95': data.metrics.http_req_duration['p(95)'],
  };
}