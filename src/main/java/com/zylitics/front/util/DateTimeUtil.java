package com.zylitics.front.util;

import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;

public class DateTimeUtil {
  
  // OffsetDateTime is taken rather than ZonedDateTime because PGJDBC doesn't support it,
  // OffsetDateTime also works with ESDB.
  public static OffsetDateTime getCurrentUTC() {
    return OffsetDateTime.now(ZoneId.of("UTC"));
  }
  
  // This method depends on the clock's timezone to get it's timezone, if clock's timezone is UTC,
  // returned timezone is UTC too.
  public static OffsetDateTime getCurrent(Clock clock) {
    return OffsetDateTime.now(clock);
  }
  
  public static LocalDateTime getCurrentLocal(Clock clock) {
    return LocalDateTime.now(clock);
  }
  
  /**
   * Does a safe conversion by checking null
   * @return null if given timestamp is null, else converted {@link LocalDateTime}
   */
  public static LocalDateTime sqlTimestampToLocal(@Nullable Timestamp timestamp) {
    if (timestamp == null) {
      return null;
    }
    return timestamp.toLocalDateTime();
  }
  
  public static long utcTimeToEpochSecs(LocalDateTime localDateTime) {
    return localDateTime.toInstant(ZoneOffset.UTC).getEpochSecond();
  }
  
  public static String getFrondEndDisplayDate(LocalDateTime dateTime) {
    return dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy, h:mm:ss a"));
  }
}
