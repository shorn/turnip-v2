package turnip.endpoint;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static turnip.spring.config.WebSecurityConfig.PUBLIC;

@RequestMapping(PUBLIC)
@RestController
public class AnonymousEndpoint {

  /*
  curl loclhost:8080/public/anon-map
   */
  @GetMapping("/anon-map")
  public Map<String, String> warmUp(){
    return Map.of("status","UP");
  }

  /*
  curl loclhost:8080/public/anon-object
   */
  @GetMapping("/anon-object")
  public Result getObject(){
    return new Result().setStatus2("UP");
  }
  
  public static class Result {
    public String status2;

    public String getStatus2() {
      return status2;
    }

    public Result setStatus2(String status2) {
      this.status2 = status2;
      return this;
    }
  }
}
