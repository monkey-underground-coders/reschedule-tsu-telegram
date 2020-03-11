package space.delusive.tversu.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BaseUtilsTest {

    @Test
    @DisplayName("BaseUtils capitalizeString when \"c\" returns \"C\"")
    public void shouldReturnCapitalizedString() {
        String expected = "C";
        String actual = BaseUtils.capitalizeString("c");
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("BaseUtils capitalizeString when \"B\" returns \"B\"")
    public void shouldReturnSameString() {
        String expected = "B";
        String actual = BaseUtils.capitalizeString("B");
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("BaseUtils capitalizeString when \"something\" returns \"Something\"")
    public void shouldCapitalizeOnlyFirstLetterOfString() {
        String expected = "Something";
        String actual = BaseUtils.capitalizeString("something");
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("BaseUtils capitalizeString when null throws an exception")
    public void shouldThrowAnExceptionWhenNull() {
        assertThrows(NullPointerException.class, () -> BaseUtils.capitalizeString(null));
    }

    @Test
    @DisplayName("BaseUtils capitalizeString when empty throws an exception")
    public void shouldThrowAnExceptionWhenEmptyString() {
        assertThrows(StringIndexOutOfBoundsException.class, () -> BaseUtils.capitalizeString(""));
    }
}
