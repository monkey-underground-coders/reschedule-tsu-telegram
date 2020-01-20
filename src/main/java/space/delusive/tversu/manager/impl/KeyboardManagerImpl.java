package space.delusive.tversu.manager.impl;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

/**
 * Для удобной работы с клавиатурками, шоб по 1к раз не писать одно и то же =="
 *
 * @author Delusive-
 * @version 1.1
 */
public class KeyboardManagerImpl implements space.delusive.tversu.manager.KeyboardManager {
    private ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
    private List<KeyboardRow> keyboard = new ArrayList<>();
    private KeyboardRow keyboardRow = new KeyboardRow();
    private int maxColumnsCount;

    public KeyboardManagerImpl(int maxColumnsCount) {
        this.maxColumnsCount = maxColumnsCount;
    }

    /**
     * Добавить итем в клавиатуру
     *
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
     * Добавить итем на новую строку в клавиатуру!
     *
     * @param item то, что надо добавить
     */
    public void addItemOnNewLine(String item) {
        if (isNotEmpty(keyboardRow)) {
            keyboard.add(keyboardRow);
            keyboardRow = new KeyboardRow();
        }
        addItem(item);
    }

    /**
     * Сформировать клавиатуру из полученных ранее данных
     *
     * @return Готовая клавиатур_очка
     */
    @Override
    public ReplyKeyboardMarkup getKeyboard() {
        if (!keyboard.contains(keyboardRow)) keyboard.add(keyboardRow);
        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }

    public boolean isNotEmpty(List list) {
        return !list.isEmpty();
    }
}