CREATE TABLE reading_practice_record (
                                         id INTEGER PRIMARY KEY AUTOINCREMENT,

                                         test_id INTEGER NOT NULL,

                                         correct_count INTEGER NOT NULL,

                                         total_questions INTEGER NOT NULL,

                                         percentage REAL NOT NULL,

                                         submitted_at TEXT NOT NULL,

                                         FOREIGN KEY (test_id)
                                             REFERENCES reading_test(id)
);
