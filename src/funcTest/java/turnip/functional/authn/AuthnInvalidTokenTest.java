package turnip.functional.authn;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException.Forbidden;
import turnip.endpoint.MiscAdmin;
import turnip.functional.FunctionalTestCase;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static turnip.unit.util.BDD.EXPECT;
import static turnip.unit.util.BDD.THEN;
import static turnip.unit.util.BDD.WHEN;

public class AuthnInvalidTokenTest extends FunctionalTestCase {
  @Test public void testInvalidJwtSignature(){
    EXPECT("/list-users can be called with a valid admin token");
    assertThat(
      get(token.getAdmin(), "/api/list-users", MiscAdmin.ListUsersResult.class).emails()
    ).asList().isNotEmpty();

    
    WHEN("/list-users is called with a scam token");
    var components = token.getAdmin().split("\\.");
    var fakeToken = components[0] + "." + components[1] + "." +
      // header and payload are valid, but signature is now invalid
      components[2] + "0";
    THEN("server should reject with a 403");
    assertThatExceptionOfType(Forbidden.class).isThrownBy(()->
      get(fakeToken, "/api/list-users", MiscAdmin.ListUsersResult.class)
    ).satisfies(e->{
      log.info("exception: " + e);
      assertThat(e.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    });
  }
}
