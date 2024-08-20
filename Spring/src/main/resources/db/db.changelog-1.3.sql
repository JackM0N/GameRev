--liquibase formatted sql

--changeset Stanislaw|Przemyslaw:3 labels:expansion,tables
CREATE TABLE club
(
    club_id     BIGINT PRIMARY KEY GENERATED BY DEFAULT AS IDENTITY,
    name        VARCHAR(100) NOT NULL,
    description TEXT,
    picture     VARCHAR(255),
    admin_id    BIGINT       NOT NULL
);

CREATE TABLE club_member
(
    club_member_id BIGINT PRIMARY KEY GENERATED BY DEFAULT AS IDENTITY,
    club_id        BIGINT  NOT NULL,
    user_id        BIGINT  NOT NULL,
    club_nickname  VARCHAR(30),
    got_accepted   BOOLEAN,
    got_banned     BOOLEAN NOT NULL,
    FOREIGN KEY (club_id) REFERENCES club (club_id),
    FOREIGN KEY (user_id) REFERENCES website_user (user_id)
);

CREATE TABLE club_post
(
    club_post_id BIGINT PRIMARY KEY GENERATED BY DEFAULT AS IDENTITY,
    club_id      BIGINT    NOT NULL,
    author_id    BIGINT    NOT NULL,
    content      TEXT      NOT NULL,
    post_date    TIMESTAMP NOT NULL,
    picture      VARCHAR(255),
    FOREIGN KEY (club_id) REFERENCES club (club_id),
    FOREIGN KEY (author_id) REFERENCES club_member (club_member_id)
);

CREATE TABLE club_comment
(
    club_comment_id BIGINT PRIMARY KEY GENERATED BY DEFAULT AS IDENTITY,
    club_post_id    BIGINT    NOT NULL,
    author_id       BIGINT    NOT NULL,
    content         TEXT      NOT NULL,
    post_date       TIMESTAMP NOT NULL,
    FOREIGN KEY (club_post_id) REFERENCES club_post (club_post_id),
    FOREIGN KEY (author_id) REFERENCES club_member (club_member_id)
);


CREATE TABLE forum
(
    forum_id BIGINT PRIMARY KEY GENERATED BY DEFAULT AS IDENTITY,
    game_id  BIGINT NOT NULL,
    FOREIGN KEY (game_id) REFERENCES game (game_id)
);

CREATE TABLE forum_post
(
    forum_post_id BIGINT PRIMARY KEY GENERATED BY DEFAULT AS IDENTITY,
    forum_id      BIGINT    NOT NULL,
    author_id     BIGINT    NOT NULL,
    content       TEXT      NOT NULL,
    post_date     TIMESTAMP NOT NULL,
    picture       VARCHAR(255),
    FOREIGN KEY (forum_id) REFERENCES forum (forum_id),
    FOREIGN KEY (author_id) REFERENCES website_user (user_id)
);

CREATE TABLE forum_comment
(
    forum_comment_id BIGINT PRIMARY KEY GENERATED BY DEFAULT AS IDENTITY,
    forum_post_id    BIGINT    NOT NULL,
    author_id        BIGINT    NOT NULL,
    content          TEXT      NOT NULL,
    post_date        TIMESTAMP NOT NULL,
    FOREIGN KEY (forum_post_id) REFERENCES forum_post (forum_post_id),
    FOREIGN KEY (author_id) REFERENCES website_user (user_id)
);


ALTER TABLE club
    ADD CONSTRAINT fk_club_admin FOREIGN KEY (admin_id) REFERENCES website_user (user_id);
ALTER TABLE club_member
    ADD CONSTRAINT unique_club_membership UNIQUE (club_id, user_id);


--changeset Stanislaw:23 labels:expansion,data
--club
INSERT INTO club (name, description, picture, admin_id)
VALUES ('Limbus Enthusiasts', 'A club for fans of Limbus Company to discuss event strategies.', NULL, 4);

INSERT INTO club_member (club_id, user_id, club_nickname, got_accepted, got_banned)
VALUES (1, 4, NULL, true, false),
       (1, 5, 'ZweiClair', true, false);

INSERT INTO club_post (club_id, author_id, content, post_date, picture)
VALUES (1, 1, 'Just completed the RefractionRailway! Took me 98 turns, got lucky with last boss second phase.', '2024-08-12 10:30:00', NULL);

INSERT INTO club_comment (club_post_id, author_id, content, post_date) VALUES
    (1, 2, 'Nice setup! I’m thinking of trying a similar team.', '2024-08-12 12:00:00');

--forum
INSERT INTO forum (forum_id, game_id)
VALUES (1, 1);

INSERT INTO forum_post (forum_id, author_id, content, post_date, picture)
VALUES (1, 5, 'What do you think about the latest update?', '2024-08-13 09:45:00', NULL);

INSERT INTO forum_comment (forum_post_id, author_id, content, post_date)
VALUES (1, 6, 'I think it’s a great, especially with the new characters!', '2024-08-13 10:15:00');

