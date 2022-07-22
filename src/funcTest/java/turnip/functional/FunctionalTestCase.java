package turnip.functional;

import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.client.RestTemplate;
import turnip.functional.config.AuthnTokenSvc;
import turnip.functional.config.FuncTestProps;
import turnip.functional.config.FunctionalTestConfig;
import turnip.util.Log;

import static java.lang.String.format;
import static turnip.util.Log.to;
import static turnip.util.RestUtil.createEntityWithBearer;

@SpringJUnitConfig(FunctionalTestConfig.class)
public abstract class FunctionalTestCase {
  protected Log log = to(getClass());

  @Autowired protected RestTemplate rest;
  @Autowired protected FuncTestProps props;
  @Autowired protected AuthnTokenSvc token;


  @RegisterExtension
  protected static TurnipJettyTestServer jettyTestServer =
    new TurnipJettyTestServer();  
  
  public <T> T get(String authnToken, String url, Class<T> resultType){
    HttpEntity<T> entity = createEntityWithBearer(authnToken);

    var epResponse = rest.exchange(turnipApiServerUrl(url),
      HttpMethod.GET, entity, resultType);
    
    return epResponse.getBody();
  }

  public <TRequest, TResult> TResult post(
    String authnToken, String url, 
    TRequest request, Class<TResult> resultType
  ){
    HttpEntity<TRequest> entity = createEntityWithBearer(authnToken, request);

    var epResponse = rest.exchange(turnipApiServerUrl(url),
      HttpMethod.POST, entity, resultType);
    
    return epResponse.getBody();
  }

  public String turnipApiServerUrl(String url){
    //noinspection HttpUrlsUsage
    return format("http://%s%s", props.turnipApiServer, url);
  }

  protected String getTestPrefix(TestInfo testInfo) {
    return testInfo.getTestClass().orElseThrow().getSimpleName();
  }
}
