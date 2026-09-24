-- =============================================================================
-- Royal Pearl Hyderabad – V1__init.sql
-- Full schema: enums, tables, constraints, indexes, updated_at trigger
-- =============================================================================

-- ---------------------------------------------------------------------------
-- Extensions
-- ---------------------------------------------------------------------------
CREATE EXTENSION IF NOT EXISTS "pgcrypto";   -- gen_random_uuid()

-- ---------------------------------------------------------------------------
-- Enums
-- ---------------------------------------------------------------------------
DO $$ BEGIN
  CREATE TYPE app_role AS ENUM ('admin', 'moderator', 'user');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

-- ---------------------------------------------------------------------------
-- updated_at trigger function (shared by all tables)
-- ---------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ---------------------------------------------------------------------------
-- users
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
  id               UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
  email            TEXT        UNIQUE NOT NULL,
  password_hash    TEXT,                          -- NULL for OAuth-only users
  full_name        TEXT,
  phone            TEXT,
  avatar_url       TEXT,
  provider         TEXT        NOT NULL DEFAULT 'email',
  email_verified   BOOLEAN     NOT NULL DEFAULT TRUE,
  created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TRIGGER trg_users_updated_at
  BEFORE UPDATE ON users
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ---------------------------------------------------------------------------
-- user_roles
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_roles (
  id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id    UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  role       app_role    NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  UNIQUE (user_id, role)
);

CREATE INDEX IF NOT EXISTS idx_user_roles_user_id ON user_roles(user_id);

