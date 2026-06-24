# Observability Stack Setup

## Quick Start

```bash
docker compose -f docker-compose.observability.yml up -d
```

Wait 10–15 seconds for all services to initialize.

## Services

| Service | URL | Purpose |
|---|---|---|
| **Grafana** | http://localhost:3001 | Dashboards (admin/admin) |
| **Prometheus** | http://localhost:9090 | Metrics store |
| **Loki** | http://localhost:3100 | Log aggregation |
| **Tempo** | http://localhost:3200 | Distributed tracing (OTLP gRPC :4317, HTTP :4318) |

## Dashboards

A pre-configured dashboard **"Finger Seal — Query Performance"** is auto-provisioned in Grafana with:

- **Query Latency** — avg / max latency over time
- **Query Throughput** — queries per second
- **HTTP Request Rate** — requests/s by method + endpoint
- **HTTP Latency (max)** — per-endpoint max latency
- **Success Rate** — % of 2xx responses
- **Error Rate** — 5xx / 4xx per second
- **P95 / P99 Latency** — tail latency percentiles

## Docker image (with OTel agent)

The `Dockerfile.backend` includes the OpenTelemetry Java agent for auto-instrumentation.
When deployed with the observability stack, traces are sent to Tempo automatically.

### Environment variables

| Variable | Default | Purpose |
|---|---|---|
| `JAVA_TOOL_OPTIONS` | `-javaagent:/app/opentelemetry-javaagent.jar` | OTel agent |
| `OTEL_SERVICE_NAME` | `finger-seal` | Service name in traces |
| `OTEL_EXPORTER_OTLP_ENDPOINT` | `http://tempo:4317` | OTLP gRPC target |
| `OTEL_METRICS_EXPORTER` | `prometheus` | Metrics via /actuator/prometheus |
| `OTEL_LOGS_EXPORTER` | `none` | Logs not exported |

### Manual spans

Services instrumented with manual spans:

| Span name | Location | Tags |
|---|---|---|
| `query.execute` | `QueryService.execute()` | `connection.id`, `sql.length` |
| `ws.broadcast` | `QueryEventWebSocketHandler.broadcast()` | `event.type` |

Errors are recorded on spans via `span.error(e)` for trace-aware error tracking.

## Troubleshooting

- Grafana datasources are auto-provisioned. If missing, restart Grafana: `docker compose restart grafana`
- Ensure the backend is running and accessible for Prometheus scraping (default: `http://app:8080/actuator/prometheus`)
