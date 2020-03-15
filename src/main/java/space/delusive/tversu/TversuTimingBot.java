package space.delusive.tversu;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import space.delusive.tversu.component.MetricsRegistrar;
import space.delusive.tversu.entity.Cell;
import space.delusive.tversu.entity.DayOfWeek;
import space.delusive.tversu.entity.User;
import space.delusive.tversu.entity.WeekSign;
import space.delusive.tversu.exception.NoSuchButtonException;
import space.delusive.tversu.exception.SoldisWhatTheFuckException;
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
import java.util.function.Supplier;

@Component
@Log4j2
public class TversuTimingBot extends TelegramLongPollingBot {
    private final DataManager config;
    private final DataManager messages;
    private final UserService userService;
    private final TimingService timingService;
    private final FacultyService facultyService;
    private final MetricsRegistrar metricsRegistrar;

    @Autowired
    public TversuTimingBot(@Qualifier("options") DefaultBotOptions options,
                           @Qualifier("config") DataManager config,
                           @Qualifier("messages") DataManager messages,
                           UserService userService,
                           FacultyService facultyService,
                           TimingService timingService,
                           MetricsRegistrar metricsRegistrar) {
        super(options);
        this.config = config;
        this.messages = messages;
        this.userService = userService;
        this.facultyService = facultyService;
        this.timingService = timingService;
        this.metricsRegistrar = metricsRegistrar;
    }


