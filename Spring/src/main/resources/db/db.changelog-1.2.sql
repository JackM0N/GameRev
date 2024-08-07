--liquibase formatted sql

--changeset Stanislaw:16 labels:schema,refactoring
--replacing dictionaries for string enums
ALTER TABLE game DROP CONSTRAINT IF EXISTS fk_game_release_status;
ALTER TABLE user_game DROP CONSTRAINT IF EXISTS fk_user_game_completion_status;
ALTER TABLE critic_review DROP CONSTRAINT IF EXISTS fk_critic_review_review_status;

ALTER TABLE game ADD COLUMN release_status_name VARCHAR(255);
UPDATE game SET release_status_name = (SELECT status_name FROM release_status WHERE release_status_id = game.release_status);
ALTER TABLE game DROP COLUMN release_status;

ALTER TABLE user_game ADD COLUMN completion_status_name VARCHAR(255);
UPDATE user_game SET completion_status_name = (SELECT completion_name FROM completion_status WHERE completion_status_id = user_game.completion_status);
ALTER TABLE user_game DROP COLUMN completion_status;

ALTER TABLE critic_review ADD COLUMN review_status_name VARCHAR(255);
UPDATE critic_review SET review_status_name = (SELECT status_name FROM review_status WHERE review_status_id = critic_review.review_status);
ALTER TABLE critic_review DROP COLUMN review_status;

DROP TABLE IF EXISTS release_status;
DROP TABLE IF EXISTS completion_status;
DROP TABLE IF EXISTS review_status;

ALTER TABLE game RENAME COLUMN release_status_name to release_status;
ALTER TABLE user_game RENAME COLUMN completion_status_name to completion_status;
ALTER TABLE critic_review RENAME COLUMN review_status_name to review_status;

ALTER TABLE game ALTER COLUMN release_status SET NOT NULL;
ALTER TABLE user_game ALTER COLUMN completion_status SET NOT NULL;
ALTER TABLE critic_review ALTER COLUMN review_status SET NOT NULL;


--changeset Stanislaw:17 labels:schema,refactoring
UPDATE game SET release_status = UPPER(release_status);
UPDATE user_game SET completion_status = UPPER(completion_status);
UPDATE critic_review SET review_status = UPPER(review_status);


--changeset Stanislaw:18 labels:schema,refactoring
UPDATE game SET release_status = REPLACE(release_status, '-', '_');
UPDATE user_game SET completion_status = REPLACE(completion_status, '-', '_');
UPDATE critic_review SET review_status = REPLACE(review_status, '-', '_');

