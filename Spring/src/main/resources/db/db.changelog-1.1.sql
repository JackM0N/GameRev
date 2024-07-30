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


--changeset Stanislaw:9 labels:triggers
--trigger updating users_score in game on user_review CRUD changes
CREATE OR REPLACE FUNCTION update_users_score()
    RETURNS TRIGGER AS '
    BEGIN
        IF TG_OP = ''INSERT'' OR TG_OP = ''DELETE'' OR OLD.score IS DISTINCT FROM NEW.score THEN
            UPDATE game g
            SET users_score = COALESCE((SELECT AVG(ur.score)
                                        FROM user_review ur
                                        WHERE ur.game_id = OLD.game_id OR ur.game_id = NEW.game_id), 0)
            WHERE g.game_id = OLD.game_id OR g.game_id = NEW.game_id;
        END IF;

        RETURN NEW;
    END;
' LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_users_score
    AFTER INSERT OR UPDATE OR DELETE ON user_review
    FOR EACH ROW
EXECUTE FUNCTION update_users_score();


--trigger updating positive_ and negative_rating in user_review on rating CRUD changes
CREATE OR REPLACE FUNCTION update_user_review_ratings()
    RETURNS TRIGGER AS '
BEGIN
    UPDATE user_review ur
    SET
        positive_rating = (
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
        )
    WHERE ur.user_review_id = OLD.user_review_id OR ur.user_review_id = NEW.user_review_id;

    RETURN NEW;
END;
' LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_user_review_ratings
    AFTER INSERT OR UPDATE OR DELETE ON rating
    FOR EACH ROW
EXECUTE FUNCTION update_user_review_ratings();



--changeset Stanislaw:10 labels:schema,fix
ALTER TABLE website_user
    ADD COLUMN current_token TEXT;



--changeset Stanislaw:11 labels:schema,fix
ALTER TABLE website_user
    DROP COLUMN current_token;


--changeset Stanislaw:12 labels:schema,fix
ALTER TABLE report
    ADD COLUMN content TEXT NOT NULL default 'Old report that was created before reason was possible to give'


--changeset Stanislaw:13 labels:schema,fix
ALTER TABLE game
    ADD CONSTRAINT unique_game_title UNIQUE (title);


--changeset Stanislaw:14 labels:schema,fix
ALTER TABLE game
    ALTER COLUMN users_score TYPE DECIMAL(4,2);

