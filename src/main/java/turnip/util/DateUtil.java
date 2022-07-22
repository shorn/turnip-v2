package turnip.util;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.function.Function;

public class DateUtil {
  public static String UTC_ID = "UTC";
  public static String[] MONTH_NAMES = {
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"
  };

  private static String FILESAFE_DATETIME_FORMAT = "yyyyMMdd-HHmmss";
  private static String FILESAFE_MIILLISECOND_DATETIME_FORMAT =
    "yyyyMMdd-HHmmss-SSS";
  private static String COMPACT_MIILLISECOND_DATETIME_FORMAT =
    "yyyyMMddHHmmssSSS";
  private static String MILLISECOND_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
  private static final String YYYY_MM_DD_HH_MM = "yyyy-MM-dd HH:mm";
  /**
   Note lack of space between minutes and zone
   */
  public static final String YYYY_MM_DD_HH_MMZ = YYYY_MM_DD_HH_MM + "Z";
  public static final String MONTH_COMPACT_ALPHA = "MMMyyyy";

  public static final String TIME_MILLIS = "HH:mm:ss.SSS";

  /**
   I'm not actually sure this is right, the timezone gets formatted like
   "+0000", but I think SES does it as "(UTC)".  I was just adding it for
   completeness when initially implemented, so it can likely be changed
   without causing a major issue.
   */
  public static final String RFC822_FORMAT = "EEE, d MMM yyyy HH:mm:ss Z";

  public static TimeZone aestTimezone() {
    return TimeZone.getTimeZone("AEST");
  }

  public static TimeZone utcTimezone() {
    return TimeZone.getTimeZone("UTC");
  }

  public static ZoneId utcZoneId() {
    return utcTimezone().toZoneId();
  }

  /**
   (2000, 0) -> ("1st January 2000 00:00.000" - default TZ)
   */
  @SuppressWarnings("deprecation")
  public static Date fromMonth(int year, int month) {
    return new Date(year - 1900, month, 1);
  }

  public static YearMonth parseMonth(String value) {
    DateTimeFormatter formatter = new DateTimeFormatterBuilder().
      parseCaseInsensitive().
      appendPattern(MONTH_COMPACT_ALPHA).
      toFormatter(Locale.ENGLISH);
    return YearMonth.parse(value, formatter);
  }

  public static String formatYearMonth(YearMonth value) {
    DateTimeFormatter formatter = new DateTimeFormatterBuilder()
      .appendPattern(MONTH_COMPACT_ALPHA)
      .toFormatter(Locale.ENGLISH);
    return formatter.format(value).toUpperCase();
  }

  public static Date parseMonthToDate(String value) {
    ZonedDateTime zonedDateTime = parseMonth(value).
      atDay(1).atStartOfDay().atZone(utcZoneId());
    return Date.from(zonedDateTime.toInstant());
  }

  public static String formatFileSafe(Date date) {
    SimpleDateFormat fmt = new SimpleDateFormat(FILESAFE_DATETIME_FORMAT);
    fmt.setTimeZone(TimeZone.getTimeZone(UTC_ID));

    return fmt.format(date);
  }

  public static String formatTimeMillis(Date date) {
    SimpleDateFormat fmt = new SimpleDateFormat(TIME_MILLIS);
    fmt.setTimeZone(TimeZone.getTimeZone(UTC_ID));

    return fmt.format(date);
  }

  public static String formatMillisecondFileSafe(Date date) {
    SimpleDateFormat fmt = new SimpleDateFormat(
      FILESAFE_MIILLISECOND_DATETIME_FORMAT);
    fmt.setTimeZone(TimeZone.getTimeZone(UTC_ID));

    return fmt.format(date);
  }

  public static String formatCompactMillisecond(Date date) {
    SimpleDateFormat fmt = new SimpleDateFormat(
      COMPACT_MIILLISECOND_DATETIME_FORMAT);
    fmt.setTimeZone(TimeZone.getTimeZone(UTC_ID));

    return fmt.format(date);
  }

  /**
   @see #MILLISECOND_DATETIME_FORMAT
   */
  public static String formatMillis(Date date) {
    SimpleDateFormat fmt = new SimpleDateFormat(MILLISECOND_DATETIME_FORMAT);
    fmt.setTimeZone(TimeZone.getTimeZone(UTC_ID));

    return fmt.format(date);
  }

  /**
   @see #RFC822_FORMAT
   */
  public static String formatRfc822(Date date) {
    SimpleDateFormat fmt = new SimpleDateFormat(RFC822_FORMAT);
    fmt.setTimeZone(TimeZone.getTimeZone(UTC_ID));

    return fmt.format(date);
  }

  /**
   @param date - "1970-01-01 10:00" will give you a date with 0 milliseconds
   */
  public static Date parseBrisDateTime(String date) {
    Guard.hasValue("date must have a value", date);

    SimpleDateFormat df = new SimpleDateFormat(YYYY_MM_DD_HH_MM);
    df.setTimeZone(aestTimezone());
    try {
      return df.parse(date);
    }
    catch( ParseException e ){
      throw new IllegalArgumentException("couldn't parse date", e);
    }
  }

