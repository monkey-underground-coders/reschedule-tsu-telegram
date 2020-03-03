package space.delusive.tversu.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DateUtilsTest {

    @Test
    @DisplayName("DateUtils compareTime when second is after first returns 1")
    public void shouldReturnOneIfSecondIsAfterFirst() {
        String first = "8:30";
        String second = "10:15";
        int actual = DateUtils.compareTime(first, second);
        assertEquals(1, actual);
    }

    @Test
    @DisplayName("DateUtils compareTime when second = first returns 0")
    public void shouldReturnZeroIfEqual() {
        String first = "8:30";
        String second = "8:30";
        int actual = DateUtils.compareTime(first, second);
        assertEquals(0, actual);
    }

    @Test
    @DisplayName("DateUtils compareTime when second = first returns 0")
    public void shouldReturnMinusOneIfFirstIsAfterSecond() {
        String first = "15:50";
        String second = "12:10";
        int actual = DateUtils.compareTime(first, second);
        assertEquals(-1, actual);
    }

    @Test
    @DisplayName("DateUtils compareTime when null throws an exception")
    public void shouldThrowAnExceptionIfNull() {
        assertThrows(NullPointerException.class, () -> DateUtils.compareTime(null, null));
    }

    @Test
    @DisplayName("DateUtils compareTime when no colon found throws an exception")
    public void shouldThrowAnExceptionIfNoColonFound() {
        assertThrows(NumberFormatException.class, () -> DateUtils.compareTime("no_colon_here", "here_too"));
    }

    @Test
    @DisplayName("DateUtils compareTime when no colon found throws an exception")
    public void shouldThrowAnExceptionIfEmptyString() {
        assertThrows(NumberFormatException.class, () -> DateUtils.compareTime("", ""));
    }

    @Test
    @DisplayName("DateUtils getCurrentTime always returns time in correct format")
    public void shouldReturnTimeInValidFormat() {
        String formatRegex = "[0-2][0-9]:[0-5][0-9]";
        assertTrue(DateUtils.getCurrentTime().matches(formatRegex));
    }
}
