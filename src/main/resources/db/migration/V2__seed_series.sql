-- Catalogo inicial de series soportadas por el ingestor de dolarapi.com.
-- Los valores en si NO se siembran aca: los trae IngestScheduler contra la API real.

INSERT INTO indicator_series (code, name, unit, source) VALUES
    ('dolar-oficial', 'Dólar Oficial',       'ARS', 'dolarapi.com'),
    ('dolar-blue',    'Dólar Blue',          'ARS', 'dolarapi.com'),
    ('dolar-mep',     'Dólar MEP',           'ARS', 'dolarapi.com'),
    ('dolar-ccl',     'Dólar Contado con Liquidación', 'ARS', 'dolarapi.com'),
    ('dolar-cripto',  'Dólar Cripto',        'ARS', 'dolarapi.com');
