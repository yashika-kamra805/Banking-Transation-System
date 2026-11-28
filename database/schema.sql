CREATE DATABASE IF NOT EXISTS banking_db;

USE banking_db;

CREATE TABLE IF NOT EXISTS customers (
    customer_id BIGINT NOT NULL AUTO_INCREMENT,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(20) DEFAULT NULL,
    address VARCHAR(255) DEFAULT NULL,
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (customer_id),
    UNIQUE KEY email (email)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;

  CREATE TABLE IF NOT EXISTS accounts (
      account_id BIGINT NOT NULL AUTO_INCREMENT,
      customer_id BIGINT NOT NULL,
      account_number VARCHAR(30) NOT NULL,
      account_type VARCHAR(20) NOT NULL,
      balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
      status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
      created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
      PRIMARY KEY (account_id),
      UNIQUE KEY account_number (account_number),
      KEY fk_account_customer (customer_id),
      CONSTRAINT fk_account_customer
          FOREIGN KEY (customer_id)
          REFERENCES customers (customer_id),
      CONSTRAINT chk_account_balance CHECK (balance >= 0)
  ) ENGINE=InnoDB
    DEFAULT CHARSET=utf8mb4
    COLLATE=utf8mb4_0900_ai_ci;

    CREATE TABLE IF NOT EXISTS transactions (
        transaction_id BIGINT NOT NULL AUTO_INCREMENT,
        from_account_id BIGINT DEFAULT NULL,
        to_account_id BIGINT DEFAULT NULL,
        transaction_type VARCHAR(20) NOT NULL,
        amount DECIMAL(15,2) NOT NULL,
        status VARCHAR(20) NOT NULL,
        description VARCHAR(255) DEFAULT NULL,
        created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
        idempotency_key VARCHAR(100) DEFAULT NULL,

        PRIMARY KEY (transaction_id),

        UNIQUE KEY uk_transactions_idempotency_key (idempotency_key),
        KEY fk_transaction_from (from_account_id),
        KEY fk_transaction_to (to_account_id),

        CONSTRAINT fk_transaction_from
            FOREIGN KEY (from_account_id)
            REFERENCES accounts (account_id),

        CONSTRAINT fk_transaction_to
            FOREIGN KEY (to_account_id)
            REFERENCES accounts (account_id),

        CONSTRAINT chk_transaction_amount CHECK (amount > 0)

    ) ENGINE=InnoDB
      DEFAULT CHARSET=utf8mb4
      COLLATE=utf8mb4_0900_ai_ci;

      CREATE TABLE IF NOT EXISTS transaction_audit (
          audit_id BIGINT NOT NULL AUTO_INCREMENT,
          transaction_id BIGINT NOT NULL,
          action VARCHAR(50) NOT NULL,
          details TEXT,
          created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,

          PRIMARY KEY (audit_id),
          KEY fk_audit_transaction (transaction_id),

          CONSTRAINT fk_audit_transaction
              FOREIGN KEY (transaction_id)
              REFERENCES transactions (transaction_id)

      ) ENGINE=InnoDB
        DEFAULT CHARSET=utf8mb4
        COLLATE=utf8mb4_0900_ai_ci;

        CREATE TABLE IF NOT EXISTS failed_transactions (
            failed_id BIGINT NOT NULL AUTO_INCREMENT,
            transaction_id BIGINT DEFAULT NULL,
            error_message TEXT NOT NULL,
            attempted_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,

            PRIMARY KEY (failed_id)

        ) ENGINE=InnoDB
          DEFAULT CHARSET=utf8mb4
          COLLATE=utf8mb4_0900_ai_ci;

          CREATE TABLE IF NOT EXISTS idempotency_key_locks (
              idempotency_key VARCHAR(100) NOT NULL,
              created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,

              PRIMARY KEY (idempotency_key)

          ) ENGINE=InnoDB
            DEFAULT CHARSET=utf8mb4
            COLLATE=utf8mb4_0900_ai_ci;