package space.delusive.tversu.util;

import space.delusive.tversu.dto.DayOfWeek;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateUtils {
    public static DayOfWeek getCurrentDayOfWeek() {
        LocalDate localDate = LocalDate.now(ZoneId.of("Europe/Moscow"));
        return DayOfWeek.valueOf(localDate.getDayOfWeek().toString());
    }

    /**
     * Compares two times in format HH:mm
     *
     * @param one first time in format HH:mm
     * @param two second time in format HH:mm
     * @return one > two == -1 | one == two == 0 | one < two == 1
     */
    public static byte compareTime(String one, String two) {
        String[] first = one.split(":");
        String[] second = two.split(":");
        byte firstHours = Byte.parseByte(first[0]);
        byte firstMins = Byte.parseByte(first[1]);
        byte secondHours = Byte.parseByte(second[0]);
        byte secondMins = Byte.parseByte(second[1]);

        if (firstHours > secondHours) return -1;
        if (firstHours < secondHours) return 1;
        if (firstMins == secondMins) return 0;
        return (byte) (firstMins > secondMins ? -1 : 1);
    }

    public static String getCurrentTime() {
        return LocalDateTime.now(ZoneId.of("Europe/Moscow")).format(DateTimeFormatter.ofPattern("HH:mm"));
    }
}
