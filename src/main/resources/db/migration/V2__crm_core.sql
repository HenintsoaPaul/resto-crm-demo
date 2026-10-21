create table tags (
    id          bigserial primary key,
    restaurant_id bigint       not null references restaurants (id) on delete cascade,
    name        varchar(100) not null,
    color       varchar(20),
    created_at  timestamptz  not null default now(),
    updated_at  timestamptz  not null default now(),
    constraint uq_tags_restaurant_name unique (restaurant_id, name)
);

create index idx_tags_restaurant_id on tags (restaurant_id);

create table clients (
    id             bigserial primary key,
    restaurant_id  bigint       not null references restaurants (id) on delete cascade,
    first_name     varchar(255) not null,
    last_name      varchar(255) not null,
    email          varchar(255),
    phone          varchar(50),
    birthdate      date,
    address        varchar(500),
    last_visit_date timestamptz,
    visit_count    integer      not null default 0,
    total_spend    numeric(12, 2),
    created_by     bigint references users (id) on delete set null,
    deleted_at     timestamptz,
    created_at     timestamptz  not null default now(),
    updated_at     timestamptz  not null default now()
);

create index idx_clients_restaurant_last_visit on clients (restaurant_id, last_visit_date);
create index idx_clients_restaurant_email on clients (restaurant_id, lower(email));
create index idx_clients_restaurant_deleted_at on clients (restaurant_id, deleted_at);

create table client_tags (
    client_id  bigint not null references clients (id) on delete cascade,
    tag_id     bigint not null references tags (id) on delete cascade,
    primary key (client_id, tag_id)
);

create index idx_client_tags_tag_id on client_tags (tag_id);

create table visits (
    id            bigserial primary key,
    client_id     bigint      not null references clients (id) on delete cascade,
    restaurant_id bigint      not null references restaurants (id) on delete cascade,
    visit_date    timestamptz not null,
    party_size    integer,
    amount_spent  numeric(12, 2),
    notes         varchar(1000),
    created_by    bigint references users (id) on delete set null,
    created_at    timestamptz not null default now(),
    updated_at    timestamptz not null default now()
);

create index idx_visits_restaurant_visit_date on visits (restaurant_id, visit_date);
create index idx_visits_client_visit_date on visits (client_id, visit_date);

create table client_notes (
    id              bigserial primary key,
    client_id       bigint       not null references clients (id) on delete cascade,
    restaurant_id   bigint       not null references restaurants (id) on delete cascade,
    author_user_id  bigint references users (id) on delete set null,
    body            varchar(4000) not null,
    created_at      timestamptz  not null default now(),
    updated_at      timestamptz  not null default now()
);

create index idx_client_notes_client_id on client_notes (client_id);
create index idx_client_notes_restaurant_id on client_notes (restaurant_id);
