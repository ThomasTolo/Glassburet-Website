#!/usr/bin/env bash
# Register members from a CSV: one username per line (no header, no passwords).
# Generates a random password for each user and prints name,password to stdout.
# Usage: ./scripts/register_from_csv.sh members.csv
#
# Example members.csv:
#   Alice
#   Bob

set -euo pipefail
if [ "$#" -ne 1 ]; then
  echo "Usage: $0 members.csv"
  exit 1
fi
CSV="$1"
HOST=${API_HOST:-http://localhost:8080}
read -rp "Admin name: " ADMIN_NAME
read -rs -p "Admin password: " ADMIN_PASS
echo

TOKEN=$(curl -s -X POST "$HOST/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"$ADMIN_NAME\",\"password\":\"$ADMIN_PASS\"}" | jq -r .token)
if [ "$TOKEN" = "null" ] || [ -z "$TOKEN" ]; then
  echo "Login failed" >&2
  exit 2
fi

echo "name,password"
while IFS= read -r name; do
  name=$(echo "$name" | xargs)
  [ -z "$name" ] && continue

  pass=$(openssl rand -base64 12 | tr -dc 'A-Za-z0-9' | head -c 14)
  pass="${pass}$(shuf -i 0-9 -n 1)"

  status=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$HOST/api/auth/register" \
    -H "Content-Type: application/json" \
    -d "{\"name\":\"$name\",\"password\":\"$pass\"}")

  if [ "$status" = "200" ]; then
    echo "$name,$pass"
  else
    echo "FAILED: $name (HTTP $status)" >&2
  fi
done < "$CSV"
