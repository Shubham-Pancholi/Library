CREATE TABLE patron.administrative_block_history (
    id UUID NOT NULL PRIMARY KEY,
    patron_id UUID NOT NULL REFERENCES patron.patron(id),
    action VARCHAR(20) NOT NULL CONSTRAINT action_enum CHECK (action IN ('BLOCKED', 'CLEARED')),
    reason TEXT NOT NULL CONSTRAINT reason_validation CHECK (btrim(reason) <> ''),
    changed_at TIMESTAMPTZ NOT NULL,
    actor_issuer TEXT NOT NULL CONSTRAINT actor_issure_validation CHECK(btrim(actor_issuer) <> ''),
    actor_subject TEXT NOT NULL CONSTRAINT actor_subject_validation CHECK(btrim(actor_subject) <> '')
);