package turnip.util;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static turnip.unit.util.BDD.THEN;
import static turnip.unit.util.BDD.WHEN;

public class RestUtilTest {
  @Test
  public void testit(){
    WHEN("RestUtil creates a HTTPEntity");
    String testToken = "testtoken";
    HttpEntity<?> entity = RestUtil.createEntityWithBearer(testToken);
    THEN("should attach authorization header with the bearer token");
    assertThat(entity.getHeaders().get("authorization").get(0)).
      isEqualTo("bearer " + testToken);
  }
}
