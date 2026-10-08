CREATE TABLE public.event_publication(
    id UUID PRIMARY KEY,
    publication_date TIMESTAMP WITH TIME ZONE NOT NULL,
    listener_id VARCHAR(512) NOT NULL,
    event_type VARCHAR(512) NOT NULL,
    serialized_event TEXT NOT NULL,
    completion_date TIMESTAMP WITH TIME ZONE,
    status VARCHAR(32),
    completion_attempts INTEGER NOT NULL DEFAULT 0,
    last_resubmission_date TIMESTAMP WITH TIME ZONE
);