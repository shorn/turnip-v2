package turnip.functional.endpoint;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import turnip.endpoint.MiscAdmin.AddUserRequest;
import turnip.endpoint.MiscAdmin.ListUsersResult;
import turnip.functional.FunctionalTestCase;
import turnip.functional.spring.bean.UserManager;
import turnip.service.UserSvc.UserInfo;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static turnip.service.AuthzSvc.USER_ROLE;
import static turnip.unit.util.BDD.EXPECT;
import static turnip.unit.util.BDD.GIVEN;
import static turnip.unit.util.BDD.THEN;
import static turnip.unit.util.BDD.WHEN;

/**
The actual application logic doesn't really make sense because it's not a real
app.  The current endpoints just exist so I can demonstrate GET/POST requests. 
 */
public class MiscAdminEndpointTest extends FunctionalTestCase {

  @Autowired private UserManager userManager;
  
  @Test
  public void addUserFlowShouldWork(TestInfo testInfo){
    GIVEN("non-admin user exists");
    var userInfo = get(token.getUser(), "/api/user-info", UserInfo.class);
    assertThat(userInfo.email()).isEqualTo(props.userEmail);

    
    EXPECT("non-admin user can't call /list-users because not authorized");
    assertThatExceptionOfType(HttpClientErrorException.class).isThrownBy(()->{
      get(token.getUser(), "/api/list-users", ListUsersResult.class);
    }).satisfies(e->{
      assertThat(e.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    });


    // will start failing (possibly intermittently) once result is paginated
    EXPECT("/list-users should return empty for non-existent user");
    String newUserEmail = userManager.formatNewUserEmail(
      getTestPrefix(testInfo) );
    assertThat(
      get(token.getAdmin(), "/api/list-users", ListUsersResult.class).emails()
    ).isNotNull().asList().doesNotContain(newUserEmail);
    
    
    WHEN("new user is added");
    UserInfo newUser = post(token.getAdmin(), "/api/add-user", 
      new AddUserRequest(newUserEmail, USER_ROLE), UserInfo.class);
    THEN("/list-users should return the new user");
    assertThat(
      get(token.getAdmin(), "/api/list-users", ListUsersResult.class).emails()
    ).isNotNull().asList().contains(newUserEmail);
  }
}
