-- Serie de indicadores (ej: "dolar-oficial", "dolar-blue", "dolar-mep") y sus valores
-- historicos. Diseñado para soportar multiples fuentes e indicadores futuros
-- (inflacion, tasas, paritarias) sin cambiar el esquema.

CREATE TABLE indicator_series (
    id            BIGSERIAL PRIMARY KEY,
    code          VARCHAR(64)  NOT NULL UNIQUE,
    name          VARCHAR(128) NOT NULL,
    unit          VARCHAR(32)  NOT NULL,
    source        VARCHAR(64)  NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE indicator_value (
    id            BIGSERIAL PRIMARY KEY,
    series_id     BIGINT       NOT NULL REFERENCES indicator_series(id) ON DELETE CASCADE,
    observed_at   TIMESTAMPTZ  NOT NULL,
    buy_value     NUMERIC(14,4),
    sell_value    NUMERIC(14,4) NOT NULL,
    fetched_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_series_observed UNIQUE (series_id, observed_at)
);

CREATE INDEX idx_indicator_value_series_date ON indicator_value (series_id, observed_at DESC);

COMMENT ON TABLE indicator_series IS 'Catalogo de series (una por tipo de dolar/indicador)';
COMMENT ON TABLE indicator_value IS 'Valores historicos observados de cada serie';
