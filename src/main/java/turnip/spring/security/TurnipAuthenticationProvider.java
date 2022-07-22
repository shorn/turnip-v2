package turnip.spring.security;

import com.auth0.jwk.JwkProvider;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.spring.security.api.JwtAuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import turnip.util.Log;

import static turnip.util.Log.to;
import static turnip.util.StringUtil.isBlank;

/** 
Expects that the Auth0 Pipeline has a custom rule to populate the 
custom turnip_email claim.  
*/
public class TurnipAuthenticationProvider extends JwtAuthenticationProvider {
  public static final String CLAIM_TURNIP_EMAIL = "http://turnip_email";
  private static Log log = to(TurnipAuthenticationProvider.class);

  public TurnipAuthenticationProvider(
    JwkProvider jwkProvider, String issuer, String audience
  ) {
    super(jwkProvider, issuer, audience);
  }

  @Override
  public Authentication authenticate(Authentication authentication) 
  throws AuthenticationException {
    Authentication postAuth = super.authenticate(authentication);
    if( postAuth == null ){
      return null;
    }

    String turnipEmail = ((DecodedJWT) postAuth.getDetails()).
      getClaim(CLAIM_TURNIP_EMAIL).asString();
    if( isBlank(turnipEmail) ){
      log.msg("JWT received does not contain the turnip_email claim").
        with("authentication", authentication).warn();
      throw new BadCredentialsException(
        "Authentication did not contain a turnip claim");
    }

    return new TurnipAuthn(turnipEmail, postAuth);
  }
}
