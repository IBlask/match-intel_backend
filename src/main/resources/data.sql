INSERT INTO users(id, username, first_name, last_name, email, password, enabled)
    VALUES
        ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'ivan01', 'Ivan', 'Ivić', 'blaskovic.ivan1@gmail.com', '$2a$10$/o9PxTASyyvPxy5/6etBeeExHZYBUzkm/7ROcGurq0Z30wyadcAM.', 1),
        ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'mmarkic', 'Marko', 'Markić', 'blaskovic.ivan1+1@gmail.com', '$2a$10$/o9PxTASyyvPxy5/6etBeeExHZYBUzkm/7ROcGurq0Z30wyadcAM.', 1),
        ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'pere', 'Pero', 'Perić', 'blaskovic.ivan1+2@gmail.com', '$2a$10$/o9PxTASyyvPxy5/6etBeeExHZYBUzkm/7ROcGurq0Z30wyadcAM.', 1);

INSERT INTO matches(id, player1_id, player2_id, initial_server, visibility, start_date, start_time, is_finished, final_score, player1efficiency, player2efficiency, number_of_likes, number_of_comments)
    VALUES
        ('906c02b7-471c-4aea-a5de-f75114e63fac', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'PUBLIC', '16.06.2025.', '05:33', 1, '2 : 0', 25, 75, 4, 8),
        ('906c02b7-471c-4aea-a5de-f75114e63fa4', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'PUBLIC', '17.06.2025.', '05:33', 1, '2 : 1', 52, 48, 12, 16),
        ('906c02b7-471c-4aea-a5de-f75114e63fad', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'PUBLIC', '16.06.2025.', '06:33', 1, '0 : 2', null, null, 24, 0);

INSERT INTO follow_request(id, followee_id, follower_id, status, timestamp)
    VALUES
        ('906c02b7-471c-4aea-a5de-e75114e63fac', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'ACCEPTED', '2025-06-16 05:33:51.688972');