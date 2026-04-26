import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  scenarios: {
    notify_burst: {
      executor: 'ramping-vus',
      stages: [
        { duration: '30s', target: 10 },
        { duration: '1m', target: 20 },
        { duration: '30s', target: 0 },
      ],
    },
  },
  thresholds: {
    http_req_duration: ['p(95)<1500'],
    http_req_failed: ['rate<0.20'],
  },
};

export default function () {
  const baseUrl = __ENV.BASE_URL || 'http://localhost:8080';
  const token = __ENV.ADMIN_ACCESS_TOKEN || __ENV.ACCESS_TOKEN;
  if (!token) {
    throw new Error('ADMIN_ACCESS_TOKEN (or ACCESS_TOKEN) is required');
  }

  const payload = JSON.stringify({
    title: `[k6] burst-${Date.now()}`,
    body: 'k6 notification burst test payload',
    pushEnabled: true,
    imageUrls: [],
  });

  const res = http.post(`${baseUrl}/api/admin/announcements`, payload, {
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    },
  });

  check(res, {
    'announcement status is 200': (r) => r.status === 200,
  });

  sleep(0.3);
}
