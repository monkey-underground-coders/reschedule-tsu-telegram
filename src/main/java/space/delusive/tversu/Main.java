package space.delusive.tversu;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import space.delusive.tversu.manager.IDataManager;
import space.delusive.tversu.manager.impl.PropertiesManager;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);
    private static final IDataManager settings = new PropertiesManager("/timingbot.properties");

    public static void main(String[] args) {
        ApiContextInitializer.init();
        TelegramBotsApi api = new TelegramBotsApi();
        try {
            api.registerBot(new TversuTimingBot(settings));
            logger.info("TverSU Timing Bot started successfully!");
        } catch (TelegramApiRequestException e) {
            logger.error(e);
        }
    }
}
