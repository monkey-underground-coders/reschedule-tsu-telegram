package space.delusive.tversu;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import space.delusive.tversu.config.ApplicationContextConfiguration;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        ApiContextInitializer.init();
        TelegramBotsApi api = new TelegramBotsApi();
        ApplicationContext context = new AnnotationConfigApplicationContext(ApplicationContextConfiguration.class);
        try {
            api.registerBot(context.getBean(TversuTimingBot.class));
            logger.info("TverSU Timing Bot started successfully!");
        } catch (TelegramApiRequestException e) {
            logger.error(e);
        }
    }
}
