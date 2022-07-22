package turnip.spring.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.StringJoiner;

public class TurnipAuthn implements Authentication {
  private final String turnipEmail;
  private final Authentication auth0Token;

  public TurnipAuthn(
    String turnipEmail,
    Authentication auth0Token
  ) {
    this.turnipEmail = turnipEmail;
    this.auth0Token = auth0Token;
  }

  @Override
  public String getName() {
    return turnipEmail;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return auth0Token.getAuthorities();
  }

  @Override
  public Object getCredentials() {
    return auth0Token.getCredentials();
  }

  @Override
  public DecodedJWT getDetails() {
    return (DecodedJWT) auth0Token.getDetails();
  }

  @Override
  public String getPrincipal() {
    return turnipEmail;
  }

  @Override
  public boolean isAuthenticated() {
    return true;
  }

  @Override
  public void setAuthenticated(boolean isAuthenticated)
  throws IllegalArgumentException {
    auth0Token.setAuthenticated(isAuthenticated);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ",
      TurnipAuthn.class.getSimpleName() + "[",
      "]")
      .add("turnipEmail='" + turnipEmail + "'")
      .toString();
  }
}
