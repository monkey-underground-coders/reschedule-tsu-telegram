package space.delusive.tversu.manager;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

public interface IKeyboardManager {
    ReplyKeyboardMarkup getKeyboard();

    void addItem(String item);
}
