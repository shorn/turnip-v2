package turnip.spring.config;

import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.spring.security.api.BearerSecurityContextRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import turnip.spring.security.TurnipAuthenticationProvider;
import turnip.util.Guard;
import turnip.util.Log;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;
import static turnip.util.Log.to;

@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
  public static final String API = "/api";
  public static final String PUBLIC = "/public";
  public static final String AUDIENCE_PROP_NAME = "auth0.audience";

  private static final Log log = to(WebSecurityConfig.class);

  /** The "audience of the JWT" - i.e. the target system (i.e. this 
   server). */
  private final String audience;
  /** The "issuer of the JWT" - i.e. the system that issued the JWT to the 
   bearer. */
  private final String issuer;
  private final long leeway;

  public WebSecurityConfig(
    @Value("${" + AUDIENCE_PROP_NAME + ":https://localhost:8080}") String audience, 
    @Value("${auth0.issuer:https://rabbit-turnip.us.auth0.com/}") String issuer,
    @Value("${auth0.leeway:2000}") long leeway
  ) {
    log.with("audience", audience).with("issuer", issuer).
      with("leeway", leeway).info("init");
    Guard.hasValue(AUDIENCE_PROP_NAME + " must be set", audience);
    Guard.hasValue("auth0.issuer must be set", issuer);
    this.audience = audience;
    this.issuer = issuer;
    this.leeway = leeway;
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    final JwkProvider jwkProvider = new JwkProviderBuilder(issuer).build();

    http.
      authenticationProvider(
        new TurnipAuthenticationProvider(jwkProvider, issuer, audience).
          // Otherwise - InvalidClaimException "Token can't be used before ..." 
          withJwtVerifierLeeway(leeway) ).
      securityContext().
        securityContextRepository(new BearerSecurityContextRepository()).
      and().
        exceptionHandling().
      and().authorizeRequests().
        mvcMatchers(API + "/**").fullyAuthenticated().
        mvcMatchers(PUBLIC + "/**").permitAll().
        anyRequest().denyAll().
      and().
        httpBasic().disable().
        csrf().disable().
        sessionManagement().sessionCreationPolicy(STATELESS);
  }

}
