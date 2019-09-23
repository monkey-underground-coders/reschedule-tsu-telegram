package space.delusive.tversu.manager.impl;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import space.delusive.tversu.manager.IKeyboardManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Для удобной работы с клавиатурками, шоб по 1к раз не писать одно и то же =="
 * @author Delusive-
 * @version 1.0
 */
public class KeyboardManager implements IKeyboardManager {
    private ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
    private List<KeyboardRow> keyboard = new ArrayList<>();
    private KeyboardRow keyboardRow = new KeyboardRow();
    private int maxColumnsCount;

    public KeyboardManager(int maxColumnsCount) {
        this.maxColumnsCount = maxColumnsCount;
    }

    /**
     * Добавить итем в клавиатуру
     * @param item итем, который надо добавить
     */
    @Override
    public void addItem(String item) {
        if (keyboardRow.size() == maxColumnsCount) {
            keyboard.add(keyboardRow);
            keyboardRow = new KeyboardRow();
        }
        keyboardRow.add(item);
    }

    /**
     * Сформировать клавиатуру из полученных ранее данных
     * @return Готовая клавиатур_очка
     */
    @Override
    public ReplyKeyboardMarkup getKeyboard() {
        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }
}
