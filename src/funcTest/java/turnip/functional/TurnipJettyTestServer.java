package turnip.functional;

import org.eclipse.jetty.server.ServerConnector;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import turnip.App;
import turnip.jetty.EmbeddedJetty;
import turnip.spring.config.AppConfig;
import turnip.util.Log;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.extension.ExtensionContext.Namespace.GLOBAL;
import static turnip.spring.config.WebSecurityConfig.AUDIENCE_PROP_NAME;
import static turnip.util.ExceptionUtil.createRuntimeException;
import static turnip.util.JvmUtil.normaliseJvmDefaults;
import static turnip.util.Log.to;
import static turnip.util.NetUtil.isLocalhostPortAvailable;

public class TurnipJettyTestServer 
implements BeforeAllCallback, 
  ExtensionContext.Store.CloseableResource 
{
  private static Log log = to(TurnipJettyTestServer.class);

  private static boolean started = false;

  private static ServerConnector serverConnector;
  private static EmbeddedJetty jetty;
  
  private Map<RequestMappingInfo, HandlerMethod> turnipApiHandlerMethods;


  @Override
  public void beforeAll(ExtensionContext context) throws Exception {
    if( !started ){
      started = true;
      initTurnip();
      context.getRoot().getStore(GLOBAL).
        put(this.getClass().getName(), this);
    }
  }

  @Override
  public void close() {
    shutdownTurnip();
  }

  public void initTurnip() throws Exception {
    to(FunctionalTestCase.class).info("initTurnip()");
    normaliseJvmDefaults();

    jetty = new EmbeddedJetty();

    /* I often accidentally run both local dev server and func tests at same 
    time on the same DB.  Using the same http port acts as a proxy "shared 
    resource" to detect that situation. */
    if( !isLocalhostPortAvailable(App.PORT) ){
      throw createRuntimeException(
        "Port %s is in use," +
          " usually caused by a Turnip dev server still running." +
          " Stop other process so they don't step on each others DB.",
        App.PORT);
    }

    serverConnector = jetty.configureHttpConnector(App.PORT);
    jetty.addServletContainerInitializer((sci, ctx) ->
    {
      var rootContext = AppConfig.initServletContext(ctx);
      MutablePropertySources propertySources =
        rootContext.getEnvironment().getPropertySources();
      /* use a different API audience for functional tests, this way 
      the production user database is not polluted with tests users */
      propertySources.addLast(new MapPropertySource(
        "functest_source",
        Map.of(AUDIENCE_PROP_NAME, "turnip-functional-test-api")));

      rootContext.addApplicationListener(event -> {
        if( event instanceof ContextRefreshedEvent ){
          turnipApiHandlerMethods = rootContext.
            getBean(RequestMappingHandlerMapping.class).getHandlerMethods();
        }
      });
    });

    jetty.getServer().start();
  }

  public static void shutdownTurnip() {
    try {
      jetty.shutdown();
    }
    catch( Exception e ){
      fail("Jetty did not shutdown properly after unit tests", e);
    }
  }

  public Map<RequestMappingInfo, HandlerMethod> getTurnipApiHandlerMethods() {
    return turnipApiHandlerMethods;
  }

  public EmbeddedJetty getJetty() {
    return jetty;
  }
}