INSERT INTO forum_comment (forum_post_id, author_id, content, post_date)
VALUES (1, 4, 'Meh, was hoping for farmwatch ID', '2024-08-13 10:16:00');


--changeset Stanislaw:24 labels:expansion,schema
ALTER TABLE forum
    ADD COLUMN forum_name VARCHAR(100),
    ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN parent_forum_id BIGINT,
    ADD FOREIGN KEY (parent_forum_id) REFERENCES forum (forum_id);

UPDATE forum
SET forum_name = game.title
FROM game
WHERE forum.game_id = game.game_id;

ALTER TABLE forum
    ALTER COLUMN game_id DROP NOT NULL,
    ALTER COLUMN forum_name SET NOT NULL;

ALTER TABLE forum
    ADD CONSTRAINT unique_forum_name UNIQUE (forum_name);


CREATE TABLE forum_moderator
(
    forum_moderator_id BIGINT PRIMARY KEY GENERATED BY DEFAULT AS IDENTITY,
    forum_id           BIGINT NOT NULL,
    moderator_id       BIGINT NOT NULL,
    FOREIGN KEY (forum_id) REFERENCES forum (forum_id),
    FOREIGN KEY (moderator_id) REFERENCES website_user (user_id)
);

ALTER TABLE forum_moderator
    ADD CONSTRAINT unique_forum_moderator UNIQUE (forum_id, moderator_id);

--proper forum hierarchy, missing check for circular reference bc liquibase cant parse trigger with declare
ALTER TABLE forum
    ADD CONSTRAINT check_self_parent
        CHECK (forum_id IS DISTINCT FROM parent_forum_id);


--changeset Stanislaw:25 labels:expansion,schema
--add title
ALTER TABLE club_post
    ADD COLUMN title VARCHAR(150) NOT NULL DEFAULT 'MISSING TITLE';

ALTER TABLE forum_post
    ADD COLUMN title VARCHAR(150) NOT NULL DEFAULT 'MISSING TITLE';

ALTER TABLE club_post
    ALTER COLUMN title DROP DEFAULT;

ALTER TABLE forum_post
    ALTER COLUMN title DROP DEFAULT;

--add last_response_date to show if post had replies since user last checked
ALTER TABLE forum_post
    ADD COLUMN last_response_date TIMESTAMP;

ALTER TABLE club_post
    ADD COLUMN last_response_date TIMESTAMP;

--update old posts
UPDATE forum_post fp
SET last_response_date = (SELECT MAX(fc.post_date)
                          FROM forum_comment fc
                          WHERE fc.forum_post_id = fp.forum_post_id)
WHERE EXISTS (SELECT 1
              FROM forum_comment fc
              WHERE fc.forum_post_id = fp.forum_post_id);

UPDATE club_post cp
SET last_response_date = (SELECT MAX(cc.post_date)
                          FROM club_comment cc
                          WHERE cc.club_post_id = cp.club_post_id)
WHERE EXISTS (SELECT 1
              FROM club_comment cc
              WHERE cc.club_comment_id = cp.club_post_id);

--trigger for new posts in forums
CREATE OR REPLACE FUNCTION update_forum_post_last_response_date()
    RETURNS TRIGGER AS
'
    BEGIN
        UPDATE forum_post
        SET last_response_date = NEW.post_date
        WHERE forum_post_id = NEW.forum_post_id;

        RETURN NEW;
    END;
' LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_forum_post_last_response_date
    AFTER INSERT
    ON forum_comment
    FOR EACH ROW
EXECUTE FUNCTION update_forum_post_last_response_date();

--trigger for new posts in clubs
CREATE OR REPLACE FUNCTION update_club_post_last_response_date()
    RETURNS TRIGGER AS
'
    BEGIN
        UPDATE club_post
        SET last_response_date = NEW.post_date
        WHERE club_post_id = NEW.club_post_id;

        RETURN NEW;
    END;
' LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_club_post_last_response_date
    AFTER INSERT
    ON club_comment
    FOR EACH ROW
EXECUTE FUNCTION update_club_post_last_response_date();


--changeset Stanislaw:26 labels:expansion,data
INSERT INTO forum (forum_id, forum_name) --should be autoincrement, but liquibase cant count, works ok form dbeaver
VALUES (2, 'General');

UPDATE forum
SET parent_forum_id = 2
WHERE forum_id = 1;

INSERT INTO forum (forum_id, forum_name, parent_forum_id, game_id)
VALUES (3, 'Mirror Dungeon', 1, 1);

INSERT INTO forum_post(forum_id, author_id, title, content, post_date)
VALUES (3, 4, 'Standard mode strategies', 'Here you can post your teams, suggested gifts and other tips that will help new players solve standard mode.', '2024-08-14 11:16:23');

INSERT INTO forum_post(forum_id, author_id, title, content, post_date)
VALUES (3, 4, 'Hard mode strategies', 'Here you can post your teams, suggested gifts and other tips that will help new players solve hard mode.', '2024-08-14 11:17:23');

INSERT INTO forum_comment(forum_post_id, author_id, content, post_date)
VALUES (2, 5, 'Just take whatever strongest IDs you have and Faust`s base EGO for sanity healing, should be enough.', '2024-08-14 12:24:52');

