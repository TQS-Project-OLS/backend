import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Rate, Counter, Trend } from 'k6/metrics';

// Custom metrics
const errorRate = new Rate('errors');
const successfulRequests = new Counter('successful_requests');
const authenticationTime = new Trend('authentication_time');

// Configuration
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const TEST_TYPE = __ENV.TEST_TYPE || 'load'; // smoke, load, spike

// Test user credentials (will be created during setup)
const TEST_USER = {
    username: `k6user_${Date.now()}`,
    password: 'TestPassword123!',
    email: `k6user_${Date.now()}@test.com`,
    name: 'K6 Test User'
};

// Test scenarios based on TEST_TYPE
const scenarios = {
    smoke: {
        stages: [
            { duration: '30s', target: 1 },  // 1 user for 30s
            { duration: '30s', target: 1 },  // Stay at 1 user
        ],
        thresholds: {
            http_req_failed: ['rate<0.25'],      // Error rate < 25% (expected 400/404 for non-existent items)
            http_req_duration: ['p(95)<1000'],   // 95% requests < 1s
            errors: ['rate<0.01'],               // Custom error counter (actual failures only)
        },
    },
    load: {
        stages: [
            { duration: '1m', target: 10 },   // Ramp up to 10 users
            { duration: '3m', target: 10 },   // Stay at 10 users
            { duration: '1m', target: 20 },   // Ramp up to 20 users
            { duration: '2m', target: 20 },   // Stay at 20 users
            { duration: '1m', target: 0 },    // Ramp down
        ],
        thresholds: {
            http_req_failed: ['rate<0.30'],      // Error rate < 30% (expected 400/404 for non-existent items)
            http_req_duration: ['p(95)<500'],    // 95% requests < 500ms
            http_req_duration: ['p(99)<1500'],   // 99% requests < 1.5s
            errors: ['rate<0.05'],               // Custom error counter (actual failures only)
        },
    },
    spike: {
        stages: [
            { duration: '30s', target: 5 },    // Normal load
            { duration: '30s', target: 50 },   // Spike to 50 users
            { duration: '1m', target: 50 },    // Stay at spike
            { duration: '30s', target: 5 },    // Scale down
            { duration: '1m', target: 5 },     // Recovery period
            { duration: '30s', target: 0 },    // Ramp down
        ],
        thresholds: {
            http_req_failed: ['rate<0.35'],      // Error rate < 35% (more tolerant for spike + expected 400/404)
            http_req_duration: ['p(95)<2000'],   // 95% requests < 2s
            errors: ['rate<0.15'],               // Custom error counter (actual failures only)
        },
    },
};

// Select scenario based on TEST_TYPE
export const options = scenarios[TEST_TYPE] || scenarios.load;

// Shared state for authentication token
let authToken = null;
let testUserId = null;

// Setup function - runs once before the test
export function setup() {
    console.log(`Running ${TEST_TYPE.toUpperCase()} test against ${BASE_URL}`);
    
    // Check if application is healthy
    const healthCheck = http.get(`${BASE_URL}/actuator/health`);
    if (healthCheck.status !== 200) {
        console.error('Application health check failed!');
        return { healthy: false };
    }
    
    // Create a test user for authenticated requests
    const signupPayload = JSON.stringify({
        username: TEST_USER.username,
        password: TEST_USER.password,
        email: TEST_USER.email,
        name: TEST_USER.name,
    });
    
    const signupResponse = http.post(`${BASE_URL}/api/auth/signup`, signupPayload, {
        headers: { 'Content-Type': 'application/json' },
    });
    
    if (signupResponse.status === 200) {
        const responseBody = JSON.parse(signupResponse.body);
        console.log(`Test user created: ${TEST_USER.username}`);
        return {
            healthy: true,
            token: responseBody.token,
            username: responseBody.username,
            password: TEST_USER.password,  // Pass the password for later login tests
        };
    } else {
        // User might already exist, try to login
        const loginPayload = JSON.stringify({
            username: TEST_USER.username,
            password: TEST_USER.password,
        });
        
        const loginResponse = http.post(`${BASE_URL}/api/auth/login`, loginPayload, {
            headers: { 'Content-Type': 'application/json' },
        });
        
        if (loginResponse.status === 200) {
            const responseBody = JSON.parse(loginResponse.body);
            console.log(`Logged in as existing user: ${TEST_USER.username}`);
            return {
                healthy: true,
                token: responseBody.token,
                username: responseBody.username,
                password: TEST_USER.password,  // Pass the password for later login tests
            };
        }
        
        console.warn('Could not create or login test user, proceeding with public endpoints only');
        return { healthy: true, token: null, username: null, password: null };
    }
}

