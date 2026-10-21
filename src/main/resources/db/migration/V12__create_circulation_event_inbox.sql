CREATE TABLE circulation.event_inbox (
    consumer_name VARCHAR(128) NOT NULL CONSTRAINT name_not_blank CHECK (btrim(consumer_name) <> ''),
    event_id UUID NOT NULL,
    processed_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (consumer_name, event_id)
);