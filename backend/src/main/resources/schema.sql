DROP TABLE IF EXISTS note;
DROP TABLE IF EXISTS folder;
DROP TABLE IF EXISTS user_account;

CREATE TABLE user_account (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    account VARCHAR(64) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT uk_user_account_account UNIQUE (account)
);

CREATE TABLE folder (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    owner_user_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    display_order INT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT fk_folder_owner FOREIGN KEY (owner_user_id) REFERENCES user_account (id),
    CONSTRAINT uk_folder_owner_name UNIQUE (owner_user_id, name)
);

CREATE TABLE note (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    author_user_id BIGINT NOT NULL,
    folder_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    markdown_content LONGTEXT NOT NULL,
    rendered_html LONGTEXT NOT NULL,
    publication_status VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT fk_note_author FOREIGN KEY (author_user_id) REFERENCES user_account (id),
    CONSTRAINT fk_note_folder FOREIGN KEY (folder_id) REFERENCES folder (id)
);

CREATE INDEX idx_user_account_account ON user_account (account);
CREATE INDEX idx_folder_owner_order ON folder (owner_user_id, display_order);
CREATE INDEX idx_note_author_folder_updated ON note (author_user_id, folder_id, updated_at);

