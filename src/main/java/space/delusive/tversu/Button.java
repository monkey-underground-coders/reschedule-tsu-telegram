package space.delusive.tversu;

import space.delusive.tversu.exception.NoSuchButtonException;
import space.delusive.tversu.manager.IDataManager;
import space.delusive.tversu.manager.impl.PropertiesManager;

enum Button {
    CURRENT_LESSON("menu.main.button.current.lesson"),
    NEXT_LESSON("menu.main.button.next.lesson"),
    TODAY_LESSONS("menu.main.button.today.lessons"),
    TOMORROW_LESSONS("menu.main.button.tomorrow.lessons"),
    UNREGISTER("menu.main.button.unregister");

    private static final IDataManager buttonsProps = new PropertiesManager("/buttons_ru.properties");

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

    String getLocalizedName() {
        return buttonsProps.getString(nameInProps);
    }

}
