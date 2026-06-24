# Vault Setup Guide

## Prerequisites

- Docker & Docker Compose
- `curl` (for the init script)

## Quick Start

```bash
# 1. Start Vault dev server
docker compose -f docker-compose.vault.yml up -d

# 2. Seed development secrets
chmod +x scripts/vault-init.sh
./scripts/vault-init.sh
```

Vault is now running at `http://localhost:8200` with token `fingerseal-dev-token`.

## Enable Vault in the application

By default, Vault is **disabled** (`vault.enabled: false`). To enable:

```bash
cd backend
export VAULT_ENABLED=true
export VAULT_URI=http://localhost:8200
export VAULT_TOKEN=fingerseal-dev-token
./gradlew bootRun
```

Or via `application.yml`:

```yaml
vault:
  enabled: true
  uri: http://localhost:8200
  token: fingerseal-dev-token
  mount-path: fingerseal
```

## How credentials are resolved

1. **Vault first** — `ConnectionService.findById()` queries Vault at path `fingerseal/data/connections/{id}`.
2. **Fallback** — If Vault is disabled or the path doesn't exist, uses the local AES-GCM encrypted password from the database.

## Adding a new secret

```bash
curl -H "X-Vault-Token: fingerseal-dev-token" \
  -X POST http://localhost:8200/v1/fingerseal/data/connections/1 \
  -d '{"data":{"host":"prod-db.example.com","port":5432,"database":"prod","username":"admin","password":"s3cret"}}'
```

## Production

- Use AppRole or Kubernetes auth instead of static token
- Unseal Vault (dev mode starts unsealed by default)
- Enable TLS on the Vault server
- Configure proper audit logging
