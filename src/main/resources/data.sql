INSERT INTO users(id, username, first_name, last_name, email, password, enabled, profile_visibility)
    VALUES
        ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'ivan01', 'Ivan', 'Ivić', 'blaskovic.ivan1@gmail.com', '$2a$10$/o9PxTASyyvPxy5/6etBeeExHZYBUzkm/7ROcGurq0Z30wyadcAM.', 1, 'PUBLIC'),
        ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'mmarkic', 'Marko', 'Markić', 'blaskovic.ivan1+1@gmail.com', '$2a$10$/o9PxTASyyvPxy5/6etBeeExHZYBUzkm/7ROcGurq0Z30wyadcAM.', 1, 'PUBLIC'),
        ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'pere', 'Pero', 'Perić', 'blaskovic.ivan1+2@gmail.com', '$2a$10$/o9PxTASyyvPxy5/6etBeeExHZYBUzkm/7ROcGurq0Z30wyadcAM.', 1, 'PUBLIC');

INSERT INTO matches(id, player1_id, player2_id, initial_server, visibility, start_date, start_time, is_finished, final_score, player1efficiency, player2efficiency, number_of_likes, number_of_comments)
    VALUES
        ('906c02b7-471c-4aea-a5de-f75114e63fac', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'PUBLIC', '16.06.2025.', '05:33', 1, '2 : 0', 25, 75, 4, 8),
        ('906c02b7-471c-4aea-a5de-f75114e63fa4', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'PUBLIC', '17.06.2025.', '05:33', 1, '2 : 1', 52, 48, 12, 16),
        ('906c02b7-471c-4aea-a5de-f75114e63fad', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'PUBLIC', '16.06.2025.', '06:33', 1, '0 : 2', null, null, 24, 0);

INSERT INTO follow_request(id, followee_id, follower_id, status, timestamp)
    VALUES
        ('906c02b7-471c-4aea-a5de-e75114e63fac', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'ACCEPTED', '2025-06-16 05:33:51.688972');

INSERT INTO clubs(id, name, address, email, phone, description, logo_url, latitude, longitude,
                  reservation_type, average_rating, number_of_reviews, created_at)
    VALUES
        ('b1f0ecc0-0000-4000-8000-000000000001', 'Match Point Club', 'Vukovarska 25, Zagreb',
         'info@matchpoint.hr', '+385 1 555 0100', 'Indoor hard courts in the city center.',
         NULL, 45.7950, 15.9680, 'INSTANT', 4.5, 1, '2026-08-05 10:00:00'),
        ('b1f0ecc0-0000-4000-8000-000000000002', 'Elite Tennis Club', 'Ulica grada Vukovara 100, Zagreb',
         'contact@elitetennis.hr', '+385 1 555 0200', 'Clay and grass courts, pro shop on site.',
         NULL, 45.8000, 15.9900, 'APPROVAL', NULL, 0, '2026-08-05 10:05:00');

INSERT INTO club_members(id, club_id, user_id, role, joined_at)
    VALUES
        ('c1f0ecc0-0000-4000-8000-000000000001', 'b1f0ecc0-0000-4000-8000-000000000001',
         'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'OWNER', '2026-08-05 10:00:00'),
        ('c1f0ecc0-0000-4000-8000-000000000002', 'b1f0ecc0-0000-4000-8000-000000000001',
         'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'ADMIN', '2026-08-05 10:01:00'),
        ('c1f0ecc0-0000-4000-8000-000000000003', 'b1f0ecc0-0000-4000-8000-000000000002',
         'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'OWNER', '2026-08-05 10:05:00');

INSERT INTO club_courts(id, club_id, name, surface_type, price_per_hour)
    VALUES
        ('d1f0ecc0-0000-4000-8000-000000000001', 'b1f0ecc0-0000-4000-8000-000000000001', 'Court 1', 'HARD', 12.00),
        ('d1f0ecc0-0000-4000-8000-000000000002', 'b1f0ecc0-0000-4000-8000-000000000001', 'Court 2', 'HARD', 12.00),
        ('d1f0ecc0-0000-4000-8000-000000000003', 'b1f0ecc0-0000-4000-8000-000000000002', 'Clay A', 'CLAY', 15.00);

INSERT INTO club_reviews(id, club_id, user_id, rating, comment, created_at)
    VALUES
        ('e1f0ecc0-0000-4000-8000-000000000001', 'b1f0ecc0-0000-4000-8000-000000000001',
         'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 5, 'Great courts and friendly staff!', '2026-08-05 10:00:00');

INSERT INTO club_follows(id, club_id, user_id, timestamp)
    VALUES
        ('e1f0ecc0-0000-4000-8000-000000000002', 'b1f0ecc0-0000-4000-8000-000000000001',
         'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', '2026-08-05 10:02:00');

INSERT INTO club_posts(id, club_id, content, image_url, created_at, number_of_likes, number_of_comments)
    VALUES
        ('e1f0ecc0-0000-4000-8000-000000000003', 'b1f0ecc0-0000-4000-8000-000000000001',
         'Summer tournament starts next week! Sign up at the reception.', NULL, '2026-08-05 10:03:00', 1, 1);

INSERT INTO club_post_likes(id, user_id, post_id)
    VALUES
        ('e1f0ecc0-0000-4000-8000-000000000004', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12',
         'e1f0ecc0-0000-4000-8000-000000000003');

INSERT INTO club_post_comments(id, user_id, post_id, comment, created_at)
    VALUES
        ('e1f0ecc0-0000-4000-8000-000000000005', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13',
         'e1f0ecc0-0000-4000-8000-000000000003', 'Count me in!', '2026-08-05 10:04:00');

INSERT INTO court_reservations(id, club_id, court_id, user_id, reservation_date, start_time, end_time, status, created_at)
    VALUES
        ('e1f0ecc0-0000-4000-8000-000000000006', 'b1f0ecc0-0000-4000-8000-000000000001',
         'd1f0ecc0-0000-4000-8000-000000000001', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13',
         '2026-08-06', '10:00', '11:00', 'CONFIRMED', '2026-08-05 10:06:00'),
        ('e1f0ecc0-0000-4000-8000-000000000007', 'b1f0ecc0-0000-4000-8000-000000000002',
         'd1f0ecc0-0000-4000-8000-000000000003', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13',
         '2026-08-06', '15:00', '16:00', 'PENDING', '2026-08-05 10:07:00');