INSERT INTO forum_comment(forum_post_id, author_id, content, post_date)
VALUES (2, 6, 'If you have more IDs, you can try forming teams around Burn, Charge, etc, then each Identity will synergize with others. ', '2024-08-14 12:24:52');

--moderators: 4 can manage all, 5 only subforum
INSERT INTO forum_moderator (forum_id, moderator_id)
VALUES (1, 4),
       (2, 4),
       (3, 4),
       (3, 5);

UPDATE forum_post
SET title = 'Update is finally here!'
WHERE title = 'MISSING TITLE';

UPDATE club_post
SET title = 'This new RR is great!'
WHERE title = 'MISSING TITLE';


--changeset Stanislaw:27 labels:expansion,schema
--trigger to delete all child forums
CREATE OR REPLACE FUNCTION soft_delete_child_forums()
    RETURNS TRIGGER AS
'
    BEGIN
        UPDATE forum
        SET is_deleted = TRUE
        WHERE parent_forum_id = OLD.forum_id;
        RETURN NEW;
    END;
' LANGUAGE plpgsql;

CREATE TRIGGER trigger_forum_soft_delete
    BEFORE UPDATE OF is_deleted
    ON forum
    FOR EACH ROW
    WHEN (NEW.is_deleted = TRUE)
EXECUTE FUNCTION soft_delete_child_forums();


--changeset Stanislaw:28 labels:expansion,data
-- Forum Posts
INSERT INTO forum_post(forum_id, author_id, title, content, post_date, picture, last_response_date)
VALUES
    (2, 6, 'Looking for recommendations', 'Ive recently finished Elden Ring, it was fun. Im looking for something similar but with a different setting. Any suggestions?', '2024-08-14 11:16:23', NULL, '2024-08-16 09:30:45'),
    (2, 5, 'Best strategy games of 2024?', 'Ive been into strategy games lately and was wondering what everyones favorite from this year is?', '2024-08-12 13:45:12', NULL, '2024-08-14 18:22:10'),
    (2, 4, 'Favorite Indie Games?', 'I love discovering hidden indie gems. What are some of your favorite indie games of all time?', '2024-08-15 09:55:45', NULL, '2024-08-15 11:25:32');

-- Forum Comments
INSERT INTO forum_comment(forum_post_id, author_id, content, post_date)
VALUES
    (4, 5, 'You should try Sekiro: Shadows Die Twice if you havent already. It has a different setting but the same challenging combat.', '2024-08-14 13:22:50'),
    (4, 4, 'If you want something a bit different, maybe check out Dark Souls 3 or Bloodborne. They have a similar vibe.', '2024-08-15 08:14:29'),
    (5, 6, 'For me, its definitely Age of Empires IV. The new expansions have really kept it fresh.', '2024-08-12 16:05:33'),
    (5, 4, 'Im really enjoying Company of Heroes 3. Its a solid blend of tactics and action.', '2024-08-13 10:30:15');


--changeset Stanislaw:29 labels:expansion,data
BEGIN;

ALTER TABLE forum DROP CONSTRAINT check_self_parent;
ALTER TABLE forum_post DROP CONSTRAINT forum_post_forum_id_fkey;
ALTER TABLE forum DROP CONSTRAINT forum_parent_forum_id_fkey;
ALTER TABLE forum_moderator DROP CONSTRAINT forum_moderator_forum_id_fkey;

UPDATE forum SET forum_id = -999 WHERE forum_id = 1;
UPDATE forum SET forum_id = 1 WHERE forum_id = 2;
UPDATE forum SET forum_id = 2 WHERE forum_id = -999;

UPDATE forum SET parent_forum_id = -999 WHERE parent_forum_id = 1;
UPDATE forum SET parent_forum_id = 1 WHERE parent_forum_id = 2;
UPDATE forum SET parent_forum_id = 2 WHERE parent_forum_id = -999;

UPDATE forum_post SET forum_id = -999 WHERE forum_id = 1;
UPDATE forum_post SET forum_id = 1 WHERE forum_id = 2;
UPDATE forum_post SET forum_id = 2 WHERE forum_id = -999;

UPDATE forum_moderator SET forum_id = -999 WHERE forum_id = 1;
UPDATE forum_moderator SET forum_id = 1 WHERE forum_id = 2;
UPDATE forum_moderator SET forum_id = 2 WHERE forum_id = -999;

ALTER TABLE forum_post ADD CONSTRAINT forum_post_forum_id_fkey FOREIGN KEY (forum_id) REFERENCES forum (forum_id);
ALTER TABLE forum ADD CONSTRAINT forum_parent_forum_id_fkey FOREIGN KEY (parent_forum_id) REFERENCES forum (forum_id);
ALTER TABLE forum_moderator ADD CONSTRAINT forum_moderator_forum_id_fkey FOREIGN KEY (forum_id) REFERENCES forum (forum_id);
ALTER TABLE forum
    ADD CONSTRAINT check_self_parent
        CHECK (forum_id IS DISTINCT FROM parent_forum_id);

COMMIT;

