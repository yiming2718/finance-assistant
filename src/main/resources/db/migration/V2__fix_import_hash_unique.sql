ALTER TABLE transactions DROP CONSTRAINT uk_txn_import;

CREATE UNIQUE INDEX uk_txn_import ON transactions (
                                                   CASE WHEN import_hash IS NOT NULL THEN account_id END,
                                                   CASE WHEN import_hash IS NOT NULL THEN import_hash END
    );