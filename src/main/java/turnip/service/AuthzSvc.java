package turnip.service;

import org.springframework.stereotype.Component;
import turnip.spring.security.Role;
import turnip.spring.security.TurnipAuthn;
import turnip.util.Log;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.disjoint;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static turnip.util.Log.to;

@Component
public class AuthzSvc {
  private static Log log = to(AuthzSvc.class);

  public static List<Role> NO_ROLES = emptyList();
  public static List<Role> USER_ROLE = singletonList(Role.User);
  public static List<Role> ADMIN_ROLE = singletonList(Role.Admin);
  
  private UserSvc userSvc;

  public AuthzSvc(UserSvc userSvc) {
    this.userSvc = userSvc;
  }

  public void guardHasRole(TurnipAuthn auth, Role role){
    if( !hasRole(auth, role) ){
      log.msg("not authorized for role").
        with("auth", auth).with("role", role).warn();
      throw new NotAuthorizedExcepton("user not authorized");
    }
  }

  public void guardIsUser(TurnipAuthn auth){
    guardHasRole(auth, Role.User);
  }

  public void guardIsAdmin(TurnipAuthn auth){
    guardHasRole(auth, Role.Admin);
  }
  
  public void guardHasAnyRole(TurnipAuthn auth){
    if( disjoint(getRoles(auth), Arrays.asList(Role.values().clone())) ){
      log.msg("not authorized for any role").with("auth", auth).warn();
      throw new NotAuthorizedExcepton("user not authorized");
    }
  }

  public boolean hasRole(TurnipAuthn auth, Role role){
    return getRoles(auth).contains(role);
  }

  public boolean hasAdminRole(TurnipAuthn auth){
    return hasRole(auth, Role.Admin);
  }
  
  public boolean hasUserRole(TurnipAuthn auth){
    return hasRole(auth, Role.User);
  }
  
  public List<Role> getRoles(TurnipAuthn auth){
    return userSvc.findUser(auth.getPrincipal()).
      map(UserSvc.UserInfo::roles).
      orElse(NO_ROLES);
  }
  
  public static class NotAuthorizedExcepton extends RuntimeException {
    public NotAuthorizedExcepton(String message) {
      super(message);
    }
  }
}
