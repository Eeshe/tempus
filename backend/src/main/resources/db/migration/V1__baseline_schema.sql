CREATE TABLE clients (
    id integer, 
    created_at timestamp not null,
    name varchar(255) not null,
    user_id bigint not null,
    primary key (id)
);

CREATE TABLE database_meta (
    current_snapshot_time INTEGER NOT NULL
);

CREATE TABLE projects (
    id integer,
    created_at timestamp not null,
    hourly_rate numeric(38,2),
    name varchar(255) not null,
    client_id bigint,
    user_id bigint,
    is_archived boolean default false not null,
    primary key (id)
);

CREATE TABLE tasks (
    id integer,
    created_at timestamp not null,
    name varchar(255) not null,
    project_id bigint not null,
    user_id bigint not null,
    primary key (id)
);

CREATE TABLE time_entries (
    id integer,
    created_at timestamp not null,
    description varchar(255),
    end_time timestamp,
    is_billable boolean not null,
    start_time timestamp not null,
    project_id bigint not null,
    task_id bigint,
    user_id bigint not null,
    primary key (id)
);

CREATE TABLE users (id integer,
    created_at timestamp not null,
    name varchar(255) not null unique,
    password varchar(255) not null,
    primary key (id)
);
