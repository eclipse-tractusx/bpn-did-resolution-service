CREATE TABLE IF NOT EXISTS edc_did_entries
(
    bpn VARCHAR NOT NULL PRIMARY KEY,
    did VARCHAR NOT NULL
);

-- CREATE TABLE IF NOT EXISTS edc_did_entry_metadata
-- (
--     updated_at BIGINT NOT NULL DEFAULT now(),
--     version    INT    NOT NULL DEFAULT 0
-- )

