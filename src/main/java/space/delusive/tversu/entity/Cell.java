package space.delusive.tversu.entity;

import space.delusive.tversu.util.EmojiUtils;

public class Cell extends Entity {
    private WeekSign weekSign;
    private String fullSubjectName;
    private String shortSubjectName;
    private String teacherName;
    private String teacherTitle;
    private DayOfWeek dayOfWeek;
    private byte columnPosition;
    private String start;
    private String end;
    private String auditoryAddress;
    private byte course;
    private String group;
    private byte subgroup;
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

    public void setCourse(byte course) {
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

    public void setSubgroup(byte subgroup) {
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

    public String toString() {
        String[] auditoryData = auditoryAddress.split("\\|");
        StringBuilder response = new StringBuilder();
        response.append(EmojiUtils.getEmojiOfDigit(columnPosition + 1)).append(" *").append(fullSubjectName).append("*\n")
                .append("⏳ С ").append(start).append(" до ").append(end).append('\n')
                .append("\uD83D\uDC68\u200D\uD83C\uDFEB ").append(teacherName).append('\n')
                .append("\uD83D\uDCCD Аудитория ").append(auditoryData[1]).append(", корпус ").append(auditoryData[0]).append('\n')
                .append("\uD83E\uDD32 ").append(crossPair ? "С другой группой" : "Только у вашей группы");
        return response.toString();
    }
}
