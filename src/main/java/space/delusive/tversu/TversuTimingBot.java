package space.delusive.tversu;

import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage.SendMessageBuilder;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import space.delusive.tversu.component.MetricsRegistrar;
import space.delusive.tversu.dto.Cell;
import space.delusive.tversu.dto.DayOfWeek;
import space.delusive.tversu.dto.WeekSign;
import space.delusive.tversu.entity.User;
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
    private static final String MARKDOWN_PARSE_MODE = "Markdown";

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
        if (!isTextMessage || !update.getMessage().isUserMessage()) {
            return;
        }
        Message message = update.getMessage();
        org.telegram.telegrambots.meta.api.objects.User author = message.getFrom();
        log.info("User (ID: {}, FN: {}, LN: {}) sent message with text: {}",
                author.getId(), author.getFirstName(), author.getLastName(), message.getText());
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
        long userId = msg.getFrom().getId();
        metricsRegistrar.registerUserCall(userId);

        User user = getUser(userId);
        execute(getResponseBasedOnUserState(msg, user)
                .chatId(Long.toString(userId))
                .parseMode(MARKDOWN_PARSE_MODE)
                .build());

        long timeConsumed = System.currentTimeMillis() - startTime;
        metricsRegistrar.registerTimeConsumed(timeConsumed);
    }

    private User getUser(long userId) {
        User user;
        val optionalUser = userService.getUserById(userId);
        if (optionalUser.isPresent()) {
            user = optionalUser.get();
            updateLastMessageDate(user);
        } else {
            user = registerAndGetUser(userId);
        }
        return user;
    }

    private void updateLastMessageDate(User user) {
        user.setLastMessageDate(Date.valueOf(LocalDate.now()));
        userService.updateUser(user);
    }

    private SendMessageBuilder getResponseBasedOnUserState(Message message, User user) {
        try {
            switch (user.getState()) {
                case START:
                case CHOOSING_FACULTY:
                case CHOOSING_PROGRAM:
                case CHOOSING_COURSE:
                case CHOOSING_GROUP:
                case CHOOSING_SUBGROUP:
                    return messageOnRegistering(message, user);
                case MAIN_MENU:
                    return sendMenuMessage(message, user);
                case CHOOSING_DAY_OF_WEEK:
                    return messageOnChoosingDayOfWeek(message, user);
                case SETTINGS_MENU:
                    return messageOnSettingsMenu(message, user);
            }
        } catch (SoldisWhatTheFuckException e) {
            log.warn(e);
            return messageOnSoldisWhatTheFuckException(user);
        }
        return null;
    }

    private User registerAndGetUser(long userId) {
        return userService.createNewUser(userId)
                .orElseThrow(() -> new IllegalStateException("Smth went wrong while saving the user"));
    }

    private void updateUserWithState(User user, BotState state) {
        user.setState(state);
        userService.updateUser(user);
    }


    // exception messages:

    private SendMessageBuilder messageOnSoldisWhatTheFuckException(User user) {
        updateUserWithState(user, BotState.MAIN_MENU);
        return SendMessage.builder()
                .text(messages.getString("groups.were.renamed"))
                .replyMarkup(getMenuKeyboard());
    }

    // :exception messages


    // register messages:

    private SendMessageBuilder messageOnRegistering(Message msg, User user) {
        BotState state = user.getState();
        metricsRegistrar.registerPath("/reg/" + state.name().toLowerCase());
        switch (state) {
            case START:
                return messageOnStart(user);
            case CHOOSING_FACULTY:
                return messageOnChoosingFaculty(msg, user);
            case CHOOSING_PROGRAM:
                return messageOnChoosingProgram(msg, user);
            case CHOOSING_COURSE:
                return messageOnChoosingCourse(msg, user);
            case CHOOSING_GROUP:
                return messageOnChoosingGroup(msg, user);
            case CHOOSING_SUBGROUP:
                return messageOnChoosingSubgroup(msg, user);
        }
        return null;
    }

    private SendMessageBuilder messageOnStart(User user) {
        SendMessageBuilder response = SendMessage.builder()
                .text(messages.getString("start"))
                .replyMarkup(getFacultiesKeyboard())
                .chatId(Long.toString(user.getId()));
        updateUserWithState(user, BotState.CHOOSING_FACULTY);
        return response;
    }

    private SendMessageBuilder messageOnChoosingFaculty(Message request, User user) {
        SendMessageBuilder response = SendMessage.builder();
        if (!facultyService.getFaculties().contains(request.getText())) {
            response.text(messages.getString("invalid.faculty"))
                    .replyMarkup(getFacultiesKeyboard());
        } else {
            user.setFaculty(request.getText());
            updateUserWithState(user, BotState.CHOOSING_PROGRAM);
            response.text(messages.getString("choose.program"))
                    .replyMarkup(getProgramKeyboard(request.getText()));
        }
        return response;
    }

    private SendMessageBuilder messageOnChoosingProgram(Message request, User user) {
        return processBackButtonWhileRegister(request, user, getFacultiesKeyboard(), "back.to.faculties")
                .orElseGet(() -> {
                    SendMessageBuilder response = SendMessage.builder();
                    if (!facultyService.getPrograms(user.getFaculty()).contains(request.getText())) {
                        response.text(messages.getString("invalid.program"))
                                .replyMarkup(getProgramKeyboard(user.getFaculty()));
                    } else {
                        user.setProgram(request.getText());
                        updateUserWithState(user, BotState.CHOOSING_COURSE);
                        response.text(messages.getString("choose.course"))
                                .replyMarkup(getCoursesKeyboard(user));
                    }
                    return response;
                });
    }

    private SendMessageBuilder messageOnChoosingCourse(Message request, User user) {
        Supplier<SendMessageBuilder> sendMessageSupplier = () -> {
            SendMessageBuilder response = SendMessage.builder();
            try {
                int course = Integer.parseInt(request.getText());
                if (facultyService.getCourses(user.getFaculty(), user.getProgram()).contains(course)) {
                    user.setCourse(course);
                    updateUserWithState(user, BotState.CHOOSING_GROUP);
                    response.text(messages.getString("choose.group"))
                            .replyMarkup(getGroupsKeyboard(user));
                } else {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                response.text(messages.getString("invalid.course"))
                        .replyMarkup(getCoursesKeyboard(user));
            }
            return response;
        };
        return processBackButtonWhileRegister(request, user, getProgramKeyboard(user.getFaculty()), "back.to.programs")
                .orElseGet(sendMessageSupplier);
    }

    private SendMessageBuilder messageOnChoosingGroup(Message request, User user) {
        Supplier<SendMessageBuilder> sendMessageSupplier = () -> {
            SendMessageBuilder response = SendMessage.builder();
            var groups = facultyService.getGroups(user.getFaculty(), user.getProgram(), user.getCourse());
            if (!groups.contains(request.getText())) {
                response.text(messages.getString("invalid.group"))
                        .replyMarkup(getGroupsKeyboard(user));
            } else {
                user.setGroup(request.getText());
                int subgroups = facultyService.getSubgroupsCount(user.getFaculty(), user.getProgram(), user.getCourse(), user.getGroup());
                if (subgroups == 0) {
                    user.setSubgroup(0);
                    updateUserWithState(user, BotState.MAIN_MENU);
                    response.text(messages.getString("register.end"))
                            .replyMarkup(getMenuKeyboard());
                } else {
                    updateUserWithState(user, BotState.CHOOSING_SUBGROUP);
                    response.text(messages.getString("choose.subgroup"))
                            .replyMarkup(getSubgroupsKeyboard(user));
                }
            }
            return response;
        };
        return processBackButtonWhileRegister(request, user, getCoursesKeyboard(user), "back.to.courses")
                .orElseGet(sendMessageSupplier);
    }

    private SendMessageBuilder messageOnChoosingSubgroup(Message request, User user) {
        Supplier<SendMessageBuilder> sendMessageSupplier = () -> {
            SendMessageBuilder response = SendMessage.builder();
            int subgroups = facultyService.getSubgroupsCount(user.getFaculty(), user.getProgram(), user.getCourse(), user.getGroup());
            int subgroup;
            try {
                subgroup = Integer.parseInt(request.getText());
                if (subgroup > subgroups || subgroup < 1) {
                    throw new NumberFormatException();
                } else {
                    user.setSubgroup(subgroup);
                    updateUserWithState(user, BotState.MAIN_MENU);
                    response.text(messages.getString("register.end"))
                            .replyMarkup(getMenuKeyboard());
                }
            } catch (NumberFormatException e) {
                response.text(messages.getString("invalid.subgroup"))
                        .replyMarkup(getSubgroupsKeyboard(user));
            }
            return response;
        };
        return processBackButtonWhileRegister(request, user, getGroupsKeyboard(user), "back.to.groups")
                .orElseGet(sendMessageSupplier);
    }

    private Optional<SendMessageBuilder> processBackButtonWhileRegister(Message request, User user, ReplyKeyboardMarkup replyKeyboardMarkup, String messagePlaceholder) {
        boolean isNotBackButtonPressed = !request.getText().equals(Button.TO_PREVIOUS_STAGE.getLocalizedName());
        if (isNotBackButtonPressed) {
            return Optional.empty();
        }
        updateUserWithState(user, BotState.getByOrdinal(user.getState().ordinal() - 1));
        SendMessageBuilder response = SendMessage.builder()
                .text(messages.getString(messagePlaceholder))
                .replyMarkup(replyKeyboardMarkup);
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

    private SendMessageBuilder sendMenuMessage(Message request, User user) throws SoldisWhatTheFuckException {
        Button userChoice;
        try {
            userChoice = Button.of(request.getText());
        } catch (NoSuchButtonException e) {
            log.debug(e);
            return SendMessage.builder()
                    .text(messages.getString("main.menu.invalid.choice"))
                    .replyMarkup(getMenuKeyboard());
        }
        metricsRegistrar.registerPath("/menu/" + userChoice.name().toLowerCase());
        switch (userChoice) {
            case CURRENT_LESSON:
                return messageOnChoseCurrentLesson(user);
            case NEXT_LESSON:
                return messageOnChoseNextLesson(user);
            case TODAY_LESSONS:
                return messageOnChoseTodayLessons(user);
            case TOMORROW_LESSONS:
                return messageOnChoseTomorrowLessons(user);
            case REMAINING_LESSONS_OF_WEEK:
                return messageOnChoseRemainingLessonsOfWeek(user);
            case LESSONS_OF_SPECIFIED_DAY:
                return messageOnChoseLessonsOfSpecifiedDay(user);
            case SETTINGS:
                return messageOnSettings(user);
            case FEEDBACK:
                return messageOnFeedback();
        }
        return null;
    }

    private SendMessageBuilder messageOnChoseCurrentLesson(User user) throws SoldisWhatTheFuckException {
        StringBuilder responseStringBuilder = new StringBuilder();
        Optional<Cell> currentLesson = timingService.getCurrentLesson(user);
        if (currentLesson.isPresent()) {
            responseStringBuilder.append(messages.getString("current.lesson")).append("\n\n")
                    .append(currentLesson.get().toLongString());
        } else {
            responseStringBuilder.append(messages.getString("current.lesson.not.found"));
        }
        return SendMessage.builder()
                .text(responseStringBuilder.toString())
                .replyMarkup(getMenuKeyboard());
    }

    private SendMessageBuilder messageOnChoseNextLesson(User user) throws SoldisWhatTheFuckException {
        StringBuilder responseStringBuilder = new StringBuilder();
        Optional<Cell> nextLesson = timingService.getNextLesson(user);
        if (nextLesson.isPresent()) {
            responseStringBuilder.append(messages.getString("next.lesson")).append("\n\n")
                    .append(nextLesson.get().toLongString());
        } else {
            responseStringBuilder.append(messages.getString("next.lesson.not.found"));
        }
        return SendMessage.builder()
                .text(responseStringBuilder.toString())
                .replyMarkup(getMenuKeyboard());
    }

    private SendMessageBuilder messageOnChoseTodayLessons(User user) throws SoldisWhatTheFuckException {
        StringBuilder responseStringBuilder = new StringBuilder();
        List<Cell> todayLessons = timingService.getTodayLessons(user);
        if (todayLessons.isEmpty()) {
            responseStringBuilder.append(messages.getString("today.lessons.not.found"));
        } else {
            responseStringBuilder.append(messages.getString("today.lessons")).append("\n\n");
            todayLessons.forEach(cell -> responseStringBuilder.append(cell).append("\n\n"));
        }
        return SendMessage.builder()
                .text(responseStringBuilder.toString())
                .replyMarkup(getMenuKeyboard());
    }

    private SendMessageBuilder messageOnChoseTomorrowLessons(User user) throws SoldisWhatTheFuckException {
        StringBuilder builder = new StringBuilder();
        List<Cell> tomorrowLessons = timingService.getTomorrowOrMondayLessons(user);
        boolean isTodaySaturday = DateUtils.getCurrentDayOfWeek() == DayOfWeek.SATURDAY;
        if (tomorrowLessons.isEmpty()) {
            builder.append(messages.getString("tomorrow.lessons.not.found"));
        } else {
            builder.append(isTodaySaturday ?
                    messages.getString("tomorrow.lessons.monday") :
                    messages.getString("tomorrow.lessons")).append("\n\n");
            tomorrowLessons.forEach(cell -> builder.append(cell.toString()).append("\n\n"));
        }
        if (DateUtils.isNowBeginningOfDay() && !isTodaySaturday) {
            String currentDayName = BaseUtils.getLocalizedNameOfDayInAccusative(DateUtils.getCurrentDayOfWeek(), messages);
            String tomorrowDayName = BaseUtils.getLocalizedNameOfDayInAccusative(DateUtils.getCurrentDayOfWeek().next(), messages);
            builder.append("\n")
                    .append(messages.getString("tomorrow.lessons.day.just.began")
                            .replace("%tomorrow%", tomorrowDayName)
                            .replace("%today%", currentDayName));
        }
        return SendMessage.builder()
                .text(builder.toString())
                .replyMarkup(getMenuKeyboard());
    }

    private SendMessageBuilder messageOnChoseRemainingLessonsOfWeek(User user) throws SoldisWhatTheFuckException {
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
        return SendMessage.builder()
                .text(responseStringBuilder.toString())
                .replyMarkup(getMenuKeyboard());
    }

    private SendMessageBuilder messageOnChoseLessonsOfSpecifiedDay(User user) {
        updateUserWithState(user, BotState.CHOOSING_DAY_OF_WEEK);
        String messageText = messages.getString("timing.specified.day.choose.day");
        if (DateUtils.getCurrentDayOfWeek() == DayOfWeek.SUNDAY) {
            WeekSign nextWeekSign = facultyService.getNextWeekSign(user.getFaculty());
            String localizedNextWeekSign = BaseUtils.getLocalizedNameOfWeekSign(nextWeekSign, messages);
            messageText += messages.getString("timing.specified.day.choose.day.warning")
                    .replace("%week%", localizedNextWeekSign);
        } else {
            WeekSign currentWeekSign = facultyService.getCurrentWeekSign(user.getFaculty());
            String localizedCurrentWeekSign = BaseUtils.getLocalizedNameOfWeekSign(currentWeekSign, messages);
            messageText += messages.getString("timing.specified.day.choose.day.current.week.sign")
                    .replace("%week%", localizedCurrentWeekSign);
        }
        return SendMessage.builder()
                .text(messageText)
                .replyMarkup(getKeyboardOfWorkingDaysForTwoWeeks());
    }

    private SendMessageBuilder messageOnSettings(User user) {
        String textResponse = messages.getString("settings")
                .replace("%faculty%", user.getFaculty())
                .replace("%program%", user.getProgram())
                .replace("%course%", String.valueOf(user.getCourse()))
                .replace("%group%", user.getGroup())
                .replace("%subgroup%", String.valueOf(user.getSubgroup()));
        updateUserWithState(user, BotState.SETTINGS_MENU);
        return SendMessage.builder()
                .text(textResponse)
                .replyMarkup(getSettingsMenuKeyboard());
    }

    private SendMessageBuilder messageOnFeedback() {
        return SendMessage.builder()
                .text(messages.getString("feedback"))
                .replyMarkup(getMenuKeyboard());
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

    private ReplyKeyboardMarkup getKeyboardOfWorkingDaysForTwoWeeks() {
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

    private SendMessageBuilder messageOnChoosingDayOfWeek(Message request, User user) throws SoldisWhatTheFuckException { // TODO: 3/3/2020 refactor this shit
        metricsRegistrar.registerPath("/main/day_schedule");
        String messageText = request.getText();
        String[] splitButtonName;
        try {
            splitButtonName = Button.of(messageText).toString().split("_");
        } catch (NoSuchButtonException e) {
            log.debug(e);
            return SendMessage.builder()
                    .text(messages.getString("timing.specified.day.invalid"))
                    .replyMarkup(getKeyboardOfWorkingDaysForTwoWeeks());
        }
        DayOfWeek dayOfWeek = DayOfWeek.valueOf(splitButtonName[0]);
        WeekSign weekSign = WeekSign.valueOf(splitButtonName[1]);
        List<Cell> lessonsOfSpecifiedDay = timingService.getLessonsOfSpecifiedDay(user, dayOfWeek, weekSign);
        SendMessageBuilder response = SendMessage.builder()
                .chatId(Long.toString(user.getId()));
        if (lessonsOfSpecifiedDay.isEmpty()) {
            log.warn("There are no lessons found for faculty \"{}\", course \"{}\", group \"{}\" and subgroup \"{}\"",
                    user.getFaculty(), user.getCourse(), user.getGroup(), user.getSubgroup());
            response.text(BaseUtils.getFormattedMessageInAccusative(dayOfWeek, weekSign, messages,
                    "timing.specified.day.no.lessons"));
        } else {
            StringBuilder stringBuilder = new StringBuilder(
                    BaseUtils.getFormattedMessageInAccusative(dayOfWeek, weekSign, messages, "timing.specified.day")
                            .replaceAll("%day%", BaseUtils.getLocalizedNameOfDayInAccusative(dayOfWeek, messages))
                            .replaceAll("%week%", BaseUtils.getLocalizedNameOfWeekSign(weekSign, messages)))
                    .append("\n\n");
            lessonsOfSpecifiedDay.forEach(cell -> stringBuilder.append(cell.toString()).append("\n\n"));
            response.text(stringBuilder.toString());
        }
        updateUserWithState(user, BotState.MAIN_MENU);
        return response.replyMarkup(getMenuKeyboard());
    }

    // :choosing day of week while want to get timing of specific day


    // settings menu:

    private SendMessageBuilder messageOnSettingsMenu(Message request, User user) {
        Button userChoice;
        try {
            userChoice = Button.of(request.getText());
        } catch (NoSuchButtonException e) {
            log.debug(e);
            return SendMessage.builder()
                    .text(messages.getString("settings.menu.invalid.choice"))
                    .replyMarkup(getMenuKeyboard());
        }
        metricsRegistrar.registerPath("/settings/" + userChoice.name().toLowerCase());
        SendMessageBuilder response = null;
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

    private SendMessageBuilder messageOnChangeSettings(User user) {
        updateUserWithState(user, BotState.CHOOSING_FACULTY);
        return SendMessage.builder()
                .text(messages.getString("change.settings"))
                .replyMarkup(getFacultiesKeyboard());
    }

    private SendMessageBuilder messageOnBackToMainMenu(User user) {
        updateUserWithState(user, BotState.MAIN_MENU);
        return SendMessage.builder()
                .text(messages.getString("settings.menu.back.to.main.menu"))
                .replyMarkup(getMenuKeyboard());
    }

    // :settings menu
}

