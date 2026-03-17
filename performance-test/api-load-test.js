import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';

// Custom metrics
const myTrend = new Trend('waiting_time');

// Test configuration
export const options = {
  stages: [
    { duration: '2m', target: 20 }, // ramp up to 20 users
    { duration: '5m', target: 20 }, // stay at 20 users for 5m
    { duration: '2m', target: 0 },  // ramp down
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'], // 95% of requests must be below 500ms
    'waiting_time': ['p(95)<400'],    // custom metric threshold
  },
};

export default function () {
  const baseUrl = __ENV.BASE_URL || 'http://localhost:8080';

  // Array of endpoints to test
  const endpoints = [
    { path: '/api/exhibitions', method: 'GET' },
    { path: '/api/exhibitions/popular', method: 'GET' },
    { path: '/api/records', method: 'GET' },
    { path: '/api/bookmarks/my', method: 'GET', needsAuth: true },
    { path: '/api/likes/my', method: 'GET', needsAuth: true },
  ];

  // For simplicity, we'll use a fixed token if needed.
  // In a real test, you might want to generate or fetch tokens dynamically.
  const token = __ENV.ACCESS_TOKEN || 'dummy-token-for-test';

  endpoints.forEach((endpoint) => {
    let params = {
      headers: {
        'Content-Type': 'application/json',
      },
    };

    if (endpoint.needsAuth) {
      params.headers['Authorization'] = `Bearer ${token}`;
    }

    const res = http.request(endpoint.method, `${baseUrl}${endpoint.path}`, null, params);
    const checkRes = check(res, {
      'status is 200': (r) => r.status === 200,
      'response time < 500ms': (r) => r.timings.duration < 500,
    });

    // Record custom metric
    myTrend.add(res.timings.waiting);
    sleep(1);
  });
}