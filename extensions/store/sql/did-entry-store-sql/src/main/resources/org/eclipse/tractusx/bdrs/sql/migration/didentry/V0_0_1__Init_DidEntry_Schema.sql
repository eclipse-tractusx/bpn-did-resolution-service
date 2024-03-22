CREATE TABLE IF NOT EXISTS edc_did_entries
(
    bpn VARCHAR NOT NULL PRIMARY KEY,
    did VARCHAR NOT NULL
);

CREATE TABLE IF NOT EXISTS edc_did_entry_metadata
(
    version    INT    NOT NULL DEFAULT 0
);

-- insert "version=0" if not exists:
INSERT INTO edc_did_entry_metadata (version)
SELECT 0
WHERE NOT EXISTS (SELECT * FROM edc_did_entry_metadata)