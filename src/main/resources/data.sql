-- Sample TBR items to demo each category. Loaded only in dev (H2).
INSERT INTO tbr_item (title, creator, category, notes, added_at, completed) VALUES
('The Halcyon Years', 'Alastair Reynolds', 'BOOK', 'Picked up after the Pushing Ice reread.', CURRENT_TIMESTAMP, false),
('The Iron Garden Sutra', 'A.D. Sui', 'BOOK', 'Strange Horizons reviewed it favorably.', CURRENT_TIMESTAMP, false),
('Annihilation', 'Jeff VanderMeer', 'BOOK', 'Reread before season finale.', CURRENT_TIMESTAMP, true),

('Sinners', 'Ryan Coogler', 'MOVIE', 'On the watchlist after critics raved.', CURRENT_TIMESTAMP, false),
('The Substance', 'Coralie Fargeat', 'MOVIE', 'Body horror, supposedly transgressive.', CURRENT_TIMESTAMP, false),

('Sundial', 'Caribou', 'MUSIC', 'Pitchfork best new music.', CURRENT_TIMESTAMP, false),
('Rumours Live', 'Fleetwood Mac', 'MUSIC', 'Disco-adjacent rock canon.', CURRENT_TIMESTAMP, true);
