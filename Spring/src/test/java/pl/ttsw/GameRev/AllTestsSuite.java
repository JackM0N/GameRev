package pl.ttsw.GameRev;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
		AuthenticationServiceTest.class,
		//AuthenticationServiceIntegrationTest.class,
		WebsiteUserServiceTest.class,
		//WebsiteUserServiceIntegrationTest.class,
		GameServiceTest.class,
		//GameServiceIntegrationTest.class,
		UserReviewServiceTest.class,
		//UserReviewServiceIntegrationTest.class,
		RatingServiceTest.class,
		//RatingServiceIntegrationTest.class,
		// TODO: GreenMail fails to start for suite, works if ran from single test class
})
public class AllTestsSuite {
	// yes, this should be empty
}