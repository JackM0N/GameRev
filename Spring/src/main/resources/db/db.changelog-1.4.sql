--liquibase formatted sql

--changeset Maja:1 labels:schema,fix
--trigger for when a comment gets deleted to update last response date
CREATE OR REPLACE FUNCTION update_forum_post_last_response_date_on_delete()
    RETURNS TRIGGER AS
'
    BEGIN
        UPDATE forum_post
        SET last_response_date = (
            SELECT MAX(post_date)
            FROM forum_comment
            WHERE forum_post_id = OLD.forum_post_id AND is_deleted = FALSE AND post_date != OLD.post_date
        )
        WHERE forum_post_id = OLD.forum_post_id;

        RETURN OLD;
    END;
' LANGUAGE plpgsql;


CREATE TRIGGER trigger_update_forum_post_last_response_date
    AFTER DELETE
    ON forum_comment
    FOR EACH ROW
    EXECUTE FUNCTION update_forum_post_last_response_date_on_delete();

