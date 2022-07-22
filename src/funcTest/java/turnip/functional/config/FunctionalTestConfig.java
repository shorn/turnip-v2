package turnip.functional.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.client.RestTemplate;

@Configuration
@ComponentScan(basePackages = {
  "turnip.functional.config", "turnip.functional.spring.bean"})
@PropertySource(name = "functest_working_dir_environment",
  value = "./functest.properties",
  ignoreResourceNotFound = true)
@PropertySource(name = "functest_user_config_environment",
  value = "file:///${user.home}/.config/turnip/functest.properties",
  ignoreResourceNotFound = true)
public class FunctionalTestConfig {
  
  @Bean
  public RestTemplate restTemplate(){
    return new RestTemplate();
  }

}
