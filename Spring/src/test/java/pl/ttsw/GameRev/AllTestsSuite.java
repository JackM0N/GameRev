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
})
public class AllTestsSuite {
	// yes, this should be empty
}