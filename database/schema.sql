-- Digital Stamp schema (MySQL 8+). Types stay portable for a later PostgreSQL move:
-- BIGINT identity, VARCHAR, TEXT, BOOLEAN/TINYINT, DATETIME(6)/timestamptz.

CREATE DATABASE IF NOT EXISTS digital_stamp
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE digital_stamp;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(120) NOT NULL,
  email VARCHAR(180) NOT NULL,
  password VARCHAR(255) NOT NULL,
  mobile VARCHAR(20),
  role VARCHAR(30) NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  qr_token VARCHAR(64) NOT NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  UNIQUE KEY uk_users_email (email),
  UNIQUE KEY uk_users_qr_token (qr_token),
  KEY idx_users_role (role)
);

CREATE TABLE IF NOT EXISTS shops (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(160) NOT NULL,
  slug VARCHAR(120) NOT NULL,
  logo VARCHAR(500),
  description VARCHAR(2000),
  address VARCHAR(500),
  phone VARCHAR(20),
  email VARCHAR(180),
  opening_hours VARCHAR(2000),
  active BOOLEAN NOT NULL DEFAULT TRUE,
  owner_id BIGINT,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  UNIQUE KEY uk_shops_slug (slug),
  KEY idx_shops_owner (owner_id),
  KEY idx_shops_active (active),
  CONSTRAINT fk_shops_owner FOREIGN KEY (owner_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS loyalty_programs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  shop_id BIGINT NOT NULL,
  name VARCHAR(160) NOT NULL,
  description VARCHAR(1000),
  required_stamps INT NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  UNIQUE KEY uk_loyalty_program_shop (shop_id),
  CONSTRAINT fk_program_shop FOREIGN KEY (shop_id) REFERENCES shops(id)
);

CREATE TABLE IF NOT EXISTS loyalty_cards (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  customer_id BIGINT NOT NULL,
  shop_id BIGINT NOT NULL,
  loyalty_program_id BIGINT NOT NULL,
  current_stamps INT NOT NULL DEFAULT 0,
  total_stamps INT NOT NULL DEFAULT 0,
  status VARCHAR(30) NOT NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  UNIQUE KEY uk_loyalty_card_customer_shop (customer_id, shop_id),
  KEY idx_loyalty_card_shop (shop_id),
  KEY idx_loyalty_card_status (status),
  CONSTRAINT fk_card_customer FOREIGN KEY (customer_id) REFERENCES users(id),
  CONSTRAINT fk_card_shop FOREIGN KEY (shop_id) REFERENCES shops(id),
  CONSTRAINT fk_card_program FOREIGN KEY (loyalty_program_id) REFERENCES loyalty_programs(id)
);

CREATE TABLE IF NOT EXISTS stamp_transactions (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  loyalty_card_id BIGINT NOT NULL,
  customer_id BIGINT NOT NULL,
  shop_id BIGINT NOT NULL,
  shop_owner_id BIGINT,
  stamps_added INT NOT NULL,
  transaction_type VARCHAR(30) NOT NULL,
  description VARCHAR(500),
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  KEY idx_stamp_tx_card (loyalty_card_id),
  KEY idx_stamp_tx_customer (customer_id),
  KEY idx_stamp_tx_shop (shop_id),
  KEY idx_stamp_tx_created (created_at),
  CONSTRAINT fk_tx_card FOREIGN KEY (loyalty_card_id) REFERENCES loyalty_cards(id),
  CONSTRAINT fk_tx_customer FOREIGN KEY (customer_id) REFERENCES users(id),
  CONSTRAINT fk_tx_shop FOREIGN KEY (shop_id) REFERENCES shops(id),
  CONSTRAINT fk_tx_owner FOREIGN KEY (shop_owner_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS rewards (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  shop_id BIGINT NOT NULL,
  loyalty_program_id BIGINT NOT NULL,
  name VARCHAR(160) NOT NULL,
  description VARCHAR(1000),
  required_stamps INT NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  KEY idx_reward_shop (shop_id),
  KEY idx_reward_program (loyalty_program_id),
  CONSTRAINT fk_reward_shop FOREIGN KEY (shop_id) REFERENCES shops(id),
  CONSTRAINT fk_reward_program FOREIGN KEY (loyalty_program_id) REFERENCES loyalty_programs(id)
);

CREATE TABLE IF NOT EXISTS reward_redemptions (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  reward_id BIGINT NOT NULL,
  customer_id BIGINT NOT NULL,
  shop_id BIGINT NOT NULL,
  loyalty_card_id BIGINT NOT NULL,
  redeemed_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  status VARCHAR(30) NOT NULL,
  redemption_code VARCHAR(40) NOT NULL,
  stamps_consumed INT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_redemption_code (redemption_code),
  KEY idx_redemption_customer (customer_id),
  KEY idx_redemption_shop (shop_id),
  CONSTRAINT fk_redemption_reward FOREIGN KEY (reward_id) REFERENCES rewards(id),
  CONSTRAINT fk_redemption_customer FOREIGN KEY (customer_id) REFERENCES users(id),
  CONSTRAINT fk_redemption_shop FOREIGN KEY (shop_id) REFERENCES shops(id),
  CONSTRAINT fk_redemption_card FOREIGN KEY (loyalty_card_id) REFERENCES loyalty_cards(id)
);

CREATE TABLE IF NOT EXISTS refresh_tokens (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  token VARCHAR(128) NOT NULL,
  user_id BIGINT NOT NULL,
  expiry_date TIMESTAMP(6) NOT NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  UNIQUE KEY uk_refresh_token (token),
  KEY idx_refresh_user (user_id),
  CONSTRAINT fk_refresh_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS system_settings (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  setting_key VARCHAR(80) NOT NULL,
  setting_value VARCHAR(1000) NOT NULL,
  description VARCHAR(255),
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  UNIQUE KEY uk_setting_key (setting_key)
);
