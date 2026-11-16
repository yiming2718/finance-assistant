CREATE TABLE users (
                       id             NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                       email          VARCHAR2(255) NOT NULL,
                       password_hash  VARCHAR2(100) NOT NULL,
                       created_at     TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
                       CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE accounts (
                          id               NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                          user_id          NUMBER NOT NULL,
                          name             VARCHAR2(100) NOT NULL,
                          account_type     VARCHAR2(20) NOT NULL,
                          opening_balance  NUMBER(19,2) DEFAULT 0 NOT NULL,
                          version          NUMBER DEFAULT 0 NOT NULL,
                          CONSTRAINT fk_accounts_user FOREIGN KEY (user_id) REFERENCES users(id),
                          CONSTRAINT ck_accounts_type CHECK (account_type IN ('CASH','BANK','CREDIT_CARD','INVESTMENT'))
);

CREATE TABLE categories (
                            id             NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                            user_id        NUMBER NOT NULL,
                            name           VARCHAR2(100) NOT NULL,
                            category_type  VARCHAR2(10) NOT NULL,
                            CONSTRAINT fk_categories_user FOREIGN KEY (user_id) REFERENCES users(id),
                            CONSTRAINT ck_categories_type CHECK (category_type IN ('INCOME','EXPENSE')),
                            CONSTRAINT uk_categories UNIQUE (user_id, name, category_type)
);

CREATE TABLE transactions (
                              id                 NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                              account_id         NUMBER NOT NULL,
                              category_id        NUMBER,
                              amount             NUMBER(19,2) NOT NULL,
                              txn_type           VARCHAR2(10) NOT NULL,
                              txn_date           DATE NOT NULL,
                              description        VARCHAR2(255),
                              transfer_group_id  VARCHAR2(36),
                              import_hash        VARCHAR2(64),
                              created_at         TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
                              CONSTRAINT fk_txn_account  FOREIGN KEY (account_id)  REFERENCES accounts(id),
                              CONSTRAINT fk_txn_category FOREIGN KEY (category_id) REFERENCES categories(id),
                              CONSTRAINT ck_txn_type CHECK (txn_type IN ('INCOME','EXPENSE','TRANSFER')),
                              CONSTRAINT uk_txn_import UNIQUE (account_id, import_hash)
);

CREATE INDEX idx_txn_account_date ON transactions(account_id, txn_date);
CREATE INDEX idx_txn_category     ON transactions(category_id);
CREATE INDEX idx_txn_transfer     ON transactions(transfer_group_id);

CREATE TABLE budgets (
                         id            NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                         user_id       NUMBER NOT NULL,
                         category_id   NUMBER NOT NULL,
                         budget_month  VARCHAR2(7) NOT NULL,
                         limit_amount  NUMBER(19,2) NOT NULL,
                         CONSTRAINT fk_budget_user     FOREIGN KEY (user_id)     REFERENCES users(id),
                         CONSTRAINT fk_budget_category FOREIGN KEY (category_id) REFERENCES categories(id),
                         CONSTRAINT uk_budget UNIQUE (user_id, category_id, budget_month)
);

CREATE TABLE recurring_rules (
                                 id            NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                 account_id    NUMBER NOT NULL,
                                 category_id   NUMBER,
                                 amount        NUMBER(19,2) NOT NULL,
                                 day_of_month  NUMBER(2) NOT NULL,
                                 description   VARCHAR2(255),
                                 active        NUMBER(1) DEFAULT 1 NOT NULL,
                                 CONSTRAINT fk_rec_account  FOREIGN KEY (account_id)  REFERENCES accounts(id),
                                 CONSTRAINT fk_rec_category FOREIGN KEY (category_id) REFERENCES categories(id),
                                 CONSTRAINT ck_rec_day CHECK (day_of_month BETWEEN 1 AND 28)
);