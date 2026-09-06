ALTER TABLE reading_test
    ADD COLUMN external_id VARCHAR(100);

CREATE UNIQUE INDEX uk_reading_test_external_id
    ON reading_test(external_id);