// Helper function to get auth headers
function getAuthHeaders(data) {
    const headers = { 'Content-Type': 'application/json' };
    if (data && data.token) {
        headers['Authorization'] = `Bearer ${data.token}`;
    }
    return headers;
}

// Main test function
export default function (data) {
    if (!data.healthy) {
        console.error('Skipping tests - application not healthy');
        return;
    }
    
    const authHeaders = getAuthHeaders(data);
    
    // Group 1: Public endpoints (no auth required)
    group('Public Endpoints', function () {
        // Health check - always public
        const healthRes = http.get(`${BASE_URL}/actuator/health`);
        const healthOk = check(healthRes, {
            'health: status is 200': (r) => r.status === 200,
            'health: status is UP': (r) => {
                try {
                    return JSON.parse(r.body).status === 'UP';
                } catch (e) {
                    return false;
                }
            },
        });
        errorRate.add(!healthOk);
        if (healthOk) successfulRequests.add(1);
        
        sleep(0.3);
    });
    
    sleep(0.5);
    
    // Group 2: API endpoints requiring authentication
    if (data.token) {
        group('Search and Filter Endpoints (Authenticated)', function () {
            // Search sheets (authenticated)
            const searchRes = http.get(`${BASE_URL}/api/sheets/search?name=`, {
                headers: authHeaders,
            });
            const searchOk = check(searchRes, {
                'search sheets: status is 200': (r) => r.status === 200,
                'search sheets: returns array': (r) => {
                    try {
                        return Array.isArray(JSON.parse(r.body));
                    } catch (e) {
                        return false;
                    }
                },
            });
            errorRate.add(!searchOk);
            if (searchOk) successfulRequests.add(1);
            
            sleep(0.3);
            
            // Search sheets with specific term
            const searchTermRes = http.get(`${BASE_URL}/api/sheets/search?name=sonata`, {
                headers: authHeaders,
            });
            const searchTermOk = check(searchTermRes, {
                'search sheets by name: status is 200': (r) => r.status === 200,
            });
            errorRate.add(!searchTermOk);
            if (searchTermOk) successfulRequests.add(1);
            
            sleep(0.3);
            
            // Filter sheets by category
            const categoryRes = http.get(`${BASE_URL}/api/sheets/filter/category?category=CLASSICAL`, {
                headers: authHeaders,
            });
            const categoryOk = check(categoryRes, {
                'filter by category: status is 200': (r) => r.status === 200,
            });
            errorRate.add(!categoryOk);
            if (categoryOk) successfulRequests.add(1);
            
            sleep(0.3);
            
            // Search instruments (authenticated - empty search returns all)
            const instrumentsRes = http.get(`${BASE_URL}/api/instruments/search?name=`, {
                headers: authHeaders,
            });
            const instrumentsOk = check(instrumentsRes, {
                'search instruments: status is 200': (r) => r.status === 200,
                'search instruments: returns array': (r) => {
                    try {
                        return Array.isArray(JSON.parse(r.body));
                    } catch (e) {
                        return false;
                    }
                },
            });
            errorRate.add(!instrumentsOk);
            if (instrumentsOk) successfulRequests.add(1);
            
            sleep(0.3);
            
            // Search instruments with specific term
            const searchInstRes = http.get(`${BASE_URL}/api/instruments/search?name=guitar`, {
                headers: authHeaders,
            });
            const searchInstOk = check(searchInstRes, {
                'search instruments by name: status is 200': (r) => r.status === 200,
            });
            errorRate.add(!searchInstOk);
            if (searchInstOk) successfulRequests.add(1);
            
            sleep(0.3);
            
            // Filter instruments by type (use valid InstrumentType enum value)
            const typeRes = http.get(`${BASE_URL}/api/instruments/filter/type?type=ACOUSTIC`, {
                headers: authHeaders,
            });
            const typeOk = check(typeRes, {
                'filter by type: status is 200': (r) => r.status === 200,
            });
            errorRate.add(!typeOk);
            if (typeOk) successfulRequests.add(1);
            
            sleep(0.3);
            
            // Filter instruments by family
            const familyRes = http.get(`${BASE_URL}/api/instruments/filter/family?family=GUITAR`, {
                headers: authHeaders,
            });
            const familyOk = check(familyRes, {
                'filter by family: status is 200': (r) => r.status === 200,
            });
            errorRate.add(!familyOk);
            if (familyOk) successfulRequests.add(1);
            
            sleep(0.3);
            
            // Get instrument by ID (may return 400 if not found)
            const instrumentByIdRes = http.get(`${BASE_URL}/api/instruments/1`, {
                headers: authHeaders,
            });
            const instrumentByIdOk = check(instrumentByIdRes, {
                'get instrument by id: valid response': (r) => r.status === 200 || r.status === 400,
            });
            errorRate.add(!instrumentByIdOk);
            if (instrumentByIdOk) successfulRequests.add(1);
            
            sleep(0.3);
            
            // Get sheet by ID (may return 400 if not found)
            const sheetByIdRes = http.get(`${BASE_URL}/api/sheets/1`, {
                headers: authHeaders,
            });
            const sheetByIdOk = check(sheetByIdRes, {
                'get sheet by id: valid response': (r) => r.status === 200 || r.status === 400,
            });
            errorRate.add(!sheetByIdOk);
            if (sheetByIdOk) successfulRequests.add(1);
        });
    }
    
    sleep(1);
    
    // Group 2: Authentication endpoints
    group('Authentication', function () {
        // Test login with valid credentials (using setup user)
        if (data.username && data.password) {
            const startTime = new Date();
            const loginPayload = JSON.stringify({
                username: data.username,
                password: data.password,
            });
            
            const loginRes = http.post(`${BASE_URL}/api/auth/login`, loginPayload, {
                headers: { 'Content-Type': 'application/json' },
            });
            
            const loginDuration = new Date() - startTime;
            authenticationTime.add(loginDuration);
            
            const loginOk = check(loginRes, {
                'login: status is 200': (r) => r.status === 200,
                'login: returns token': (r) => {
                    try {
                        return JSON.parse(r.body).token !== undefined;
                    } catch (e) {
                        return false;
                    }
                },
            });
            errorRate.add(!loginOk);
            if (loginOk) successfulRequests.add(1);
            
            sleep(0.5);
        }
        
        // Test token validation
        if (data.token) {
            const validateRes = http.get(`${BASE_URL}/api/auth/validate`, {
                headers: { 'Authorization': `Bearer ${data.token}` },
            });
            
            const validateOk = check(validateRes, {
                'validate token: status is 200': (r) => r.status === 200,
            });
            errorRate.add(!validateOk);
            if (validateOk) successfulRequests.add(1);
        }
        
        sleep(0.5);
        
        // Test login with invalid credentials
        const invalidLoginPayload = JSON.stringify({
            username: 'nonexistent_user',
            password: 'wrongpassword',
        });
        
        const invalidLoginRes = http.post(`${BASE_URL}/api/auth/login`, invalidLoginPayload, {
            headers: { 'Content-Type': 'application/json' },
        });
        
        const invalidLoginOk = check(invalidLoginRes, {
            'invalid login: status is 401': (r) => r.status === 401,
        });
        // Don't count 401 as error - it's expected behavior
        if (invalidLoginOk) successfulRequests.add(1);
    });
    
    sleep(1);
    
    // Group 3: Authenticated endpoints
    if (data.token) {
        group('Authenticated Endpoints', function () {
            // Get current user info
            const meRes = http.get(`${BASE_URL}/api/auth/me`, {
                headers: authHeaders,
            });
            
            const meOk = check(meRes, {
                'me: status is 200': (r) => r.status === 200,
            });
            errorRate.add(!meOk);
            if (meOk) successfulRequests.add(1);
            
            sleep(0.5);
            
            // Get my bookings
            const myBookingsRes = http.get(`${BASE_URL}/api/bookings/my-bookings`, {
                headers: authHeaders,
            });
            
            const myBookingsOk = check(myBookingsRes, {
                'my bookings: status is 200 or 404': (r) => r.status === 200 || r.status === 404,
            });
            errorRate.add(!myBookingsOk);
            if (myBookingsOk) successfulRequests.add(1);
            
            sleep(0.5);
            
            // Get my instruments
            const myInstrumentsRes = http.get(`${BASE_URL}/api/instruments/my-instruments`, {
                headers: authHeaders,
            });
            
            const myInstrumentsOk = check(myInstrumentsRes, {
                'my instruments: status is 200': (r) => r.status === 200,
            });
            errorRate.add(!myInstrumentsOk);
            if (myInstrumentsOk) successfulRequests.add(1);
            
            sleep(0.5);
            
            // Get availabilities (if any instrument exists)
            const availRes = http.get(`${BASE_URL}/api/availability/instrument/1`, {
                headers: authHeaders,
            });
            
            const availOk = check(availRes, {
                'availability: status is 200 or 404': (r) => r.status === 200 || r.status === 404,
            });
            errorRate.add(!availOk);
            if (availOk) successfulRequests.add(1);
        });
    }
    
    sleep(1);
    
    // Group 4: Pricing endpoints (requires auth)
    if (data.token) {
        group('Pricing', function () {
            // Get price for item
            const priceRes = http.get(`${BASE_URL}/api/items/price/1`, {
                headers: authHeaders,
            });
            
            const priceOk = check(priceRes, {
                'price: status is 200 or 404': (r) => r.status === 200 || r.status === 404,
            });
            errorRate.add(!priceOk);
            if (priceOk) successfulRequests.add(1);
        });
    }
    
    sleep(1);
}

