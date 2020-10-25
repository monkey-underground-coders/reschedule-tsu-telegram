package space.delusive.tversu;

public enum BotState {
    START,
    CHOOSING_FACULTY,
    CHOOSING_PROGRAM,
    CHOOSING_COURSE,
    CHOOSING_GROUP,
    CHOOSING_SUBGROUP,
    MAIN_MENU,
    CHOOSING_DAY_OF_WEEK,
    SETTINGS_MENU;

    public static BotState getByOrdinal(int ordinal) {
        return BotState.values()[ordinal];
    }
}
