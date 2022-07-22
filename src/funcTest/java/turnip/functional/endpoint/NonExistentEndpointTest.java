package turnip.functional.endpoint;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import turnip.functional.FunctionalTestCase;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NonExistentEndpointTest extends FunctionalTestCase {

  @Test
  public void nonExistentEndpointShould404() {
    try {
      get(token.getUser(), "/api/doesn-not-exist", String.class);
    }
    catch( HttpClientErrorException e ){
      assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
    }

  }

}

