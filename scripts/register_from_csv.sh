#!/usr/bin/env bash
# Register members from a CSV: name,password per line (no header).
# Requires admin credentials to login and obtain a token.
# Usage: ./scripts/register_from_csv.sh members.csv

set -euo pipefail
if [ "$#" -ne 1 ]; then
  echo "Usage: $0 members.csv"
  exit 1
fi
CSV="$1"
HOST=${API_HOST:-http://localhost:8080}
read -p "Admin name: " ADMIN_NAME
read -s -p "Admin password: " ADMIN_PASS
echo

# login
TOKEN=$(curl -s -X POST "$HOST/api/auth/login" -H "Content-Type: application/json" -d "{\"name\":\"$ADMIN_NAME\",\"password\":\"$ADMIN_PASS\"}" | jq -r .token)
if [ "$TOKEN" = "null" ] || [ -z "$TOKEN" ]; then
  echo "Login failed"
  exit 2
fi

while IFS=, read -r name pass; do
  name=$(echo "$name" | xargs)
  pass=$(echo "$pass" | xargs)
  if [ -z "$name" ] || [ -z "$pass" ]; then
    echo "Skipping empty line"
    continue
  fi
  echo "Registering $name..."
  curl -s -X POST "$HOST/api/auth/register" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d "{\"name\": \"$name\", \"password\": \"$pass\", \"role\": \"ROLE_MEMBER\"}" \
    | jq .
done < "$CSV"

echo "Done."