package space.delusive.tversu;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import space.delusive.tversu.entity.Cell;
import space.delusive.tversu.entity.DayOfWeek;
import space.delusive.tversu.entity.User;
import space.delusive.tversu.entity.WeekSign;
import space.delusive.tversu.exception.NoSuchButtonException;
import space.delusive.tversu.manager.DataManager;
import space.delusive.tversu.manager.KeyboardManager;
import space.delusive.tversu.manager.impl.KeyboardManagerImpl;
import space.delusive.tversu.service.FacultyService;
import space.delusive.tversu.service.TimingService;
import space.delusive.tversu.service.UserService;
import space.delusive.tversu.util.BaseUtils;
import space.delusive.tversu.util.DateUtils;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@Log4j2
public class TversuTimingBot extends TelegramLongPollingBot {
    private final DataManager config;
    private final DataManager messages;
    private final UserService userService;
    private final TimingService timingService;
    private final FacultyService facultyService;

    //stages
    private static final int START = 0;
    private static final int CHOOSING_FACULTY = 1;
    private static final int CHOOSING_PROGRAM = 2;
    private static final int CHOOSING_COURSE = 3;
    private static final int CHOOSING_GROUP = 4;
    private static final int CHOOSING_SUBGROUP = 5;
    private static final int MAIN_MENU = 6;
    private static final int CHOOSING_DAY_OF_WEEK = 7;

    @Autowired
    public TversuTimingBot(@Qualifier("config") DataManager config, @Qualifier("messages") DataManager messages, UserService userService, FacultyService facultyService, TimingService timingService) {
        this.config = config;
        this.messages = messages;
        this.userService = userService;
        this.facultyService = facultyService;
        this.timingService = timingService;
    }


