INSERT INTO user_account (id, account, password_hash, created_at, updated_at) VALUES
  (1, 'alice', 'Glj60NqzfJJifqXQj6kkFQ==:U8tbfuaV8HBNxBurmK16iGSOXCeZcDu3m9JL8EPPBMA=', '2026-04-24 10:00:00', '2026-04-24 10:00:00'),
  (2, 'bob', 'MYpTlychWRxuj/taTZCi1A==:+gDF1KMJhwwEH+2rZZcASq7C5A540GyyOFOr5EbepHc=', '2026-04-24 10:05:00', '2026-04-24 10:05:00'),
  (3, 'ally-reader', 'ni/e3GV0cmhgS/aYsLlsZQ==:u6avb+/w+3Fk9iSzAELWfYPqSzuNlcPZABXKwztfN20=', '2026-04-24 10:10:00', '2026-04-24 10:10:00');

INSERT INTO folder (id, owner_user_id, name, display_order, created_at, updated_at) VALUES
  (10, 1, 'Java Basics', 0, '2026-04-24 10:15:00', '2026-04-24 10:15:00'),
  (11, 1, 'Recipes', 1, '2026-04-24 10:16:00', '2026-04-24 10:16:00'),
  (20, 2, 'Travel', 0, '2026-04-24 10:17:00', '2026-04-24 10:17:00');

INSERT INTO note (id, author_user_id, folder_id, title, markdown_content, rendered_html, publication_status, created_at, updated_at) VALUES
  (100, 1, 10, 'Streams', '# Streams' || CHAR(10) || 'Useful stream notes.', '<h1>Streams</h1><p>Useful stream notes.</p>', 'PUBLISHED', '2026-04-24 10:20:00', '2026-04-24 10:20:00'),
  (101, 1, 11, 'Soup', '![bowl](https://example.com/soup.png)', '<p><img src="https://example.com/soup.png" alt="bowl"></p>', 'PUBLISHED', '2026-04-24 10:21:00', '2026-04-24 10:21:00'),
  (200, 2, 20, 'Kyoto', '# Kyoto' || CHAR(10) || 'Read-only travel note.', '<h1>Kyoto</h1><p>Read-only travel note.</p>', 'PUBLISHED', '2026-04-24 10:22:00', '2026-04-24 10:22:00');

