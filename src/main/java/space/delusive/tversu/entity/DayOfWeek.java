package space.delusive.tversu.entity;

public enum DayOfWeek {
    SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY;

    public DayOfWeek next() {
        DayOfWeek[] days = DayOfWeek.values();
        int i = 0;
        for (; days[i] != this; i++);
        return days[(i + 1) % days.length];
    }
}