-- ---------------------------------------------------------------------------
-- rooms
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS rooms (
  id              UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
  name            TEXT           NOT NULL,
  slug            TEXT           UNIQUE NOT NULL,
  description     TEXT,
  price_per_night NUMERIC(10,2)  NOT NULL,
  max_guests      INT            NOT NULL,
  image_url       TEXT,
  amenities       JSONB,
  active          BOOLEAN        NOT NULL DEFAULT TRUE,
  created_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

CREATE TRIGGER trg_rooms_updated_at
  BEFORE UPDATE ON rooms
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE INDEX IF NOT EXISTS idx_rooms_slug   ON rooms(slug);
CREATE INDEX IF NOT EXISTS idx_rooms_active ON rooms(active);

-- ---------------------------------------------------------------------------
-- bookings
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS bookings (
  id              UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
  booking_id      TEXT           UNIQUE,                  -- RP + 7 digits, server-generated
  user_id         UUID           REFERENCES users(id) ON DELETE SET NULL,
  full_name       TEXT           NOT NULL,
  email           TEXT           NOT NULL,
  phone           TEXT           NOT NULL,
  room_name       TEXT,
  check_in        DATE,
  check_out       DATE,
  guests          INT,
  arrival         TIME,
  notes           TEXT,
  room_price      NUMERIC(10,2),
  total_amount    NUMERIC(10,2),
  status          TEXT           NOT NULL DEFAULT 'new',
  booking_status  TEXT           NOT NULL DEFAULT 'pending',
  payment_method  TEXT           NOT NULL DEFAULT 'cash',
  payment_status  TEXT           NOT NULL DEFAULT 'pending',
  created_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW(),

  -- Constraints
  CONSTRAINT chk_bookings_payment_method  CHECK (payment_method  IN ('cash','online')),
  CONSTRAINT chk_bookings_payment_status  CHECK (payment_status  IN ('pending','paid','refunded')),
  CONSTRAINT chk_bookings_booking_status  CHECK (booking_status  IN ('pending','confirmed','checked_in','checked_out','cancelled')),
  CONSTRAINT chk_bookings_checkout_after_checkin CHECK (check_out > check_in),
  CONSTRAINT chk_bookings_guests_positive CHECK (guests > 0)
);

CREATE TRIGGER trg_bookings_updated_at
  BEFORE UPDATE ON bookings
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE INDEX IF NOT EXISTS idx_bookings_booking_id   ON bookings(booking_id);
CREATE INDEX IF NOT EXISTS idx_bookings_user_id      ON bookings(user_id);
CREATE INDEX IF NOT EXISTS idx_bookings_created_at   ON bookings(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_bookings_email        ON bookings(email);

-- ---------------------------------------------------------------------------
-- menu_items
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS menu_items (
  id          UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
  name        TEXT           NOT NULL,
  category    TEXT           NOT NULL,
  description TEXT,
  price       NUMERIC(10,2)  NOT NULL,
  image_url   TEXT,
  veg         BOOLEAN        NOT NULL,
  available   BOOLEAN        NOT NULL DEFAULT TRUE,
  sort_order  INT,
  created_at  TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
  updated_at  TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

CREATE TRIGGER trg_menu_items_updated_at
  BEFORE UPDATE ON menu_items
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE INDEX IF NOT EXISTS idx_menu_items_category  ON menu_items(category);
CREATE INDEX IF NOT EXISTS idx_menu_items_available ON menu_items(available);
CREATE INDEX IF NOT EXISTS idx_menu_items_sort      ON menu_items(sort_order NULLS LAST);

-- ---------------------------------------------------------------------------
-- table_reservations  (restaurant food orders)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS table_reservations (
  id              UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
  order_id        TEXT           UNIQUE,                  -- RPO- + 7 digits, server-generated
  user_id         UUID           REFERENCES users(id) ON DELETE SET NULL,
  name            TEXT           NOT NULL,
  phone           TEXT           NOT NULL,
  order_type      TEXT,                                   -- dine_in | takeaway | delivery
  preferred_time  TEXT,
  address         TEXT,
  notes           TEXT,
  items           JSONB          NOT NULL,                -- [{name, price, qty}, ...]
  total           NUMERIC(10,2)  NOT NULL,
  status          TEXT           NOT NULL DEFAULT 'new',
  order_status    TEXT           NOT NULL DEFAULT 'pending',
  payment_method  TEXT           NOT NULL DEFAULT 'cash',
  payment_status  TEXT           NOT NULL DEFAULT 'pending',
  created_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW(),

  -- Constraints
  CONSTRAINT chk_orders_payment_method CHECK (payment_method  IN ('cash','online')),
  CONSTRAINT chk_orders_payment_status CHECK (payment_status  IN ('pending','paid','refunded')),
  CONSTRAINT chk_orders_order_status   CHECK (order_status    IN ('pending','preparing','ready','delivered','cancelled'))
);

CREATE TRIGGER trg_orders_updated_at
  BEFORE UPDATE ON table_reservations
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE INDEX IF NOT EXISTS idx_orders_order_id   ON table_reservations(order_id);
CREATE INDEX IF NOT EXISTS idx_orders_user_id    ON table_reservations(user_id);
CREATE INDEX IF NOT EXISTS idx_orders_created_at ON table_reservations(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_orders_phone      ON table_reservations(phone);

-- ---------------------------------------------------------------------------
-- contact_messages
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS contact_messages (
  id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
  name       TEXT        NOT NULL,
  email      TEXT        NOT NULL,
  phone      TEXT,
  subject    TEXT,
  message    TEXT        NOT NULL,
  status     TEXT        NOT NULL DEFAULT 'new',
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

  CONSTRAINT chk_contact_status CHECK (status IN ('new','read','replied','archived'))
);

CREATE INDEX IF NOT EXISTS idx_contact_created_at ON contact_messages(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_contact_status     ON contact_messages(status);

-- ---------------------------------------------------------------------------
-- newsletter_subscribers
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS newsletter_subscribers (
  id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
  email      TEXT        UNIQUE NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ---------------------------------------------------------------------------
-- refresh_tokens
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS refresh_tokens (
  id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id     UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  token_hash  TEXT        NOT NULL,
  expires_at  TIMESTAMPTZ NOT NULL,
  revoked     BOOLEAN     NOT NULL DEFAULT FALSE,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id    ON refresh_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_token_hash ON refresh_tokens(token_hash);
