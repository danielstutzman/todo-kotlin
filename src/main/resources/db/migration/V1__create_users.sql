CREATE TABLE users (
  id                     SERIAL,
  email                  TEXT NOT NULL,
  encrypted_password     TEXT NOT NULL,
  confirmed_at           TIMESTAMPTZ,
  created_at             TIMESTAMPTZ NOT NULL,
  updated_at             TIMESTAMPTZ NOT NULL,
  PRIMARY KEY (id)
);
CREATE UNIQUE INDEX idx_users_email on users(email);
CREATE UNIQUE INDEX idx_users_lower_email on users(lower(email));
