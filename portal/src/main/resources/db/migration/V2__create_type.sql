DROP TYPE IF EXISTS task_status;
CREATE TYPE task_status AS ENUM (
    'OPEN',
    'CLOSED',
    'DENIED',
    'PROCESSED'
);

DROP TYPE IF EXISTS task_priority;
CREATE TYPE task_priority AS ENUM (
    'LOW',
    'MIDDLE',
    'HIGH'
);