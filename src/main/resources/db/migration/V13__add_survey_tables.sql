-- Survey/Question system for post-sale feedback

CREATE TABLE questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question_text VARCHAR(500) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT NOW()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE survey_responses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    response_value VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_survey_response_post FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    CONSTRAINT fk_survey_response_question FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_survey_responses_post_id ON survey_responses(post_id);
CREATE INDEX idx_survey_responses_question_id ON survey_responses(question_id);

-- Seed the first survey question
INSERT INTO questions (question_text) VALUES ('Was the product sold through our platform?');
