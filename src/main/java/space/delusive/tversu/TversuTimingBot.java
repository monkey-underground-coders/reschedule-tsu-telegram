package space.delusive.tversu;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import space.delusive.tversu.connection.IDatabaseManager;
import space.delusive.tversu.connection.impl.MysqlDatabaseManager;
import space.delusive.tversu.dao.IFacultyDao;
import space.delusive.tversu.dao.IUserDao;
import space.delusive.tversu.dao.impl.RestFacultyDao;
import space.delusive.tversu.dao.impl.SqlUserDao;
import space.delusive.tversu.entity.User;
import space.delusive.tversu.manager.IDataManager;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;

public class TversuTimingBot extends TelegramLongPollingBot {
    private final Logger logger = LogManager.getLogger(TversuTimingBot.class);

    private final IDataManager config;
    private final IDataManager messages;
    private final IUserDao userDao;
    private final IFacultyDao facultyDao;

    private static final int START = 0;
    private static final int CHOOSING_FACULTY = 1;
    private static final int CHOOSING_PROGRAM = 2;
    private static final int CHOOSING_COURSE = 3;
    private static final int CHOOSING_GROUP = 4;
    private static final int CHOOSING_SUBGROUP = 5;
    private static final int MAIN_MENU = 6;

    TversuTimingBot(IDataManager config, IDataManager messages) {
        this.config = config;
        this.messages = messages;
        this.facultyDao = new RestFacultyDao(config);
        String dbUrl = config.getString("db.url");
        String dbUsername = config.getString("db.username");
        String dbPassword = config.getString("db.,password");
        IDatabaseManager databaseManager = new MysqlDatabaseManager(dbUrl, dbUsername, dbPassword);
        userDao = new SqlUserDao(databaseManager);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!(update.hasMessage() && update.getMessage().hasText()) || !update.getMessage().isUserMessage()) return;
        try {
            handleIncomingMessage(update.getMessage());
        } catch (TelegramApiException e) {
            logger.error(e);
        }
    }

    @Override
    public String getBotUsername() {
        return config.getString("bot.name");
    }

    @Override
    public String getBotToken() {
        return config.getString("bot.token");
    }

    private void handleIncomingMessage(Message msg) throws TelegramApiException {
        User user = userDao.getUserById(msg.getFrom().getId());
        if (user == null) { //user doesn't exist
            user = registerAndGetUser(msg.getFrom().getId());
        }
        SendMessage response;
        switch (user.getState()) {
            case START:
            case CHOOSING_FACULTY:
            case CHOOSING_PROGRAM:
            case CHOOSING_COURSE:
            case CHOOSING_GROUP:
            case CHOOSING_SUBGROUP:
                response = messageOnRegistering(msg, user);
                break;
            default:
                response = sendMenuMessage();
        }
        execute(response);
    }

    private User registerAndGetUser(long userId) {
        Date currDate = Date.valueOf(LocalDate.now());
        userDao.addUser(new User(userId, START, null, null, 0, currDate, currDate));
        return userDao.getUserById(userId);
    }

    private void updateUserStage(User user, int stage) {
        user.setState(stage);
        if (!userDao.updateUser(user)) logger.error("Something went wrong while updating user stage!");
    }


    // register messages:

    private SendMessage messageOnRegistering(Message msg, User user) {
        SendMessage response = null;
        switch (user.getState()) {
            case START:
                response = messageOnStart(msg, user);
                break;
            case CHOOSING_FACULTY:
                response = messageOnChoosingFaculty(msg, user);
                break;
            case CHOOSING_PROGRAM:
                response = messageOnChoosingProgram(msg, user);
                break;
            case CHOOSING_COURSE:
                response = messageOnChoosingCourse(msg, user);
                break;
            case CHOOSING_GROUP:
                response = messageOnChoosingGroup(msg, user);
                break;
            case CHOOSING_SUBGROUP:
                response = messageOnChoosingSubgroup(msg, user);
                break;
        }
        return response;
    }

    private SendMessage messageOnStart(Message request, User user) {
        SendMessage response = new SendMessage();
        response.setChatId(request.getChatId())
                .enableMarkdown(true)
                .setText(messages.getString("start"))
                .setReplyMarkup(getFacultiesKeyboard());
        updateUserStage(user, CHOOSING_FACULTY);
        return response;
    }

    private SendMessage messageOnChoosingFaculty(Message request, User user) {
        SendMessage response = new SendMessage();
        response.setChatId(request.getChatId())
                .enableMarkdown(true);
        if (!facultyDao.getFaculties().contains(request.getText())) {
            response.setText(messages.getString("invalid.faculty"));
            response.setReplyMarkup(getFacultiesKeyboard());
        } else {
            response.setText(messages.getString("choose.program"));
            response.setReplyMarkup(getProgramKeyboard(request.getText()));
            user.setFaculty(request.getText());
            user.setState(CHOOSING_PROGRAM);
            userDao.updateUser(user);
        }
        return response;
    }

    private SendMessage messageOnChoosingProgram(Message request, User user) {
        //TODO
        return null;
    }

    private SendMessage messageOnChoosingCourse(Message request, User user) {
        //TODO
        return null;
    }

    private SendMessage messageOnChoosingGroup(Message request, User user) {
        //TODO
        return null;
    }

    private SendMessage messageOnChoosingSubgroup(Message request, User user) {
        //TODO
        return null;
    }


    // :register messages end

    // register keyboards:

    private ReplyKeyboardMarkup getFacultiesKeyboard() {
        var faculties = facultyDao.getFaculties();
        var replyKeyboardMarkup = new ReplyKeyboardMarkup();
        var keyboard = new ArrayList<KeyboardRow>();
        var row = new KeyboardRow();
        for (int i = 0; i < faculties.size(); i++) {
            if (i != 0 && i % 2 == 0) {
                keyboard.add(row);
                row = new KeyboardRow();
            }
            row.add(faculties.get(i));
        }
        replyKeyboardMarkup.setKeyboard(keyboard);
        return replyKeyboardMarkup;
    }

    private ReplyKeyboardMarkup getProgramKeyboard(String faculty) {
        var programs = facultyDao.getPrograms(faculty);
        var replyKeyboardMarkup = new ReplyKeyboardMarkup();
        var keyboard = new ArrayList<KeyboardRow>();
        var row = new KeyboardRow();
        programs.forEach(row::add);
        keyboard.add(row);
        replyKeyboardMarkup.setKeyboard(keyboard);
        return replyKeyboardMarkup;
    }

    private ReplyKeyboardMarkup getGroupsKeyboard() {
        //TODO
        return null;
    }

    // :register keyboards end

    private SendMessage sendMenuMessage() {
        //TODO
        return null;
    }
}
