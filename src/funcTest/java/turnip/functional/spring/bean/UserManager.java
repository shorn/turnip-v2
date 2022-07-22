package turnip.functional.spring.bean;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static java.lang.String.format;
import static turnip.util.DateUtil.UTC_ID;

/**
Encapsulate logic of creating unique user email.
 */
@Component
public class UserManager {

  private int counter;
  private String functestId;

  @PostConstruct
  public void initUserCounter(){
    functestId = formatFunctTestId();
    counter = 1;
  }

  public synchronized String formatNewUserEmail(String prefix){
    return format("functest-%s-%s-%s", functestId, prefix, counter++);
  }
  
  public static String formatFunctTestId() {
    Date date = new Date();
    SimpleDateFormat fmt = new SimpleDateFormat(
      "yyyyMMddHHmmss");
    fmt.setTimeZone(TimeZone.getTimeZone(UTC_ID));

    return fmt.format(date);
  }
}
