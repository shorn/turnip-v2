package turnip.spring.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import turnip.util.Log;

import static turnip.util.Log.to;

@Component
public class StartupListener implements
  ApplicationListener<ContextRefreshedEvent> {
  
  private static final Log log = to(StartupListener.class);

  public static int counter;

  @Value("${turnip.greeting:no greeting config}")
  private String greeting;
  
  @Override public void onApplicationEvent(ContextRefreshedEvent event) {
    log.with("greeting", greeting).info();
    counter++;
  }
}