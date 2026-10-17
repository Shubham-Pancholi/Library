ALTER TABLE patron.patron
    ADD COLUMN administrative_block BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN administrative_block_reason TEXT,
    ADD CONSTRAINT block_mechanism_check CHECK (
        (administrative_block IS TRUE AND (administrative_block_reason IS NOT NULL AND btrim(administrative_block_reason) <> ''))
        OR
        (administrative_block IS FALSE AND administrative_block_reason IS NULL)
    );