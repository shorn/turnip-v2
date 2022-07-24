package turnip;

import turnip.jetty.EmbeddedJetty;
import turnip.spring.config.AppConfig;
import turnip.util.Log;

import static turnip.util.JvmUtil.normaliseJvmDefaults;
import static turnip.util.Log.to;

public class App {
  public static final int PORT = 8080;
  
  private static final Log log = to(App.class);

  public static void main(String... args) throws Exception {
    normaliseJvmDefaults();
    
    var jetty = new EmbeddedJetty();
    /* IDEA whining about not using try-with-resources, but pretty sure we  
    don't want that given we let the main method return and rely on the 
    daemon threads to keep the JVM alive.  */
    //noinspection resource
    jetty.configureHttpConnector(PORT);
    jetty.addServletContainerInitializer( (sci, ctx) -> 
        AppConfig.initServletContext(ctx) );

    // Will be called when pressing ctrl-c, for example.
    Runtime.getRuntime().addShutdownHook(
      new Thread(jetty::shutdown, "app-shutdown") );
    
    log.info("starting the server");
    jetty.startJoin();
  }

}

 
