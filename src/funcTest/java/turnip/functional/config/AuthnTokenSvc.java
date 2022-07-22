package turnip.functional.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import turnip.util.Guard;
import turnip.util.Log;

import javax.annotation.PostConstruct;

import static java.lang.String.format;
import static turnip.util.Log.to;
import static turnip.util.RestUtil.createEntityWithBearer;

/** Utility service for talking to Auth0 to get authn tokens for test users */
@Component
public class AuthnTokenSvc {
  protected Log log = to(AuthnTokenSvc.class);
  
  // authn tokens for the users that must be manually created 
  // in Auth0 before running the tests.
  private String user;
  private String admin;
  private String nonUser;

  @Autowired private FuncTestProps props;
  @Autowired private RestTemplate rest;

  /** Doing this eagerly, the execution time is not counted against 
   whatever test happens to run first.  Also, given Auth0 has aggressive
   usage limits - we don't want to do this many times.
   BUT - that means if it fails it's hard to tell what's happening!
   Caused me isssues when picking up the project again.
   */
  @PostConstruct
  public void setup(){
    authenticatePreExistingUsers();
  }
  
  public void authenticatePreExistingUsers(){
    log.with("domain", props.auth0TenantDomain).
      with("audience", props.auth0Audience).
      with("client_id", props.auth0ClientId).
      info("authenticating functional test users");
    Guard.hasValue("auth0TenantDomain must be set", props.auth0TenantDomain);
    Guard.hasValue("auth0Audience must be set", props.auth0Audience);
    Guard.hasValue("auth0ClientId must be set", props.auth0ClientId);
    Guard.hasValue("auth0ClientSecret must be set",
      props.auth0ClientSecret);
    Guard.hasValue("sharedPassword must be set", props.sharedPassword);

    log.with("userEmail", props.userEmail).info("load user authn token");
    user = authenticateUser(props.userEmail);

    log.with("userEmail", props.adminEmail).info("load admin authn token");
    admin = authenticateUser(props.adminEmail);

    log.with("userEmail", props.nonUserEmail).info("load nonUser authn token");
    nonUser = authenticateUser(props.nonUserEmail);

    log.info("warm up the API server");
    HttpEntity<String> entity = createEntityWithBearer(admin);
    rest.exchange(turnipApiServerUrl("/api/warmup"),
      HttpMethod.GET, entity, String.class);
  }

  public String turnipApiServerUrl(String url){
    return format("%s://%s%s",
      props.turnipApiProtocol, props.turnipApiServer, url );
  }
  
  /**
   A user with the given email, using the sharedPassword, is expected to already
   have been created for the test client/audience.
   Don't share this audience with production, keep them separate.
   In order for this to work, the Auth0 tenant settings must have the default
   realm set ot the connection that contains the users (i.e. 
   functional-test-realm).
   */
  public String authenticateUser(String email) {
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    var map = new LinkedMultiValueMap<String, String>();
    map.add("username", email);
    map.add("password", props.sharedPassword);
    map.add("audience", props.auth0Audience);
    map.add("client_id", props.auth0ClientId);
    map.add("client_secret", props.auth0ClientSecret);
    map.add("grant_type", "password");
    map.add("scope", "openid email");

    var request = new HttpEntity<MultiValueMap<String, String>>(map, headers);

    ResponseEntity<Auth0AuthToken> response = null;
    try {
      response = rest.postForEntity(
        format("https://%s/oauth/token", props.auth0TenantDomain),
        request, Auth0AuthToken.class);
    }
    catch( HttpClientErrorException.TooManyRequests ex ){
      log.with("headers", ex.getResponseHeaders()).
        error("post to Auth0 /oauth/token returned TooManyRequests");
      throw ex;
    }

    return response.getBody().access_token;
  }

  public String getUser() {
    return user;
  }

  public String getAdmin() {
    return admin;
  }

  public String getNonUser() {
    return nonUser;
  }

  static record Auth0AuthToken(
    String token_type, String scope, String access_token
  ) {
  }

}
