package pl.ttsw.GameRev;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
        AuthenticationServiceTest.class,
        AuthenticationServiceIntegrationTest.class,
        WebsiteUserServiceTest.class,
        WebsiteUserServiceIntegrationTest.class,
        GameServiceTest.class,
        GameServiceIntegrationTest.class,
        UserReviewServiceTest.class,
        UserReviewServiceIntegrationTest.class,
        RatingServiceTest.class,
        RatingServiceIntegrationTest.class,
        UserGameServiceTest.class,
        UserGameServiceIntegrationTest.class,
        ReportServiceTest.class,
        ReportServiceIntegrationTest.class,
        CriticReviewServiceTest.class,
        CriticReviewServiceIntegrationTest.class,
        ForumServiceTest.class,
        ForumServiceIntegrationTest.class,
        ForumPostServiceTest.class,
        ForumPostServiceIntegrationTest.class,
        ForumCommentServiceTest.class,
        ForumCommentServiceIntegrationTest.class,
        ForumRequestServiceTest.class,
        ForumRequestServiceIntegrationTest.class,
})
public class AllTestsSuite {
    // yes, this should be empty
}
