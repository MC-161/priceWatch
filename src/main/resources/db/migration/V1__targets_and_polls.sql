-- M0 schema: a target is one URL = one price series.

CREATE TABLE target (
    id               BIGSERIAL PRIMARY KEY,
    url              TEXT          NOT NULL,
    selector         TEXT          NOT NULL,           -- CSS selector for the price element
    currency         CHAR(3)       NOT NULL,           -- ISO 4217
    threshold        NUMERIC(19,4) NOT NULL CHECK (threshold > 0),
    interval_seconds INTEGER       NOT NULL CHECK (interval_seconds >= 60),
    created_at       TIMESTAMPTZ   NOT NULL DEFAULT now()
);

-- One row per poll attempt, successful or not.
-- A failed poll stores NULL price and a status, never 0.
CREATE TABLE poll (
    id            BIGSERIAL PRIMARY KEY,
    target_id     BIGINT        NOT NULL REFERENCES target (id) ON DELETE CASCADE,
    polled_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),
    status        TEXT          NOT NULL CHECK (status IN ('OK', 'PARSE_ERROR', 'FETCH_ERROR')),
    price         NUMERIC(19,4),
    http_status   INTEGER,
    error         TEXT,
    raw_response  TEXT,                                 -- kept so a broken parser can be detected and replayed
    CONSTRAINT price_iff_ok CHECK ((status = 'OK') = (price IS NOT NULL))
);

CREATE INDEX poll_target_time_idx ON poll (target_id, polled_at DESC);
