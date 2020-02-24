package space.delusive.tversu.entity;

import lombok.Getter;
import lombok.Setter;
import space.delusive.tversu.util.EmojiUtils;

@Getter
@Setter
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

    public String toLongString() {
        String[] auditoryData = splitAuditoryInfo();
        StringBuilder response = new StringBuilder();
        response.append("\uD83D\uDCD6 Предмет: ").append(fullSubjectName).append('\n')
                .append("⏳ Время проведения: с ").append(start).append(" до ").append(end).append('\n')
                .append("\uD83D\uDC68\u200D\uD83C\uDFEB Преподаватель: ").append(teacherName).append(" (").append(teacherTitle).append(")\n")
                .append("\uD83C\uDFEB Локация: ").append(auditoryData[0]).append(" корпус, ").append(auditoryData[1]).append(" аудитория").append("\n\n")
                .append("\uD83D\uDCA0 Занятие проходит ").append(crossPair ? "*совместно с другой группой*" : "*только у вашей группы*");
        return response.toString();
    }

    public String toString() {
        String[] auditoryData = splitAuditoryInfo();
        StringBuilder response = new StringBuilder();
        response.append(EmojiUtils.getEmojiOfDigit(columnPosition + 1)).append(" *").append(fullSubjectName).append("*\n")
                .append("⏳ С ").append(start).append(" до ").append(end).append('\n')
                .append("\uD83D\uDC68\u200D\uD83C\uDFEB ").append(teacherName).append('\n')
                .append("\uD83D\uDCCD Аудитория ").append(auditoryData[1]).append(", корпус ").append(auditoryData[0]).append('\n')
                .append("\uD83E\uDD32 ").append(crossPair ? "С другой группой" : "Только у вашей группы");
        return response.toString();
    }

    public String toShortString() {
        String[] auditoryData = splitAuditoryInfo();
        StringBuilder response = new StringBuilder();
        response.append(EmojiUtils.getEmojiOfDigit(columnPosition + 1)).append(" *").append(shortSubjectName).append("* \n")
                .append("\uD83D\uDC68\u200D\uD83C\uDFEB ").append(shortifyTeacherName()).append("\n")
                .append("\uD83C\uDFEB ").append("Ауд. ").append(auditoryData[1]).append(", корп. ").append(auditoryData[0]);
        return response.toString();
    }

    private String[] splitAuditoryInfo() {
        return auditoryAddress.split("\\|");
    }

    private String shortifyTeacherName() {
        String[] words = teacherName.split(" ");
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; words.length / 3 > i; i++) {
            stringBuilder.append(words[i*3])
                    .append(" ")
                    .append(words[i*3+1].charAt(0))
                    .append(". ")
                    .append(words[i*3+2].charAt(0))
                    .append(".")
                    .append(", ");
        }
        return stringBuilder.toString().substring(0, stringBuilder.lastIndexOf(","));
    }
}
