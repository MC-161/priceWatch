# Schema

Source of truth: `src/main/resources/db/migration/`. This page records *why*.

## `target`

| Column | Type | Notes |
|---|---|---|
| `url` | text | One target = one URL = one price series. No cross-source matching. |
| `selector` | text | CSS selector. `content` attribute is preferred over text if present. |
| `currency` | char(3) | ISO 4217. Prices are never compared across currencies. |
| `threshold` | numeric(19,4) | Alert when price is **at or below** this. |
| `interval_seconds` | int | ≥ 60. Per-target poll interval. |

## `poll`

One row per attempt, including failures.

| Column | Notes |
|---|---|
| `status` | `OK`, `PARSE_ERROR`, `FETCH_ERROR` |
| `price` | `NULL` unless `OK`. Enforced by `price_iff_ok` check. A failure is never stored as 0. |
| `raw_response` | Body as fetched. Lets a silently broken parser be detected and re-parsed later. |

## Decisions

- **Money:** `BigDecimal` at scale 4 ↔ `NUMERIC(19,4)`. Never `double`.
- **Parse failure:** an error status with `NULL` price, not `0`. A `0` would satisfy every threshold and fire a false alert.
- **Raw responses:** stored per poll. Storage cost is the price of being able to replay parsers.

## Coming

- M1: `next_due_at` on `target` (or a separate `job` table)
- M2: lease columns (`claimed_by`, `lease_expires_at`) or a version column
- M3: `alert_state` and `outbox`
