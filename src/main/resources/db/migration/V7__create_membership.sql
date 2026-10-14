CREATE TABLE patron.membership (
    id UUID NOT NULL PRIMARY KEY,
    patron_id UUID NOT NULL REFERENCES patron.patron(id),
    plan_version_id UUID NOT NULL REFERENCES patron.membership_plan_version(id),
    starts_at TIMESTAMPTZ NOT NULL,
    ends_at TIMESTAMPTZ NOT NULL,
    status VARCHAR(16) NOT NULL CONSTRAINT status_enum CHECK (status IN ('ACTIVE', 'CANCELLED')),
    CONSTRAINT ends_at_later_than_starts_at CHECK (ends_at > starts_at)
);