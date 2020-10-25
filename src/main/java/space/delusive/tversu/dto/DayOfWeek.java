package space.delusive.tversu.dto;

public enum DayOfWeek {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY;

    public DayOfWeek next() {
        DayOfWeek[] days = DayOfWeek.values();
        return days[(getIndexOf(this) + 1) % days.length];
    }

    public boolean isBeforeOf(DayOfWeek beforeWhat) {
        return getIndexOf(this) < getIndexOf(beforeWhat);
    }

    public boolean isAfterOf(DayOfWeek afterWhat) {
        return getIndexOf(this) > getIndexOf(afterWhat);
    }

    private int getIndexOf(DayOfWeek day) {
        DayOfWeek[] days = DayOfWeek.values();
        for (int i = 0; true; i++) {
            if (days[i] == day) {
                return i;
            }
        }
    }
}
