package pl.ttsw.GameRev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.ttsw.GameRev.model.Forum;
import java.util.List;

@Repository
public interface ForumRepository extends JpaRepository<Forum, Long>, JpaSpecificationExecutor<Forum> {
    @Query(value = "WITH RankedPosts AS ( " +
            "  SELECT f.forum_id, f.forum_name, fp.forum_post_id, fp.title, fc.post_date AS last_response_date, wu.nickname, " +
            "         ROW_NUMBER() OVER ( " +
            "           PARTITION BY f.forum_name " +
            "           ORDER BY " +
            "             CASE WHEN fc.post_date IS NULL THEN 1 ELSE 0 END, " +
            "             fc.post_date DESC " +
            "         ) AS rn " +
            "  FROM forum_post fp " +
            "  INNER JOIN forum f ON f.forum_id = fp.forum_id " +
            "  LEFT JOIN forum_comment fc ON fc.forum_post_id = fp.forum_post_id " +
            "  LEFT JOIN website_user wu ON fc.author_id = wu.user_id " +
            "  WHERE f.forum_id = :forumId " +
            ") " +
            "SELECT forum_id, forum_name, forum_post_id, title, last_response_date, nickname " +
            "FROM RankedPosts " +
            "WHERE rn = 1",
            nativeQuery = true)
    String findTopPostForForum(@Param("forumId") Long forumId);
    boolean existsForumByForumName(String forumName);
    List<Forum> findByGameId(Long gameId);
}