    @Override
    public void onUpdateReceived(Update update) {
        if (!(update.hasMessage() && update.getMessage().hasText()) || !update.getMessage().isUserMessage()) return;
        Message message = update.getMessage();
        log.info("User (ID: {}, FN: {}, LN: {}) sent message with text: {}",
                message.getFrom().getId(), message.getFrom().getFirstName(), message.getFrom().getLastName(), message.getText());
        try {
            handleIncomingMessage(message);
        } catch (TelegramApiException e) {
            log.error(e);
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
        User user = userService.getUserById(msg.getFrom().getId());
        if (user == null) { //user doesn't exist
            user = registerAndGetUser(msg.getFrom().getId());
        } else {
            user.setLastMessageDate(Date.valueOf(LocalDate.now()));
            userService.updateUser(user);
        }
        SendMessage response = null;
        switch (user.getState()) {
            case START:
            case CHOOSING_FACULTY:
            case CHOOSING_PROGRAM:
            case CHOOSING_COURSE:
            case CHOOSING_GROUP:
            case CHOOSING_SUBGROUP:
                response = messageOnRegistering(msg, user);
                break;
            case MAIN_MENU:
                response = sendMenuMessage(msg, user);
                break;
            case CHOOSING_DAY_OF_WEEK:
                response = messageOnChoosingDayOfWeek(msg, user);
                break;
        }
        response.setChatId(msg.getChatId())
                .enableMarkdown(true);
        execute(response);
    }

    private User registerAndGetUser(long userId) {
        Date currDate = Date.valueOf(LocalDate.now());
        userService.addUser(new User(userId, START, null, null, 0, null, 0, currDate, currDate));
        return userService.getUserById(userId);
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
        response.setText(messages.getString("start"))
                .setReplyMarkup(getFacultiesKeyboard());
        user.setState(CHOOSING_FACULTY);
        userService.updateUser(user);
        return response;
    }

    private SendMessage messageOnChoosingFaculty(Message request, User user) {
        SendMessage response = new SendMessage();
        if (!facultyService.getFaculties().contains(request.getText())) {
            response.setText(messages.getString("invalid.faculty"));
            response.setReplyMarkup(getFacultiesKeyboard());
        } else {
            user.setFaculty(request.getText());
            user.setState(CHOOSING_PROGRAM);
            userService.updateUser(user);
            response.setText(messages.getString("choose.program"));
            response.setReplyMarkup(getProgramKeyboard(request.getText()));
        }
        return response;
    }

    private SendMessage messageOnChoosingProgram(Message request, User user) {
        SendMessage response = new SendMessage();
        if (!facultyService.getPrograms(user.getFaculty()).contains(request.getText())) {
            response.setText(messages.getString("invalid.program"));
            response.setReplyMarkup(getProgramKeyboard(user.getFaculty()));
        } else {
            user.setProgram(request.getText());
            user.setState(CHOOSING_COURSE);
            userService.updateUser(user);
            response.setText(messages.getString("choose.course"));
            response.setReplyMarkup(getCoursesKeyboard(user));
        }
        return response;
    }

    private SendMessage messageOnChoosingCourse(Message request, User user) {
        SendMessage response = new SendMessage();
        try {
            int course = Integer.parseInt(request.getText());
            if (facultyService.getCourses(user.getFaculty(), user.getProgram()).contains(course)) {
                user.setState(CHOOSING_GROUP);
                user.setCourse(course);
                userService.updateUser(user);
                response.setText(messages.getString("choose.group"));
                response.setReplyMarkup(getGroupsKeyboard(user));
            } else {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            response.setText(messages.getString("invalid.course"));
            response.setReplyMarkup(getCoursesKeyboard(user));
        }
        return response;
    }

    private SendMessage messageOnChoosingGroup(Message request, User user) {
        SendMessage response = new SendMessage();
        var groups = facultyService.getGroups(user.getFaculty(), user.getProgram(), user.getCourse());
        if (!groups.contains(request.getText())) {
            response.setText(messages.getString("invalid.group"));
            response.setReplyMarkup(getGroupsKeyboard(user));
        } else {
            user.setGroup(request.getText());
            int subgroups = facultyService.getSubgroupsCount(user.getFaculty(), user.getProgram(), user.getCourse(), user.getGroup());
            if (subgroups == 0) {
                user.setSubgroup(0);
                user.setState(MAIN_MENU);
                response.setText(messages.getString("register.end"));
                response.setReplyMarkup(getMenuKeyboard());
            } else {
                user.setState(CHOOSING_SUBGROUP);
                response.setText(messages.getString("choose.subgroup"));
                response.setReplyMarkup(getSubgroupsKeyboard(user));
            }
            userService.updateUser(user);
        }
        return response;
    }

    private SendMessage messageOnChoosingSubgroup(Message request, User user) {
        SendMessage response = new SendMessage();
        int subgroups = facultyService.getSubgroupsCount(user.getFaculty(), user.getProgram(), user.getCourse(), user.getGroup());
        int subgroup;
        try {
            subgroup = Integer.parseInt(request.getText());
            if (subgroup > subgroups || subgroup < 1) {
                throw new NumberFormatException();
            } else {
                user.setState(MAIN_MENU);
                user.setSubgroup(subgroup);
                userService.updateUser(user);
                response.setText(messages.getString("register.end"));
                response.setReplyMarkup(getMenuKeyboard());
            }
        } catch (NumberFormatException e) {
            response.setText(messages.getString("invalid.subgroup"));
            response.setReplyMarkup(getSubgroupsKeyboard(user));
        }
        return response;
    }

    // :register messages


    // register keyboards:

    private ReplyKeyboardMarkup getFacultiesKeyboard() {
        var faculties = facultyService.getFaculties();
        KeyboardManager keyboardManager = new KeyboardManagerImpl(2);
        faculties.forEach(keyboardManager::addItem);
        return keyboardManager.getKeyboard();
    }

    private ReplyKeyboardMarkup getProgramKeyboard(String faculty) {
        var programs = facultyService.getPrograms(faculty);
        KeyboardManager keyboardManager = new KeyboardManagerImpl(1);
        programs.forEach(keyboardManager::addItem);
        return keyboardManager.getKeyboard();
    }

    private ReplyKeyboardMarkup getCoursesKeyboard(User user) {
        var courses = facultyService.getCourses(user.getFaculty(), user.getProgram());
        KeyboardManager keyboardManager = new KeyboardManagerImpl(2);
        courses.forEach(course -> keyboardManager.addItem(course.toString()));
        return keyboardManager.getKeyboard();
    }

    private ReplyKeyboardMarkup getGroupsKeyboard(User user) {
        var groups = facultyService.getGroups(user.getFaculty(), user.getProgram(), user.getCourse());
        KeyboardManager keyboardManager = new KeyboardManagerImpl(2);
        groups.forEach(keyboardManager::addItem);
        return keyboardManager.getKeyboard();
    }

    private ReplyKeyboardMarkup getSubgroupsKeyboard(User user) {
        int subgroups = facultyService.getSubgroupsCount(user.getFaculty(), user.getProgram(), user.getCourse(), user.getGroup());
        KeyboardManager keyboardManager = new KeyboardManagerImpl(2);
        for (int i = 1; i <= subgroups; i++) keyboardManager.addItem(String.valueOf(i));
        return keyboardManager.getKeyboard();
    }

    // :register keyboards


    // main menu messages:

    private SendMessage sendMenuMessage(Message request, User user) {
        Button userChoice;
        try {
            userChoice = Button.of(request.getText());
        } catch (NoSuchButtonException e) {
            log.debug(e);
            return new SendMessage()
                    .setText(messages.getString("main.menu.invalid.choice"))
                    .setReplyMarkup(getMenuKeyboard());
        }
        SendMessage response = null;
        switch (userChoice) {
            case CURRENT_LESSON:
                response = messageOnChoseCurrentLesson(request, user);
                break;
            case NEXT_LESSON:
                response = messageOnChoseNextLesson(request, user);
                break;
            case TODAY_LESSONS:
                response = messageOnChoseTodayLessons(request, user);
                break;
            case TOMORROW_LESSONS:
                response = messageOnChoseTomorrowLessons(request, user);
                break;
            case REMAINING_LESSONS_OF_WEEK:
                response = messageOnChoseRemainingLessonsOfWeek(request, user);
                break;
            case LESSONS_OF_SPECIFIED_DAY:
                response = messageOnChoseLessonsOfSpecifiedDay(request, user);
                break;
            case UNREGISTER:
                response = messageOnUnregister(request, user);
                break;
            case FEEDBACK:
                response = messageOnFeedback(request, user);
                break;
        }
        return response;
    }

    private SendMessage messageOnChoseCurrentLesson(Message request, User user) {
        StringBuilder responseStringBuilder = new StringBuilder();
        Optional<Cell> currentLesson = timingService.getCurrentLesson(user);
        if (currentLesson.isPresent()) {
            responseStringBuilder.append(messages.getString("current.lesson")).append("\n\n")
                    .append(currentLesson.get().toLongString());
        } else {
            responseStringBuilder.append(messages.getString("current.lesson.not.found"));
        }
        return new SendMessage()
                .setText(responseStringBuilder.toString())
                .setReplyMarkup(getMenuKeyboard());
    }

    private SendMessage messageOnChoseNextLesson(Message request, User user) {
        StringBuilder responseStringBuilder = new StringBuilder();
        Optional<Cell> nextLesson = timingService.getNextLesson(user);
        if (nextLesson.isPresent()) {
            responseStringBuilder.append(messages.getString("next.lesson")).append("\n\n")
                    .append(nextLesson.get().toLongString());
        } else {
            responseStringBuilder.append(messages.getString("next.lesson.not.found"));
        }
        return new SendMessage()
                .setText(responseStringBuilder.toString())
                .setReplyMarkup(getMenuKeyboard());
    }

    private SendMessage messageOnChoseTodayLessons(Message request, User user) {
        StringBuilder responseStringBuilder = new StringBuilder();
        List<Cell> todayLessons = timingService.getTodayLessons(user);
        if (todayLessons.isEmpty()) {
            responseStringBuilder.append(messages.getString("today.lessons.not.found"));
        } else {
            responseStringBuilder.append(messages.getString("today.lessons")).append("\n\n");
            todayLessons.forEach(cell -> responseStringBuilder.append(cell).append("\n\n"));
        }
        return new SendMessage()
                .setText(responseStringBuilder.toString())
                .setReplyMarkup(getMenuKeyboard());
    }

    private SendMessage messageOnChoseTomorrowLessons(Message request, User user) {
        StringBuilder responseStringBuilder = new StringBuilder();
        List<Cell> tomorrowLessons = timingService.getTomorrowOrMondayLessons(user);
        if (tomorrowLessons.isEmpty()) {
            responseStringBuilder.append(messages.getString("tomorrow.lessons.not.found"));
        } else {
            responseStringBuilder.append(DateUtils.getCurrentDayOfWeek() == DayOfWeek.SATURDAY ?
                    messages.getString("tomorrow.lessons.monday") :
                    messages.getString("tomorrow.lessons")).append("\n\n");
            tomorrowLessons.forEach(cell -> responseStringBuilder.append(cell.toString()).append("\n\n"));
        }
        return new SendMessage()
                .setText(responseStringBuilder.toString())
                .setReplyMarkup(getMenuKeyboard());
    }

    private SendMessage messageOnChoseRemainingLessonsOfWeek(Message request, User user) {
        StringBuilder responseStringBuilder = new StringBuilder();
        Map<DayOfWeek, List<Cell>> remainingLessonsOfWeek = timingService.getRemainingLessonsOfWeek(user);
        if (remainingLessonsOfWeek.isEmpty()) {
            responseStringBuilder.append(messages.getString("remaining.lessons.of.week.not.found"));
        } else {
            responseStringBuilder.append(messages.getString("remaining.lessons.of.week.header")).append("\n\n");
            remainingLessonsOfWeek.forEach((day, cells) -> {
                String dayOfWeek = BaseUtils.getLocalizedNameOfDay(day, messages);
                responseStringBuilder.append("\uD83D\uDD36 *")
                        .append(BaseUtils.capitalizeString(dayOfWeek))
                        .append(":*\n\n");
                cells.forEach(cell -> responseStringBuilder.append(cell.toShortString()).append('\n'));
                responseStringBuilder.append("\n");
            });
        }
        return new SendMessage()
                .setText(responseStringBuilder.toString())
                .setReplyMarkup(getMenuKeyboard());
    }

    private SendMessage messageOnChoseLessonsOfSpecifiedDay(Message request, User user) {
        user.setState(CHOOSING_DAY_OF_WEEK);
        userService.updateUser(user);
        String messageText = messages.getString("timing.specified.day.choose.day");
        if (DateUtils.getCurrentDayOfWeek() == DayOfWeek.SUNDAY) {
            String nextWeekSign = BaseUtils.getLocalizedNameOfWeekSign(facultyService.getNextWeekSign(user.getFaculty()), messages);
            messageText += messages.getString("timing.specified.day.choose.day.warning")
                    .replaceAll("%week%", nextWeekSign);
        } else {
            String currentWeekSign = BaseUtils.getLocalizedNameOfWeekSign(facultyService.getCurrentWeekSign(user.getFaculty()), messages);
            messageText += messages.getString("timing.specified.day.choose.day.current.week.sign")
                    .replaceAll("%week%", currentWeekSign);
        }
        return new SendMessage()
                .setText(messageText)
                .setReplyMarkup(getKeyboardOfWorkingDaysForTwoWeeks());
    }

    private SendMessage messageOnUnregister(Message request, User user) {
        user.setState(CHOOSING_FACULTY);
        userService.updateUser(user);
        return new SendMessage()
                .setText(messages.getString("unregister"))
                .setReplyMarkup(getFacultiesKeyboard());
    }

    private SendMessage messageOnFeedback(Message request, User user) {
        return new SendMessage()
                .setText(messages.getString("feedback"))
                .setReplyMarkup(getMenuKeyboard());
    }

    // :main menu messages


    // main menu keyboards:

    private ReplyKeyboardMarkup getMenuKeyboard() {
        KeyboardManager keyboardManager = new KeyboardManagerImpl(2);
        keyboardManager.addItem(Button.CURRENT_LESSON.getLocalizedName());
        keyboardManager.addItem(Button.NEXT_LESSON.getLocalizedName());
        keyboardManager.addItemOnNewLine(Button.TODAY_LESSONS.getLocalizedName());
        keyboardManager.addItemOnNewLine(Button.TOMORROW_LESSONS.getLocalizedName());
        keyboardManager.addItemOnNewLine(Button.REMAINING_LESSONS_OF_WEEK.getLocalizedName());
        keyboardManager.addItemOnNewLine(Button.LESSONS_OF_SPECIFIED_DAY.getLocalizedName());
        keyboardManager.addItemOnNewLine(Button.UNREGISTER.getLocalizedName());
        keyboardManager.addItem(Button.FEEDBACK.getLocalizedName());
        return keyboardManager.getKeyboard();
    }

    private ReplyKeyboardMarkup getKeyboardOfWorkingDaysForTwoWeeks() { // oh god...
        KeyboardManager keyboardManager = new KeyboardManagerImpl(2);
        keyboardManager.addItem(Button.MONDAY_PLUS_WEEK.getLocalizedName());
        keyboardManager.addItem(Button.MONDAY_MINUS_WEEK.getLocalizedName());
        keyboardManager.addItem(Button.TUESDAY_PLUS_WEEK.getLocalizedName());
        keyboardManager.addItem(Button.TUESDAY_MINUS_WEEK.getLocalizedName());
        keyboardManager.addItem(Button.WEDNESDAY_PLUS_WEEK.getLocalizedName());
        keyboardManager.addItem(Button.WEDNESDAY_MINUS_WEEK.getLocalizedName());
        keyboardManager.addItem(Button.THURSDAY_PLUS_WEEK.getLocalizedName());
        keyboardManager.addItem(Button.THURSDAY_MINUS_WEEK.getLocalizedName());
        keyboardManager.addItem(Button.FRIDAY_PLUS_WEEK.getLocalizedName());
        keyboardManager.addItem(Button.FRIDAY_MINUS_WEEK.getLocalizedName());
        keyboardManager.addItem(Button.SATURDAY_PLUS_WEEK.getLocalizedName());
        keyboardManager.addItem(Button.SATURDAY_MINUS_WEEK.getLocalizedName());
        return keyboardManager.getKeyboard();
    }

    // :main menu keyboards


    private SendMessage messageOnChoosingDayOfWeek(Message request, User user) {
        String messageText = request.getText();
        String[] splittedButtonName;
        try {
            splittedButtonName = Button.of(messageText).toString().split("_");
        } catch (NoSuchButtonException e) {
            log.debug(e);
            return new SendMessage().setText(messages.getString("timing.specified.day.invalid"));
        }
        DayOfWeek dayOfWeek = DayOfWeek.valueOf(splittedButtonName[0]);
        WeekSign weekSign = WeekSign.valueOf(splittedButtonName[1]);
        List<Cell> lessonsOfSpecifiedDay = timingService.getLessonsOfSpecifiedDay(user, dayOfWeek, weekSign);
        StringBuilder stringBuilder = new StringBuilder(messages.getString("timing.specified.day")
                .replaceAll("%day%", BaseUtils.getLocalizedNameOfDayInAccusative(dayOfWeek, messages))
                .replaceAll("%week%", BaseUtils.getLocalizedNameOfWeekSign(weekSign, messages)))
                .append("\n\n");
        lessonsOfSpecifiedDay.forEach(cell -> stringBuilder.append(cell.toString()).append("\n\n"));
        user.setState(MAIN_MENU);
        userService.updateUser(user);
        return new SendMessage()
                .setText(stringBuilder.toString())
                .setReplyMarkup(getMenuKeyboard());
    }
}