// Teardown function - runs once after the test
export function teardown(data) {
    console.log(`\n${TEST_TYPE.toUpperCase()} test completed`);
    console.log(`Test user: ${data.username || 'N/A'}`);
}

// Custom summary handler
export function handleSummary(data) {
    const httpReqDuration = data.metrics.http_req_duration;
    const summary = {
        testType: TEST_TYPE,
        timestamp: new Date().toISOString(),
        metrics: {
            http_reqs: data.metrics.http_reqs ? data.metrics.http_reqs.values.count : 0,
            http_req_duration_avg: httpReqDuration ? httpReqDuration.values.avg : 0,
            http_req_duration_p95: httpReqDuration ? httpReqDuration.values['p(95)'] : 0,
            http_req_duration_p99: httpReqDuration && httpReqDuration.values['p(99)'] ? httpReqDuration.values['p(99)'] : 0,
            http_req_failed: data.metrics.http_req_failed ? data.metrics.http_req_failed.values.rate : 0,
            error_rate: data.metrics.errors ? data.metrics.errors.values.rate : 0,
            successful_requests: data.metrics.successful_requests ? data.metrics.successful_requests.values.count : 0,
            vus_max: data.metrics.vus_max ? data.metrics.vus_max.values.max : 0,
        },
        thresholds: {},
    };
    
    // Add threshold results
    if (data.thresholds) {
        for (const [name, result] of Object.entries(data.thresholds)) {
            summary.thresholds[name] = result.ok;
        }
    }
    
    console.log('\n========== Test Summary ==========');
    console.log(`Test Type: ${TEST_TYPE.toUpperCase()}`);
    console.log(`Total Requests: ${summary.metrics.http_reqs}`);
    console.log(`Avg Response Time: ${summary.metrics.http_req_duration_avg.toFixed(2)}ms`);
    console.log(`P95 Response Time: ${summary.metrics.http_req_duration_p95.toFixed(2)}ms`);
    console.log(`P99 Response Time: ${summary.metrics.http_req_duration_p99.toFixed(2)}ms`);
    console.log(`Error Rate: ${(summary.metrics.error_rate * 100).toFixed(2)}%`);
    console.log(`Failed Requests Rate: ${(summary.metrics.http_req_failed * 100).toFixed(2)}%`);
    console.log(`Max VUs: ${summary.metrics.vus_max}`);
    console.log('==================================\n');
    
    return {
        'stdout': textSummary(data, { indent: '  ', enableColors: true }),
        'results/summary.json': JSON.stringify(summary, null, 2),
    };
}

