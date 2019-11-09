package space.delusive.tversu.entity;

public class Cell extends Entity {
    private WeekSign weekSign;
    private String fullSubjectName;
    private String shortSubjectName;
    private String teacherName;
    private String teacherTitle;
    private DayOfWeek dayOfWeek;
    private String start;
    private String end;
    private String auditoryAddress;
    private int course;
    private String group;
    private int subgroup;
    private boolean crossPair;
    private String faculty;

    public WeekSign getWeekSign() {
        return weekSign;
    }

    public void setWeekSign(WeekSign weekSign) {
        this.weekSign = weekSign;
    }

    public String getFullSubjectName() {
        return fullSubjectName;
    }

    public void setFullSubjectName(String fullSubjectName) {
        this.fullSubjectName = fullSubjectName;
    }

    public String getShortSubjectName() {
        return shortSubjectName;
    }

    public void setShortSubjectName(String shortSubjectName) {
        this.shortSubjectName = shortSubjectName;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getTeacherTitle() {
        return teacherTitle;
    }

    public void setTeacherTitle(String teacherTitle) {
        this.teacherTitle = teacherTitle;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getAuditoryAddress() {
        return auditoryAddress;
    }

    public void setAuditoryAddress(String auditoryAddress) {
        this.auditoryAddress = auditoryAddress;
    }

    public int getCourse() {
        return course;
    }

    public void setCourse(int course) {
        this.course = course;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public int getSubgroup() {
        return subgroup;
    }

    public void setSubgroup(int subgroup) {
        this.subgroup = subgroup;
    }

    public boolean isCrossPair() {
        return crossPair;
    }

    public void setCrossPair(boolean crossPair) {
        this.crossPair = crossPair;
    }

    public String getFaculty() {
        return faculty;
    }

    public void setFaculty(String faculty) {
        this.faculty = faculty;
    }

    public String toLongString() {
        String[] auditoryData = auditoryAddress.split("\\|");
        StringBuilder response = new StringBuilder();
        response.append("\uD83D\uDCD6 Предмет: ").append(fullSubjectName).append('\n')
                .append("⏳ Время проведения: с ").append(start).append(" до ").append(end).append('\n')
                .append("\uD83D\uDC68\u200D\uD83C\uDFEB Преподаватель: ").append(teacherName).append(" (").append(teacherTitle).append(")\n")
                .append("\uD83C\uDFEB Локация: ").append(auditoryData[0]).append(" корпус, ").append(auditoryData[1]).append(" аудитория").append("\n\n")
                .append("\uD83D\uDCA0 Занятие проходит ").append(crossPair ? "*совместно с другой группой*" : "*только у вашей группы*");
        return response.toString();
    }
}
