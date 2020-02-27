package space.delusive.tversu.manager.impl;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import space.delusive.tversu.Button;
import space.delusive.tversu.manager.KeyboardManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Keyboard wrapper that is needed to simplify keyboard usage in the code
 *
 * @author Delusive-
 * @version 1.2
 */
public class KeyboardManagerImpl implements KeyboardManager {
    private ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
    private List<KeyboardRow> keyboard = new ArrayList<>();
    private KeyboardRow keyboardRow = new KeyboardRow();
    private int maxColumnsCount;

    public KeyboardManagerImpl(int maxColumnsCount) {
        this.maxColumnsCount = maxColumnsCount;
    }

    /**
     * Add item to the keyboard
     *
     * @param item Item object that will be added to the keyboard
     */
    @Override
    public void addItem(String item) {
        if (keyboardRow.size() == maxColumnsCount) {
            keyboard.add(keyboardRow);
            keyboardRow = new KeyboardRow();
        }
        keyboardRow.add(item);
    }

    @Override
    public void addItem(Button button) {
        addItem(button.getLocalizedName());
    }

    /**
     * Add item to the keyboard but to new line
     *
     * @param item Item object that will be added to the new line to the keyboard
     */
    @Override
    public void addItemOnNewLine(String item) {
        if (isNotEmpty(keyboardRow)) {
            keyboard.add(keyboardRow);
            keyboardRow = new KeyboardRow();
        }
        addItem(item);
    }

    @Override
    public void addItemOnNewLine(Button button) {
        addItemOnNewLine(button.getLocalizedName());
    }

    /**
     * Form the keyboard using data that we received early by add methods
     *
     * @return Keyboard object that we can use in messages
     */
    @Override
    public ReplyKeyboardMarkup getKeyboard() {
        if (!keyboard.contains(keyboardRow)) keyboard.add(keyboardRow);
        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }

    private boolean isNotEmpty(List list) {
        return !list.isEmpty();
    }
}