// Helper function for text summary
function textSummary(data, options) {
    const indent = options.indent || '  ';
    let output = '\n';
    
    output += `${indent}scenarios: (100.00%) 1 scenario, ${data.metrics.vus_max ? data.metrics.vus_max.values.max : 0} max VUs, ${options.enableColors ? '\x1b[36m' : ''}${TEST_TYPE}\x1b[0m\n`;
    output += `${indent}${indent}default: ${getScenarioDescription()}\n\n`;
    
    if (data.metrics.http_reqs) {
        output += `${indent}http_reqs...................: ${data.metrics.http_reqs.values.count}\n`;
    }
    if (data.metrics.http_req_duration) {
        output += `${indent}http_req_duration...........: avg=${data.metrics.http_req_duration.values.avg.toFixed(2)}ms p(95)=${data.metrics.http_req_duration.values['p(95)'].toFixed(2)}ms\n`;
    }
    if (data.metrics.http_req_failed) {
        output += `${indent}http_req_failed.............: ${(data.metrics.http_req_failed.values.rate * 100).toFixed(2)}%\n`;
    }
    if (data.metrics.successful_requests) {
        output += `${indent}successful_requests.........: ${data.metrics.successful_requests.values.count}\n`;
    }
    if (data.metrics.errors) {
        output += `${indent}errors......................: ${(data.metrics.errors.values.rate * 100).toFixed(2)}%\n`;
    }
    
    return output;
}

function getScenarioDescription() {
    const scenario = scenarios[TEST_TYPE];
    if (!scenario || !scenario.stages) return 'unknown';
    
    const stages = scenario.stages;
    let totalDuration = 0;
    
    for (let i = 0; i < stages.length; i++) {
        const duration = stages[i].duration;
        if (typeof duration !== 'string') continue;
        
        // Parse duration string manually
        let value = 0;
        let unit = '';
        for (let j = 0; j < duration.length; j++) {
            const char = duration[j];
            if (char >= '0' && char <= '9') {
                value = value * 10 + parseInt(char);
            } else {
                unit = char;
                break;
            }
        }
        
        if (unit === 's') totalDuration += value;
        else if (unit === 'm') totalDuration += value * 60;
        else if (unit === 'h') totalDuration += value * 3600;
    }
    
    const maxTarget = Math.max(...stages.map(s => s.target));
    
    return `${Math.floor(totalDuration / 60)}m${totalDuration % 60}s duration, up to ${maxTarget} VUs`;
}