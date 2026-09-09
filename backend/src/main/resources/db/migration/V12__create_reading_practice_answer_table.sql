CREATE TABLE reading_practice_answer (
                                         id INTEGER PRIMARY KEY AUTOINCREMENT,

                                         practice_record_id INTEGER NOT NULL,

                                         question_id INTEGER NOT NULL,

                                         question_number INTEGER NOT NULL,

                                         user_answer TEXT,

                                         correct_answer TEXT NOT NULL,

                                         correct INTEGER NOT NULL,

                                         FOREIGN KEY (practice_record_id)
                                             REFERENCES reading_practice_record(id)
                                             ON DELETE CASCADE
);
