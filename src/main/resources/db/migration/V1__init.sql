CREATE TABLE payments (
    id SERIAL PRIMARY KEY,
    customer_id VARCHAR(255) NOT NULL,
    price NUMERIC(10, 2) NOT NULL,
    final_price NUMERIC(10, 2) NOT NULL,
    points NUMERIC(10, 2) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    datetime TIMESTAMP NOT NULL,
    additional_item JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_payments_datetime ON payments (datetime);
