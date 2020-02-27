package space.delusive.tversu.manager;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import space.delusive.tversu.Button;

public interface KeyboardManager {
    ReplyKeyboardMarkup getKeyboard();

    void addItem(Button button);

    void addItem(String item);

    void addItemOnNewLine(Button button);

    void addItemOnNewLine(String item);
}