    @Override
    public void onUpdateReceived(Update update) {
        boolean isTextMessage = update.hasMessage() && update.getMessage().hasText();
        if (!isTextMessage || !update.getMessage().isUserMessage()) return;
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
        long startTime = System.currentTimeMillis();
        Integer userId = msg.getFrom().getId();
        User user = userService.getUserById(userId);
        metricsRegistrar.registerUserCall(userId);
        if (user == null) { //user doesn't exist
            user = registerAndGetUser(userId);
        } else {
            user.setLastMessageDate(Date.valueOf(LocalDate.now()));
            userService.updateUser(user);
        }
        SendMessage response = null;
        try {
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
                case SETTINGS_MENU:
                    response = messageOnSettingsMenu(msg, user);
                    break;
            }
        } catch (SoldisWhatTheFuckException e) {
            log.debug(e);
            response = messageOnSoldisWhatTheFuckException(user);
        }
        response.setChatId(msg.getChatId())
                .enableMarkdown(true);
        execute(response);
        long timeConsumed = System.currentTimeMillis() - startTime;
        metricsRegistrar.registerTimeConsumed(timeConsumed);
    }

    private User registerAndGetUser(long userId) {
        Date currDate = Date.valueOf(LocalDate.now());
        userService.addUser(new User(userId, BotState.START, null, null, 0, null, 0, currDate, currDate));
        return userService.getUserById(userId);
    }

    private void updateUserWithState(User user, BotState state) {
        user.setState(state);
        userService.updateUser(user);
    }


    // exception messages:

    private SendMessage messageOnSoldisWhatTheFuckException(User user) {
        updateUserWithState(user, BotState.MAIN_MENU);
        return new SendMessage()
                .setText(messages.getString("groups.was.renamed"))
                .setReplyMarkup(getMenuKeyboard());
    }

    // :exception messages


    // register messages:

    private SendMessage messageOnRegistering(Message msg, User user) {
        SendMessage response = null;
        BotState state = user.getState();
        metricsRegistrar.registerPath("/reg/" + state.name().toLowerCase());
        switch (state) {
            case START:
                response = messageOnStart(user);
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

    private SendMessage messageOnStart(User user) {
        SendMessage response = new SendMessage();
        response.setText(messages.getString("start"))
                .setReplyMarkup(getFacultiesKeyboard());
        updateUserWithState(user, BotState.CHOOSING_FACULTY);
        return response;
    }

    private SendMessage messageOnChoosingFaculty(Message request, User user) {
        SendMessage response = new SendMessage();
        if (!facultyService.getFaculties().contains(request.getText())) {
            response.setText(messages.getString("invalid.faculty"));
            response.setReplyMarkup(getFacultiesKeyboard());
        } else {
            user.setFaculty(request.getText());
            updateUserWithState(user, BotState.CHOOSING_PROGRAM);
            response.setText(messages.getString("choose.program"));
            response.setReplyMarkup(getProgramKeyboard(request.getText()));
        }
        return response;
    }

    private SendMessage messageOnChoosingProgram(Message request, User user) {
        return processBackButtonWhileRegister(request, user, getFacultiesKeyboard(), "back.to.faculties")
                .orElseGet(() -> {
                    SendMessage response = new SendMessage();
                    if (!facultyService.getPrograms(user.getFaculty()).contains(request.getText())) {
                        response.setText(messages.getString("invalid.program"));
                        response.setReplyMarkup(getProgramKeyboard(user.getFaculty()));
                    } else {
                        user.setProgram(request.getText());
                        updateUserWithState(user, BotState.CHOOSING_COURSE);
                        response.setText(messages.getString("choose.course"));
                        response.setReplyMarkup(getCoursesKeyboard(user));
                    }
                    return (response);
                });
    }

    private SendMessage messageOnChoosingCourse(Message request, User user) {
        Supplier<SendMessage> sendMessageSupplier = () -> {
            SendMessage response = new SendMessage();
            try {
                int course = Integer.parseInt(request.getText());
                if (facultyService.getCourses(user.getFaculty(), user.getProgram()).contains(course)) {
                    user.setCourse(course);
                    updateUserWithState(user, BotState.CHOOSING_GROUP);
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
        };
        return processBackButtonWhileRegister(request, user, getProgramKeyboard(user.getFaculty()), "back.to.programs")
                .orElseGet(sendMessageSupplier);
    }

    private SendMessage messageOnChoosingGroup(Message request, User user) {
        Supplier<SendMessage> sendMessageSupplier = () -> {
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
                    updateUserWithState(user, BotState.MAIN_MENU);
                    response.setText(messages.getString("register.end"));
                    response.setReplyMarkup(getMenuKeyboard());
                } else {
                    updateUserWithState(user, BotState.CHOOSING_SUBGROUP);
                    response.setText(messages.getString("choose.subgroup"));
                    response.setReplyMarkup(getSubgroupsKeyboard(user));
                }
            }
            return response;
        };
        return processBackButtonWhileRegister(request, user, getCoursesKeyboard(user), "back.to.courses")
                .orElseGet(sendMessageSupplier);
    }

    private SendMessage messageOnChoosingSubgroup(Message request, User user) {
        Supplier<SendMessage> sendMessageSupplier = () -> {
            SendMessage response = new SendMessage();
            int subgroups = facultyService.getSubgroupsCount(user.getFaculty(), user.getProgram(), user.getCourse(), user.getGroup());
            int subgroup;
            try {
                subgroup = Integer.parseInt(request.getText());
                if (subgroup > subgroups || subgroup < 1) {
                    throw new NumberFormatException();
                } else {
                    user.setSubgroup(subgroup);
                    updateUserWithState(user, BotState.MAIN_MENU);
                    response.setText(messages.getString("register.end"));
                    response.setReplyMarkup(getMenuKeyboard());
                }
            } catch (NumberFormatException e) {
                response.setText(messages.getString("invalid.subgroup"));
                response.setReplyMarkup(getSubgroupsKeyboard(user));
            }
            return response;
        };
        return processBackButtonWhileRegister(request, user, getGroupsKeyboard(user), "back.to.groups")
                .orElseGet(sendMessageSupplier);
    }

    private Optional<SendMessage> processBackButtonWhileRegister(Message request, User user, ReplyKeyboardMarkup replyKeyboardMarkup, String messagePlaceholder) {
        boolean isNotBackButtonPressed = !request.getText().equals(Button.TO_PREVIOUS_STAGE.getLocalizedName());
        if (isNotBackButtonPressed) {
            return Optional.empty();
        }
        SendMessage response = new SendMessage();
        updateUserWithState(user, BotState.getByOrdinal(user.getState().ordinal() - 1));
        response.setText(messages.getString(messagePlaceholder));
        response.setReplyMarkup(replyKeyboardMarkup);
        return Optional.of(response);
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
        keyboardManager.addItemOnNewLine(Button.TO_PREVIOUS_STAGE);
        return keyboardManager.getKeyboard();
    }

    private ReplyKeyboardMarkup getCoursesKeyboard(User user) {
        var courses = facultyService.getCourses(user.getFaculty(), user.getProgram());
        KeyboardManager keyboardManager = new KeyboardManagerImpl(2);
        courses.forEach(course -> keyboardManager.addItem(course.toString()));
        keyboardManager.addItemOnNewLine(Button.TO_PREVIOUS_STAGE);
        return keyboardManager.getKeyboard();
    }

    private ReplyKeyboardMarkup getGroupsKeyboard(User user) {
        var groups = facultyService.getGroups(user.getFaculty(), user.getProgram(), user.getCourse());
        KeyboardManager keyboardManager = new KeyboardManagerImpl(2);
        groups.forEach(keyboardManager::addItem);
        keyboardManager.addItemOnNewLine(Button.TO_PREVIOUS_STAGE);
        return keyboardManager.getKeyboard();
    }

    private ReplyKeyboardMarkup getSubgroupsKeyboard(User user) {
        int subgroups = facultyService.getSubgroupsCount(user.getFaculty(), user.getProgram(), user.getCourse(), user.getGroup());
        KeyboardManager keyboardManager = new KeyboardManagerImpl(2);
        for (int i = 1; i <= subgroups; i++) keyboardManager.addItem(String.valueOf(i));
        keyboardManager.addItemOnNewLine(Button.TO_PREVIOUS_STAGE);
        return keyboardManager.getKeyboard();
    }

    // :register keyboards


    // main menu messages:

    private SendMessage sendMenuMessage(Message request, User user) throws SoldisWhatTheFuckException {
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
        metricsRegistrar.registerPath("/menu/" + userChoice.name().toLowerCase());
        switch (userChoice) {
            case CURRENT_LESSON:
                response = messageOnChoseCurrentLesson(user);
                break;
            case NEXT_LESSON:
                response = messageOnChoseNextLesson(user);
                break;
            case TODAY_LESSONS:
                response = messageOnChoseTodayLessons(user);
                break;
            case TOMORROW_LESSONS:
                response = messageOnChoseTomorrowLessons(user);
                break;
            case REMAINING_LESSONS_OF_WEEK:
                response = messageOnChoseRemainingLessonsOfWeek(user);
                break;
            case LESSONS_OF_SPECIFIED_DAY:
                response = messageOnChoseLessonsOfSpecifiedDay(user);
                break;
            case SETTINGS:
                response = messageOnSettings(user);
                break;
            case FEEDBACK:
                response = messageOnFeedback();
                break;
        }
        return response;
    }

    private SendMessage messageOnChoseCurrentLesson(User user) throws SoldisWhatTheFuckException {
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

    private SendMessage messageOnChoseNextLesson(User user) throws SoldisWhatTheFuckException {
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

    private SendMessage messageOnChoseTodayLessons(User user) throws SoldisWhatTheFuckException {
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

    private SendMessage messageOnChoseTomorrowLessons(User user) throws SoldisWhatTheFuckException {
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

    private SendMessage messageOnChoseRemainingLessonsOfWeek(User user) throws SoldisWhatTheFuckException {
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

    private SendMessage messageOnChoseLessonsOfSpecifiedDay(User user) {
        updateUserWithState(user, BotState.CHOOSING_DAY_OF_WEEK);
        String messageText = messages.getString("timing.specified.day.choose.day");
        if (DateUtils.getCurrentDayOfWeek() == DayOfWeek.SUNDAY) {
            WeekSign nextWeekSign = facultyService.getNextWeekSign(user.getFaculty());
            String localizedNextWeekSign = BaseUtils.getLocalizedNameOfWeekSign(nextWeekSign, messages);
            messageText += messages.getString("timing.specified.day.choose.day.warning")
                    .replaceAll("%week%", localizedNextWeekSign);
        } else {
            WeekSign currentWeekSign = facultyService.getCurrentWeekSign(user.getFaculty());
            String localizedCurrentWeekSign = BaseUtils.getLocalizedNameOfWeekSign(currentWeekSign, messages);
            messageText += messages.getString("timing.specified.day.choose.day.current.week.sign")
                    .replaceAll("%week%", localizedCurrentWeekSign);
        }
        return new SendMessage()
                .setText(messageText)
                .setReplyMarkup(getKeyboardOfWorkingDaysForTwoWeeks());
    }

    private SendMessage messageOnSettings(User user) {
        SendMessage response = new SendMessage();
        String textResponse = messages.getString("settings")
                .replaceAll("%faculty%", user.getFaculty())
                .replaceAll("%program%", user.getProgram())
                .replaceAll("%course%", String.valueOf(user.getCourse()))
                .replaceAll("%group%", user.getGroup())
                .replaceAll("%subgroup%", String.valueOf(user.getSubgroup()));
        updateUserWithState(user, BotState.SETTINGS_MENU);
        return response.setText(textResponse)
                .setReplyMarkup(getSettingsMenuKeyboard());
    }

    private SendMessage messageOnFeedback() {
        return new SendMessage()
                .setText(messages.getString("feedback"))
                .setReplyMarkup(getMenuKeyboard());
    }

    // :main menu messages


    // main menu keyboards:

    private ReplyKeyboardMarkup getMenuKeyboard() {
        KeyboardManager keyboardManager = new KeyboardManagerImpl(2);
        keyboardManager.addItem(Button.CURRENT_LESSON);
        keyboardManager.addItem(Button.NEXT_LESSON);
        keyboardManager.addItemOnNewLine(Button.TODAY_LESSONS);
        keyboardManager.addItemOnNewLine(Button.TOMORROW_LESSONS);
        keyboardManager.addItemOnNewLine(Button.REMAINING_LESSONS_OF_WEEK);
        keyboardManager.addItemOnNewLine(Button.LESSONS_OF_SPECIFIED_DAY);
        keyboardManager.addItemOnNewLine(Button.SETTINGS);
        keyboardManager.addItem(Button.FEEDBACK);
        return keyboardManager.getKeyboard();
    }

    private ReplyKeyboardMarkup getKeyboardOfWorkingDaysForTwoWeeks() { // oh god...
        KeyboardManager keyboardManager = new KeyboardManagerImpl(2);
        keyboardManager.addItem(Button.MONDAY_PLUS_WEEK);
        keyboardManager.addItem(Button.MONDAY_MINUS_WEEK);
        keyboardManager.addItem(Button.TUESDAY_PLUS_WEEK);
        keyboardManager.addItem(Button.TUESDAY_MINUS_WEEK);
        keyboardManager.addItem(Button.WEDNESDAY_PLUS_WEEK);
        keyboardManager.addItem(Button.WEDNESDAY_MINUS_WEEK);
        keyboardManager.addItem(Button.THURSDAY_PLUS_WEEK);
        keyboardManager.addItem(Button.THURSDAY_MINUS_WEEK);
        keyboardManager.addItem(Button.FRIDAY_PLUS_WEEK);
        keyboardManager.addItem(Button.FRIDAY_MINUS_WEEK);
        keyboardManager.addItem(Button.SATURDAY_PLUS_WEEK);
        keyboardManager.addItem(Button.SATURDAY_MINUS_WEEK);
        return keyboardManager.getKeyboard();
    }

    private ReplyKeyboardMarkup getSettingsMenuKeyboard() {
        KeyboardManager keyboardManager = new KeyboardManagerImpl(1);
        keyboardManager.addItem(Button.CHANGE_SETTINGS);
        keyboardManager.addItem(Button.BACK_TO_MAIN_MENU);
        return keyboardManager.getKeyboard();
    }

    // :main menu keyboards


    // choosing day of week while want to get timing of specific day:

    private SendMessage messageOnChoosingDayOfWeek(Message request, User user) throws SoldisWhatTheFuckException { // TODO: 3/3/2020 refactor this shit
        metricsRegistrar.registerPath("/main/day_schedule");
        String messageText = request.getText();
        String[] splitButtonName;
        try {
            splitButtonName = Button.of(messageText).toString().split("_");
        } catch (NoSuchButtonException e) {
            log.debug(e);
            return new SendMessage()
                    .setText(messages.getString("timing.specified.day.invalid"))
                    .setReplyMarkup(getKeyboardOfWorkingDaysForTwoWeeks());
        }
        DayOfWeek dayOfWeek = DayOfWeek.valueOf(splitButtonName[0]);
        WeekSign weekSign = WeekSign.valueOf(splitButtonName[1]);
        List<Cell> lessonsOfSpecifiedDay = timingService.getLessonsOfSpecifiedDay(user, dayOfWeek, weekSign);
        SendMessage response = new SendMessage();
        if (lessonsOfSpecifiedDay.isEmpty()) {
            log.warn("There are no lessons found for faculty \"{}\", course \"{}\", group \"{}\" and subgroup \"{}\"",
                    user.getFaculty(), user.getCourse(), user.getGroup(), user.getSubgroup());
            response.setText(BaseUtils.getFormattedMessageInAccusative(dayOfWeek, weekSign, messages,
                    "timing.specified.day.no.lessons"));
        } else {
            StringBuilder stringBuilder = new StringBuilder(
                    BaseUtils.getFormattedMessageInAccusative(dayOfWeek, weekSign, messages, "timing.specified.day")
                            .replaceAll("%day%", BaseUtils.getLocalizedNameOfDayInAccusative(dayOfWeek, messages))
                            .replaceAll("%week%", BaseUtils.getLocalizedNameOfWeekSign(weekSign, messages)))
                    .append("\n\n");
            lessonsOfSpecifiedDay.forEach(cell -> stringBuilder.append(cell.toString()).append("\n\n"));
            response.setText(stringBuilder.toString());
        }
        updateUserWithState(user, BotState.MAIN_MENU);
        return response.setReplyMarkup(getMenuKeyboard());
    }

    // :choosing day of week while want to get timing of specific day


    // settings menu:

    private SendMessage messageOnSettingsMenu(Message request, User user) {
        Button userChoice;
        try {
            userChoice = Button.of(request.getText());
        } catch (NoSuchButtonException e) {
            log.debug(e);
            return new SendMessage()
                    .setText(messages.getString("settings.menu.invalid.choice"))
                    .setReplyMarkup(getMenuKeyboard());
        }
        metricsRegistrar.registerPath("/settings/" + userChoice.name().toLowerCase());
        SendMessage response = null;
        switch (userChoice) {
            case CHANGE_SETTINGS:
                response = messageOnChangeSettings(user);
                break;
            case BACK_TO_MAIN_MENU:
                response = messageOnBackToMainMenu(user);
                break;
        }
        return response;
    }

    private SendMessage messageOnChangeSettings(User user) {
        updateUserWithState(user, BotState.CHOOSING_FACULTY);
        return new SendMessage()
                .setText(messages.getString("change.settings"))
                .setReplyMarkup(getFacultiesKeyboard());
    }

    private SendMessage messageOnBackToMainMenu(User user) {
        updateUserWithState(user, BotState.MAIN_MENU);
        return new SendMessage()
                .setText(messages.getString("settings.menu.back.to.main.menu"))
                .setReplyMarkup(getMenuKeyboard());
    }

    // :settings menu
}

