package turnip.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static turnip.unit.util.JWT.createJwt;

public class JwtUtilTest {
  private static Log log = Log.to(JwtUtilTest.class);
  
  /**
   Shows that our simple createJwt() method conforms to the standard, by 
   matching char-for-char what the wiki page says it's supposed to do.
   https://en.wikipedia.org/w/index.php?title=JSON_Web_Token&oldid=1055270499
   */
  @Test
  public void simpleJwtCreation() {
    String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
    String payload = "{\"loggedInAs\":\"admin\",\"iat\":1422779638}";

    String testJwt = createJwt(header, payload, "secretkey");
    
//    var debug = debugJwt(testJwt);
//    log.info("header: " + debug.header());
//    log.info("payload: " + debug.payload());
    
    assertThat(testJwt).isEqualTo(
      "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
        "eyJsb2dnZWRJbkFzIjoiYWRtaW4iLCJpYXQiOjE0MjI3Nzk2Mzh9." +
        "gzSraSYS8EXBxLN_oWnFSRgCzcmJmMjLiuyu5CSpyHI"
    );
  }
}
