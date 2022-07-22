package turnip.endpoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import turnip.service.AuthzSvc;
import turnip.service.UserSvc;
import turnip.service.UserSvc.UserInfo;
import turnip.spring.security.Role;
import turnip.spring.security.TurnipAuthn;
import turnip.util.Log;

import java.util.List;
import java.util.Map;

import static turnip.spring.config.WebSecurityConfig.API;
import static turnip.util.Log.to;

@RequestMapping(API)
@RestController
public class MiscAdmin {
  private static Log log = to(MiscAdmin.class);

  private AuthzSvc authzSvc;
  private UserSvc userSvc;

  @Autowired
  public MiscAdmin(AuthzSvc authzSvc, UserSvc userSvc) {
    this.authzSvc = authzSvc;
    this.userSvc = userSvc;
  }

  @GetMapping("/warmup")
  public Map<String, String> warmUp(TurnipAuthn id){
    authzSvc.guardIsAdmin(id);
    return Map.of("status","UP");
  }

  @GetMapping("/user-info")
  public UserInfo userInfo(TurnipAuthn id) {
    authzSvc.guardHasAnyRole(id);
    return userSvc.findUser(id.getPrincipal()).orElseThrow();
  }

  @GetMapping("/list-users")
  public ListUsersResult listUsers(TurnipAuthn id) {
    authzSvc.guardIsAdmin(id);
    return new ListUsersResult(userSvc.listUserEmails());
  }

  @PostMapping("/add-user")
  public UserInfo addUser(TurnipAuthn id, @RequestBody AddUserRequest request) {
    authzSvc.guardIsAdmin(id);
    UserInfo user = new UserInfo(request.email, request.roles);
    userSvc.addUser(user);
    return user;
  }

  public record ListUsersResult(List<String> emails){}
  public record AddUserRequest(String email, List<Role> roles){}

}
