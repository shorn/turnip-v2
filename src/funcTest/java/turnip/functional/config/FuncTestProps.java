package turnip.functional.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FuncTestProps {
  /**
   Tenant settings must have "default directory" set to whatever connection
   you've created the test users in (e.g. `functional-test-realm`)
   */
  @Value("${funcTestAuth0TenantDomain:}")
  public String auth0TenantDomain;

  /**
   Comes from the Auth0 API's "API Audience" field.
   Note that the API must be authorised for the "application" (identified
   below as the Auth0ClientId
   */
  @Value("${funcTestAuth0Audience:}")
  public String auth0Audience;

  /**
   Comes from the Auth0 Application's "Client ID" field.
   - the application must be authorised for the API used as the Auth0Audience
   - the application must have password grant enabled (under advanced settings)
   */
  @Value("${funcTestAuth0ClientId:}")
  public String auth0ClientId;

  /**
   Comes from the Auth0 Application's "Client Secret" field.
   Set this in your ~/cconfig/turnup/functest.properties file, see
   {@link FunctionalTestConfig}
   */
  @Value("${funcTestAuth0ClientSecret:}")
  public String auth0ClientSecret;

  /**
   These are just manually created in the Auth0 UI, making sure to put
   them in the connection that is configred as default for the tenant.
   */
  @Value("${funcTestUserEmail:turnip-test-user@example.com}")
  public String userEmail;

  @Value("${funcTestAdminEmail:turnip-test-admin@example.com}")
  public String adminEmail;

  @Value("${funcTestNonUserEmail:turnip-test-notauser@example.com}")
  public String nonUserEmail;

  /**
   Makes my life easier to share the same password for all test users.
   */
  @Value("${funcTestSharedPassword:}")
  public String sharedPassword;

  /**
   This is the location of the turnip API server to test against
   */
  @Value("${funcTestTurnipApiServer:localhost:8080}")
  public String turnipApiServer;

  /**
   HTTP is opk for localhost
   */
  @Value("${funcTestTurnipApiProtocol:http}")
  public String turnipApiProtocol;
}
