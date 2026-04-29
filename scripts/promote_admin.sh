#!/usr/bin/env bash
# Promote or demote a member's role. Requires an existing admin account.
# Usage: ./scripts/promote_admin.sh

set -euo pipefail

HOST=${API_HOST:-http://localhost:8080}

read -p "Admin name: " ADMIN_NAME
read -s -p "Admin password: " ADMIN_PASS
echo

TOKEN=$(curl -s -X POST "$HOST/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"$ADMIN_NAME\",\"password\":\"$ADMIN_PASS\"}" | jq -r .token)

if [ "$TOKEN" = "null" ] || [ -z "$TOKEN" ]; then
  echo "Login failed"
  exit 1
fi

read -p "Member name to update: " TARGET
read -p "New role (ROLE_ADMIN / ROLE_MEMBER): " ROLE

curl -s -X PUT "$HOST/api/auth/members/$TARGET/role" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{\"role\": \"$ROLE\"}" | jq .
