package space.delusive.tversu;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import space.delusive.tversu.manager.IDataManager;
import org.telegram.telegrambots.meta.api.objects.Message;

public class TversuTimingBot extends TelegramLongPollingBot {
    private final IDataManager config;


    TversuTimingBot(IDataManager config) {
        this.config = config;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!(update.hasMessage() && update.getMessage().hasText())) return;
        handleIncomingMessage(update.getMessage());
    }

    @Override
    public String getBotUsername() {
        return config.getString("bot.name");
    }

    @Override
    public String getBotToken() {
        return config.getString("bot.token");
    }

    private void handleIncomingMessage(Message msg) {

    }

}
