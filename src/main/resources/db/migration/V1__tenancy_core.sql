create table groups
(
    id         bigserial primary key,
    name       varchar(255) not null,
    created_at timestamptz  not null default now(),
    updated_at timestamptz  not null default now()
);

create table restaurants
(
    id         bigserial primary key,
    group_id   bigint       not null references groups (id) on delete cascade,
    name       varchar(255) not null,
    address    varchar(500),
    phone      varchar(50),
    is_active  boolean      not null default true,
    created_at timestamptz  not null default now(),
    updated_at timestamptz  not null default now()
);

create index idx_restaurants_group_id on restaurants (group_id);

create table users
(
    id            bigserial primary key,
    email         varchar(255) not null unique,
    password_hash varchar(255) not null,
    full_name     varchar(255) not null,
    role          varchar(30)  not null,
    group_id      bigint       not null references groups (id) on delete cascade,
    restaurant_id bigint references restaurants (id) on delete cascade,
    is_active     boolean      not null default true,
    last_login_at timestamptz,
    created_at    timestamptz  not null default now(),
    updated_at    timestamptz  not null default now(),
    constraint chk_users_role_restaurant check (
        (role = 'GROUP_ADMIN' and restaurant_id is null)
            or (role in ('RESTAURANT_ADMIN', 'STAFF') and restaurant_id is not null)
        )
);

create index idx_users_group_id on users (group_id);
create index idx_users_restaurant_id on users (restaurant_id);

create table refresh_tokens
(
    id         bigserial primary key,
    user_id    bigint       not null references users (id) on delete cascade,
    token_hash varchar(255) not null unique,
    expires_at timestamptz  not null,
    revoked_at timestamptz,
    created_at timestamptz  not null default now(),
    updated_at timestamptz  not null default now()
);

create index idx_refresh_tokens_user_id on refresh_tokens (user_id);