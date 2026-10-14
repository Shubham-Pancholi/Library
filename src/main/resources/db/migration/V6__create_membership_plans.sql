CREATE TABLE patron.membership_plan (
    id UUID NOT NULL PRIMARY KEY,
    name TEXT NOT NULL CONSTRAINT name_not_blank CHECK (btrim(name) <> '')
);

CREATE TABLE patron.membership_plan_version (
    id UUID NOT NULL PRIMARY KEY,
    plan_id UUID NOT NULL REFERENCES patron.membership_plan(id),
    effective_from TIMESTAMPTZ NOT NULL,
    effective_to TIMESTAMPTZ,
    max_copies INT NOT NULL CONSTRAINT max_copies_greater_than_zero CHECK (max_copies > 0),
    max_days INT NOT NULL CONSTRAINT max_days_greater_than_zero CHECK (max_days > 0),
    CONSTRAINT effective_to_date_later_than_from CHECK (effective_to > effective_from)
);