CREATE TABLE circulation.patron_eligibility_projection (
    patron_id UUID NOT NULL PRIMARY KEY,
    source_version BIGINT NOT NULL CONSTRAINT source_version_check CHECK (source_version >= 0),
    administrative_block BOOLEAN NOT NULL,
    membership_id UUID,
    plan_version_id UUID,
    valid_from TIMESTAMPTZ,
    valid_until TIMESTAMPTZ,
    max_copies INTEGER,
    max_days INTEGER,
    CONSTRAINT validity_membership CHECK (
        (membership_id IS NULL AND plan_version_id IS NULL AND valid_from IS NULL AND valid_until IS NULL AND max_copies IS NULL AND max_days IS NULL)
        OR
        (
            (membership_id IS NOT NULL AND plan_version_id IS NOT NULL AND valid_from IS NOT NULL AND valid_until IS NOT NULL AND max_copies IS NOT NULL AND max_days IS NOT NULL)
            AND
            (valid_until > valid_from AND max_copies > 0 AND max_days > 0)
        )
    )
);