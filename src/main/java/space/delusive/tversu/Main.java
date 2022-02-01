package space.delusive.tversu;

import kong.unirest.GsonObjectMapper;
import kong.unirest.Unirest;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import space.delusive.tversu.config.ApplicationContextConfiguration;

@Log4j2
public class Main {
    public static void main(String[] args) {
        Unirest.config().setObjectMapper(new GsonObjectMapper());
        ApplicationContext context = new AnnotationConfigApplicationContext(ApplicationContextConfiguration.class);
        try {
            TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
            api.registerBot(context.getBean(TversuTimingBot.class));
            log.info("TverSU Timing Bot started successfully!");
        } catch (TelegramApiException e) {
            log.error(e);
        }
    }
}
