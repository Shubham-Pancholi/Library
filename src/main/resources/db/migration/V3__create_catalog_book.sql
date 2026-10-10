CREATE TABLE catalog.book(
    id UUID NOT NULL PRIMARY KEY,
    title TEXT NOT NULL CONSTRAINT book_title_not_empty CHECK (btrim(title) <> '')
);