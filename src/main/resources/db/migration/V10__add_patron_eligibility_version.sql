ALTER TABLE patron.patron
    ADD COLUMN eligibility_version BIGINT NOT NULL DEFAULT 0 CONSTRAINT non_negative_check CHECK (eligibility_version >= 0);