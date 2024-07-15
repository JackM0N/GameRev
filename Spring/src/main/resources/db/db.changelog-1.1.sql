--liquibase formatted sql

--changeset Stanislaw:8 labels:schema,fix
ALTER TABLE game
    ADD COLUMN users_score DECIMAL(3,2);
ALTER TABLE user_review
    ADD COLUMN positive_rating INT;
ALTER TABLE user_review
    ADD COLUMN negative_rating INT;

UPDATE user_review ur
SET positive_rating = (
    SELECT COUNT(*)
    FROM rating r
    WHERE r.user_review_id = ur.user_review_id
      AND r.is_positive = true
),
    negative_rating = (
        SELECT COUNT(*)
        FROM rating r
        WHERE r.user_review_id = ur.user_review_id
          AND r.is_positive = false
    );

UPDATE game g
SET users_score = (
    SELECT AVG(ur.score)
    FROM user_review ur
    WHERE ur.game_id = g.game_id
);