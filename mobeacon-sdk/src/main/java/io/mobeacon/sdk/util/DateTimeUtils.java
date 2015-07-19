package io.mobeacon.sdk.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by maxulan on 19.07.15.
 */
public class DateTimeUtils {
    private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static String now() {
        return dtToStr(new Date());
    }
    public static String dtToStr(Date dt) {
        return DATE_FORMAT.format(dt);
    }
}
