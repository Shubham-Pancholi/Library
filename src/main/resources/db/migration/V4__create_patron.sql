CREATE TABLE patron.patron(
    id UUID PRIMARY KEY,
    patron_type VARCHAR(16) NOT NULL CONSTRAINT patron_type_constraint CHECK (patron_type IN ('MEMBER', 'VISITOR')),
    first_name TEXT NOT NULL CONSTRAINT first_name_required CHECK (btrim(first_name) <> ''),
    last_name TEXT,
    phone TEXT
);