  public static String formatBrisDateTime(Date d) {
    Guard.notNull("date to format was null", d);

    SimpleDateFormat sdf = new SimpleDateFormat(YYYY_MM_DD_HH_MM);
    sdf.setTimeZone(aestTimezone());

    return sdf.format(d);
  }

  public static String formatUtcDateTime(
    @Nullable Date d,
    String defaultValue) {
    /* this happened when we were passing in a Message.getDate(), which I
     assumed would always be set, decided to go with changing the
     date to be nullable and passing a default value rather than pushing
     out the null check logic */
    if( d == null ){
      return defaultValue;
    }

    SimpleDateFormat sdf = new SimpleDateFormat(YYYY_MM_DD_HH_MM);
    sdf.setTimeZone(utcTimezone());
    return sdf.format(d);
  }

  /**
   Sets the day of month to the given date, then shifts the given day of
   month forward of backward by adjustDays,
   keeping the time intact.
   */
  public static Date resetDate(
    Date originalTime,
    Date resetDate,
    int adjustDays) {
    GregorianCalendar newTimeCal = new GregorianCalendar(aestTimezone());
    GregorianCalendar resetCal = new GregorianCalendar(aestTimezone());


    newTimeCal.setTime(originalTime);
    resetCal.setTime(resetDate);

    newTimeCal.set(Calendar.DAY_OF_MONTH, resetCal.get(Calendar.DAY_OF_MONTH));
    newTimeCal.set(Calendar.MONTH, resetCal.get(Calendar.MONTH));
    newTimeCal.set(Calendar.YEAR, resetCal.get(Calendar.YEAR));
    // add makes sure larger fields go up (e.g. rolling across a year boundary)
    newTimeCal.add(Calendar.DAY_OF_YEAR, adjustDays);

    return newTimeCal.getTime();
  }

  public static Date addMonth(Date date, int adjustMonth) {
    GregorianCalendar cal = new GregorianCalendar(utcTimezone());
    cal.setTime(date);
    cal.add(Calendar.MONTH, adjustMonth);

    return cal.getTime();
  }

  public static Date addSeconds(Date date, int adjustSeconds) {
    GregorianCalendar cal = new GregorianCalendar(utcTimezone());
    cal.setTime(date);
    cal.add(Calendar.SECOND, adjustSeconds);

    return cal.getTime();
  }

  public static String formatCompactAlpha(YearMonth yearMonth) {
    return formatCompactAlpha(yearMonth, "", Capitalize.None);
  }

  public static String formatMonthFullAlpha(int month) {
    if( month < 1 ){
      throw ExceptionUtil.createIllegalArgException(
        "month is negative, does not make sense: %s", month);
    }
    if( month > 12 ){
      throw ExceptionUtil.createIllegalArgException(
        "month is too big, does not make sense: %s", month);
    }

    return MONTH_NAMES[month - 1];
  }

  /**
   @param month 1 - 12
   */
  public static String formatMonthCompactAlpha(int month) {
    return formatMonthFullAlpha(month).substring(0, 3);
  }

  public static String formatCompactAlpha(
    @Nullable YearMonth yearMonth,
    String nullValue,
    Capitalize capitalize
  ) {
    if( yearMonth == null ){
      return nullValue;
    }
    String monthString = formatMonthCompactAlpha(yearMonth.getMonthValue());

    return String.format(
      "%s/%s",
      capitalize.handleSafely(monthString),
      yearMonth.getYear());
  }

  public static String formatCompactNumeric(YearMonth yearMonth) {
    return formatCompactNumeric(yearMonth, "");
  }

  public static String formatCompactNumeric(
    @Nullable YearMonth yearMonth,
    String nullValue
  ) {
    if( yearMonth == null ){
      return nullValue;
    }

    return String.format(
      "%02d/%s",
      yearMonth.getMonthValue(),
      yearMonth.getYear());
  }

  public enum Capitalize {
    Lower(String::toLowerCase),
    Upper(String::toUpperCase),
    Initcap(StringUtil::capitalize),
    /**
     i.e. don't capitalize
     */
    None(Function.identity());

    Capitalize(Function<String, String> format) {
      this.format = format;
    }

    private Function<String, String> format;

    public Function<String, String> getFormat() {
      return format;
    }

    private String handleSafely(String s) {
      if( s == null ){
        return "";
      }
      return this.format.apply(s);
    }
  }

  /**
   @return will return null if both params are null
   */
  public static YearMonth latest(
    @Nullable YearMonth left,
    @Nullable YearMonth right
  ) {
    if( left == null && right == null ){
      return null;
    }
    else if( left == null ){
      return right;
    }
    else if( right == null ){
      return left;
    }

    if( left.isBefore(right) ){
      return right;
    }
    else {
      return left;
    }
  }

}

