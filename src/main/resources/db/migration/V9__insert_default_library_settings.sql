INSERT INTO library_settings (setting_id, opening_time, closing_time, timezone)
VALUES (1, TIME '08:00', TIME '00:00', 'Asia/Kolkata')
ON CONFLICT (setting_id) DO NOTHING;
