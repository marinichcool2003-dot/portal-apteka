CREATE TYPE task_status AS ENUM (
    'OPEN',
    'CLOSED',
    'DENIED',
    'PROCESSED'
);

CREATE TYPE task_priority AS ENUM (
    'LOW',
    'MIDDLE',
    'HIGH'
);

CREATE TYPE client_role AS ENUM (
    'USER',
    'LEGEND',
    'ADMIN',
    'MANAGER',
    'SPEC'
);