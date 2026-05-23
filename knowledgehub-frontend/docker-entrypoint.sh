#!/bin/sh

cat > /usr/share/nginx/html/assets/env.js << EOF
(function(window) {
  window.__env = window.__env || {};
  window.__env.apiUrl = '${API_URL:-http://localhost:8080}';
  window.__env.conventionExtractionApiUrl = '${CONVENTION_EXTRACTION_API_URL:-http://localhost:8000}';
  window.__env.minioPublicUrl = '${MINIO_PUBLIC_URL:-http://localhost:9000}';
})(this);
EOF

exec nginx -g "daemon off;"
