CREATE TABLE user_settings (
    id integer,
    user_id bigint not null unique,
    background_color varchar(7) not null default '#101318',
    surface_color varchar(7) not null default '#161a22',
    text_color varchar(7) not null default '#e8eaf0',
    accent_color varchar(7) not null default '#3dd68c',
    danger_color varchar(7) not null default '#f2555c',
    primary key (id)
);

INSERT INTO user_settings (
    user_id,
    background_color,
    surface_color,
    text_color,
    accent_color,
    danger_color
)
SELECT
    users.id,
    '#101318',
    '#161a22',
    '#e8eaf0',
    '#3dd68c',
    '#f2555c'
FROM users
WHERE NOT EXISTS (
    SELECT 1
    FROM user_settings
    WHERE user_settings.user_id = users.id
);
