package turnip.functional.endpoint;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import turnip.functional.FunctionalTestCase;
import turnip.functional.spring.bean.UserManager;

import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.springframework.http.HttpMethod.POST;
import static turnip.unit.util.BDD.EXPECT;
import static turnip.util.RestUtil.createEntityWithBearer;

/**
 This test will cause excpetions to show up in the log - they can be ignored.
 They're caused by the test trying to make complicated requests (like 
 POST requests that need a bunch of properly setup data) without any specific
 knowledge of the test.
 It's too expensive to engineer a generic solution to generate correct
 inputs to every endpoint in order to avoid the exceptions.  So you'll see
 the tests generate lots on NullPointerExceptions and other input-related 
 errors.
 <p>
 The test iterates across all endpoints that the spring 
 RequestMappingHandlerMapping knows about and dynamically generates a (very 
 dumb) test that will try to exercise that endpoint.
 */
public class AuthzEndpointTest extends FunctionalTestCase {
  public static final Map<String, String> EMPTY_POST_PARAM = Map.of(
    "requestData", "from AuthzEndpointTest");

  @Autowired private UserManager userManager;
  
  @TestFactory
  public Stream<DynamicTest> allEndpointsShouldBeAuthorized() {
    // see class comment
    log.warn("***NOTE***: warnings and errors are expected below");
    return jettyTestServer.getTurnipApiHandlerMethods().entrySet().stream().
      flatMap((i) -> createEndpointTests(i.getKey(), i.getValue()));
  }
  
  public Stream<DynamicTest> createEndpointTests(
    RequestMappingInfo mapping,
    HandlerMethod handler
  ){
    String path = mapping.getDirectPaths().stream().toList().get(0);
    return mapping.getMethodsCondition().getMethods().stream().map(iMethod-> {
      if( iMethod == RequestMethod.GET ){
        return dynamicTest("GET " + path,
          () -> testGetEndpoint(path, handler));
      }
      
      if( iMethod == RequestMethod.POST ){
        return dynamicTest("POST " + path,
          () -> testPostEndpoint(path, handler));
      }

      throw new UnsupportedOperationException(
        "don't know how to deal with method: " + iMethod);
    });
  }

  /** This basic implementation won't work very well for dynamic path
   structures, but I mostly only use POST requests anyway.
   */
  private void testGetEndpoint(String path, HandlerMethod handler) {
    HttpEntity<Object> entity =
      createEntityWithBearer(token.getNonUser());
    log.msg("testing endpoint").with("method", "GET").with("path", path).info();
    try {
      rest.exchange(turnipApiServerUrl(path), 
        HttpMethod.GET, entity, String.class);
      fail("should not have been able to call GET endpoint: " + path);
    }
    catch( HttpClientErrorException e ){
      assertEquals(HttpStatus.UNAUTHORIZED, e.getStatusCode());
    }
    catch( HttpServerErrorException e ){
      // if the server gets to the point of having an 500 error, then it must
      // not have validated the user
      fail("should not have been able to call GET endpoint: " + path);
    }

    // any GET endpoint should be callable by the admin user
    entity = createEntityWithBearer(token.getAdmin());
    try {
      rest.exchange(turnipApiServerUrl(path), 
        HttpMethod.GET, entity, String.class );
      // if the test gets to here, then the server got past the security 
      // check and actually executed successfully
    }
    catch( HttpServerErrorException e ){
      // this is Ok, if it got to the point of throwing a 500, then it got
      // past any security check
    }
  }

  private void testPostEndpoint(String path, HandlerMethod handler) {
    HttpEntity<Object> entity =
      createEntityWithBearer(token.getNonUser(), EMPTY_POST_PARAM);
    
    EXPECT(path + " - non-user should be rejected as UNAUTHORIZED");
    try {
      rest.exchange(turnipApiServerUrl(path), POST, entity, String.class);
      fail("should not have been able to call POST endpoint: " + path);
    }
    catch( HttpClientErrorException e ){
      assertEquals(HttpStatus.UNAUTHORIZED, e.getStatusCode());
    }
    catch( HttpServerErrorException e ){
      // if the server gets to the point of having an 500 error, then it must
      // not have validated the user
      fail("should not have been able to call POST endpoint: " + path);
    }

    EXPECT(path + " - admin-user should be not be rejected as UNAUTHORIZED");
    // any POST endpoint should be callable by the admin user
    entity = createEntityWithBearer(token.getAdmin(), EMPTY_POST_PARAM);
    try {
      rest.exchange(turnipApiServerUrl(path), POST, entity, String.class);
      // if the test gets to here, then the server got past the security 
      // check and actually executed successfully
    }
    catch( HttpServerErrorException e ){
      // see class comment
      log.warn("***NOTE***: warnings and errors are expected above");
      // this is Ok, if it got to the point of throwing a 500, then it got
      // past any security check
    }
  }
}

