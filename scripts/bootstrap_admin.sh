#!/usr/bin/env bash
# Create the first admin user. Run this once when the DB is empty.
# Usage: ./scripts/bootstrap_admin.sh

set -euo pipefail
read -p "Admin name: " NAME
read -s -p "Password for ${NAME}: " PASSWORD
echo

HOST=${API_HOST:-http://localhost:8080}

curl -s -X POST "$HOST/api/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"name\": \"$NAME\", \"password\": \"$PASSWORD\", \"role\": \"ROLE_ADMIN\"}" \
  | jq .

echo "Done. Keep the admin credentials secure."