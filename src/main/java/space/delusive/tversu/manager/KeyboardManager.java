package space.delusive.tversu.manager;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

public interface KeyboardManager {
    ReplyKeyboardMarkup getKeyboard();

    void addItem(String item);

    void addItemOnNewLine(String item);
}
