import http from "k6/http";
import { check, sleep } from "k6";

export const options = {
  vus: 2000,
  duration: "30s",
};

const BASE_URL = "http://localhost:8080";

export function setup() {
  const res = http.post(
    `${BASE_URL}/auth-service/api/public/login`,
    JSON.stringify({
      email: "admin@esprim.tn",
      password: "messI987654321",
    }),
    { headers: { "Content-Type": "application/json" } },
  );

  return { token: res.json("token") };
}

export default function (data) {
  const params = {
    headers: {
      Authorization: `Bearer ${data.token}`,
    },
  };

  const res = http.get(`${BASE_URL}/user-service/api/students/all`, params);

  check(res, {
    "status is 200": (r) => r.status === 200,
  });

  sleep(1);
}
// k6 run stress.js
