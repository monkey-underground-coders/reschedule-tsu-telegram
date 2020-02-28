package space.delusive.tversu;

import space.delusive.tversu.exception.NoSuchButtonException;
import space.delusive.tversu.manager.DataManager;
import space.delusive.tversu.manager.impl.PropertiesManager;

public enum Button {
    // register:
    TO_PREVIOUS_STAGE("register.button.back"),

    // main menu:
    CURRENT_LESSON("menu.main.button.current.lesson"),
    NEXT_LESSON("menu.main.button.next.lesson"),
    TODAY_LESSONS("menu.main.button.today.lessons"),
    TOMORROW_LESSONS("menu.main.button.tomorrow.lessons"),
    REMAINING_LESSONS_OF_WEEK("menu.main.button.remaining.lessons.of.week"),
    LESSONS_OF_SPECIFIED_DAY("menu.main.button.lessons.of.specified.day"),
    UNREGISTER("menu.main.button.unregister"),
    FEEDBACK("menu.main.button.feedback"),

    // days of weeks:
    MONDAY_PLUS_WEEK("menu.choose.working.day.button.monday.plus"),
    TUESDAY_PLUS_WEEK("menu.choose.working.day.button.tuesday.plus"),
    WEDNESDAY_PLUS_WEEK("menu.choose.working.day.button.wednesday.plus"),
    THURSDAY_PLUS_WEEK("menu.choose.working.day.button.thursday.plus"),
    FRIDAY_PLUS_WEEK("menu.choose.working.day.button.friday.plus"),
    SATURDAY_PLUS_WEEK("menu.choose.working.day.button.saturday.plus"),
    MONDAY_MINUS_WEEK("menu.choose.working.day.button.monday.minus"),
    TUESDAY_MINUS_WEEK("menu.choose.working.day.button.tuesday.minus"),
    WEDNESDAY_MINUS_WEEK("menu.choose.working.day.button.wednesday.minus"),
    THURSDAY_MINUS_WEEK("menu.choose.working.day.button.thursday.minus"),
    FRIDAY_MINUS_WEEK("menu.choose.working.day.button.friday.minus"),
    SATURDAY_MINUS_WEEK("menu.choose.working.day.button.saturday.minus");

    private static final DataManager buttonsProps = new PropertiesManager("/buttons_ru.properties");

    public static Button of(String buttonText) {
        for (Button value : Button.values()) {
            if (buttonsProps.getString(value.nameInProps).equalsIgnoreCase(buttonText)) return value;
        }
        throw new NoSuchButtonException("Button with text " + buttonText + " not found");
    }

    private final String nameInProps;

    Button(String nameInProps) {
        this.nameInProps = nameInProps;
    }

    public String getLocalizedName() {
        return buttonsProps.getString(nameInProps);
    }

}
