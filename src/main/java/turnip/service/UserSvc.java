package turnip.service;

import org.springframework.stereotype.Component;
import turnip.spring.security.Role;
import turnip.util.Guard;
import turnip.util.Log;
import turnip.util.ObjectUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static turnip.util.ExceptionUtil.createIllegalArgException;
import static turnip.util.Log.to;
import static turnip.util.ObjectUtil.hasValue;

/**
 Quick 'n dirty user database.  Will make it use a real DB at some point.
 */
@Component
public class UserSvc {
  private static Log log = to(UserSvc.class);

  private Map<String, List<Role>> userDb = new HashMap<>(Map.of(
    "turnip-test-user@example.com", AuthzSvc.USER_ROLE,
    "turnip-test-admin@example.com", AuthzSvc.ADMIN_ROLE
  ));

  public Optional<UserInfo> findUser(String email){
    List<Role> roles = userDb.get(email);
    if( roles == null ){
      return Optional.empty();
    }
    return Optional.of(new UserInfo(email, roles));
  }
  
  public List<String> listUserEmails(){
    return userDb.keySet().stream().toList();
  }
  
  public void addUser(UserInfo userInfo){
    Guard.hasValue("must have an email", userInfo.email);
    if( userDb.containsKey(userInfo.email) ){
      throw createIllegalArgException("duplicate email: %s", userInfo.email);
    }
    if( !hasValue(userInfo.roles) ){
      throw createIllegalArgException(
        "user must have a role: %s", userInfo.email);
    }
    userDb.put(userInfo.email, userInfo.roles);
  }

  public static record UserInfo(String email, List<Role> roles){}
}
