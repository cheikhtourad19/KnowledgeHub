#!/usr/bin/env bash
set -euo pipefail

ENV_FILE="$(dirname "$0")/.env"
CURRENT_IP=$(grep -oE 'http://[0-9]+\.[0-9]+\.[0-9]+\.[0-9]+' "$ENV_FILE" | head -1 | sed 's|http://||')

echo "========================================"
echo "  KnowledgeHub — Environment Configurator"
echo "========================================"
echo ""
echo "Current server IP detected: $CURRENT_IP"
echo ""

# ── Server IP ──────────────────────────────────────────────
read -r -p "Server IP (public/ LAN address for browser access) [$CURRENT_IP]: " SERVER_IP
SERVER_IP="${SERVER_IP:-$CURRENT_IP}"

# ── Frontend URL ───────────────────────────────────────────
FRONTEND_URL="http://$SERVER_IP:80"
read -r -p "Frontend URL (where the browser loads the app) [$FRONTEND_URL]: " FRONTEND_URL_INPUT
FRONTEND_URL="${FRONTEND_URL_INPUT:-$FRONTEND_URL}"

# ── CORS Origins ───────────────────────────────────────────
echo ""
echo "--- CORS Allowed Origins ---"
echo "The browser origin is the URL in the address bar when viewing the app."
echo "Examples: http://$SERVER_IP:80 , http://$SERVER_IP:* , http://my.domain.com"
echo ""

CORS_LIST="http://$SERVER_IP:80"
read -r -p "CORS origin 1 [$CORS_LIST]: " CORS1
CORS1="${CORS1:-$CORS_LIST}"

EXTRA_CORS=""
echo ""
echo "Add more CORS origins? (e.g. http://localhost:4200 for dev)"
echo "Leave empty and press Enter to skip."
echo ""
idx=2
while true; do
  read -r -p "CORS origin $idx (blank to stop): " CORS_NEXT
  [ -z "$CORS_NEXT" ] && break
  EXTRA_CORS="$EXTRA_CORS,$CORS_NEXT"
  idx=$((idx + 1))
done

CORS_ALLOWED="${CORS1}${EXTRA_CORS}"
echo "  → CORS_ALLOWED_ORIGINS = $CORS_ALLOWED"

# ── Confirm ────────────────────────────────────────────────
echo ""
echo "========================================"
echo "  Summary of changes:"
echo "========================================"
echo "  Server IP              : $SERVER_IP"
echo "  Frontend URL           : $FRONTEND_URL"
echo "  CORS origins           : $CORS_ALLOWED"
echo ""
echo "  Vars to be updated in $ENV_FILE:"
echo "    • MINIO_EXTERNAL_ENDPOINT → http://$SERVER_IP:9000"
echo "    • CORS_ALLOWED_ORIGINS   → $CORS_ALLOWED"
echo "    • FRONTEND_API_URL       → http://$SERVER_IP:8080"
echo "    • FRONTEND_CONVENTION_EXTRACTION_API_URL → http://$SERVER_IP:8000"
echo "    • FRONTEND_MINIO_PUBLIC_URL → http://$SERVER_IP:9000"

# Also check if extra origin ports overlap with default ports
if echo ":$CORS_ALLOWED" | grep -qE ':4200'; then
  echo "    (keeping CORS entry for port 4200 — dev mode)"
fi

echo ""
read -r -p "Apply these changes? [Y/n]: " CONFIRM
CONFIRM="${CONFIRM:-Y}"
if [[ ! "$CONFIRM" =~ ^[Yy] ]]; then
  echo "Aborted — no changes made."
  exit 0
fi

# ── Apply ──────────────────────────────────────────────────
echo ""
echo "Applying changes..."

# Escape for sed
ESCAPED_IP=$(printf '%s\n' "$SERVER_IP" | sed 's/[&/\]/\\&/g')
ESCAPED_FRONTEND=$(printf '%s\n' "$FRONTEND_URL" | sed 's/[&/\]/\\&/g')
ESCAPED_CORS=$(printf '%s\n' "$CORS_ALLOWED" | sed 's/[&/\]/\\&/g')

sed -i '' \
  -e "s|^MINIO_EXTERNAL_ENDPOINT=.*|MINIO_EXTERNAL_ENDPOINT=http://${ESCAPED_IP}:9000|" \
  -e "s|^CORS_ALLOWED_ORIGINS=.*|CORS_ALLOWED_ORIGINS=${ESCAPED_CORS}|" \
  -e "s|^FRONTEND_API_URL=.*|FRONTEND_API_URL=http://${ESCAPED_IP}:8080|" \
  -e "s|^FRONTEND_CONVENTION_EXTRACTION_API_URL=.*|FRONTEND_CONVENTION_EXTRACTION_API_URL=http://${ESCAPED_IP}:8000|" \
  -e "s|^FRONTEND_MINIO_PUBLIC_URL=.*|FRONTEND_MINIO_PUBLIC_URL=http://${ESCAPED_IP}:9000|" \
  "$ENV_FILE"

echo "Done — $ENV_FILE updated."
echo ""
echo "Run 'docker compose up' to deploy with the new configuration."
