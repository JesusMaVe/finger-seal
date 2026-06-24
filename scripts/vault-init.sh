#!/usr/bin/env bash
# ponytail: dev-only. In production, use Vault's seal/unseal workflow.
set -euo pipefail

VAULT_ADDR="http://localhost:8200"
VAULT_TOKEN="fingerseal-dev-token"

# Enable KV v2 secrets engine
curl -s -H "X-Vault-Token: $VAULT_TOKEN" -X POST \
  "$VAULT_ADDR/v1/sys/mounts/fingerseal" \
  -d '{"type": "kv-v2"}' > /dev/null

# Seed one connection credential (for development)
curl -s -H "X-Vault-Token: $VAULT_TOKEN" -X POST \
  "$VAULT_ADDR/v1/fingerseal/data/connections/dev-postgres" \
  -d '{
    "data": {
      "host": "localhost",
      "port": 5432,
      "database": "fingerseal",
      "username": "fingerseal",
      "password": "fingerseal"
    }
  }' > /dev/null

echo "Vault seeded. Token: $VAULT_TOKEN"
