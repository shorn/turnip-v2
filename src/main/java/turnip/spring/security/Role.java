package turnip.spring.security;

import turnip.util.ExceptionUtil;
import turnip.util.Guard;

public enum Role {
  Admin(Role.ADMINUSER),
  User(Role.USER);

  /** Can add / maintain users */
  public static final String ADMINUSER = "ROLE_ADMIN";
  /** Can user normal system funcations */
  public static final String USER = "ROLE_USER";


  private String authority;

  Role(String authority) {
    Guard.hasValue(authority);
    this.authority = authority;
  }

  public String getAuthority() {
    return authority;
  }

  @Override
  public String toString() {
    return "Role[" + authority + "]";
  }

  public static Role map(String value){
    Guard.hasValue("value to map to Role cannot be empty", value);
    value = value.trim();
    if( ADMINUSER.equalsIgnoreCase(value) ){
      return Role.Admin;
    }
    else if( USER.equalsIgnoreCase(value) ){
      return Role.User;
    }
    else {
      throw ExceptionUtil.createIllegalArgException(
        "cannot convert value to Role: '%s'", value );
    }
  }
}
