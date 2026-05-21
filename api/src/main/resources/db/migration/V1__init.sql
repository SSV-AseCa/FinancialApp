CREATE TABLE investor (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    auth0_sub VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE portfolio (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    investor_id UUID NOT NULL UNIQUE REFERENCES investor(id),
    name VARCHAR(255) NOT NULL DEFAULT 'My Portfolio',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
