package space.delusive.tversu.dto;

import lombok.Getter;
import lombok.Setter;
import space.delusive.tversu.util.BaseUtils;
import space.delusive.tversu.util.EmojiUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
public class Cell {
    private static final String NEW_LINE = "\n";
    private static final String EMPTY_STRING = "";

    private static final String SUBJECT_PATTERN = "\uD83D\uDCD6 Предмет: %subject%";
    private static final String SUBJECT_PLACEHOLDER = "%subject%";
    private static final String LESSON_TIME_PATTERN = "⏳ Время проведения: с %start% до %end%";
    private static final String LESSON_TIME_START_PLACEHOLDER = "%start%";
    private static final String LESSON_TIME_END_PLACEHOLDER = "%end%";
    private static final String TEACHER_PATTERN = "\uD83D\uDC68\u200D\uD83C\uDFEB Преподаватель: %teacher% %formattedTeacherTitle%";
    private static final String TEACHER_PLACEHOLDER = "%teacher%";
    private static final String FORMATTED_TEACHER_TITLE_PLACEHOLDER = "%formattedTeacherTitle%";
    private static final String TEACHER_TITLE_PATTERN = "(%teacherTitle%)";
    private static final String TEACHER_TITLE_PLACEHOLDER = "%teacherTitle%";
    private static final String TEACHER_IS_NOT_SPECIFIED = "не указан";
    private static final String LOCATION_PATTERN = "\uD83C\uDFEB Локация: %location%";
    private static final String LOCATION_PLACEHOLDER = "%location%";
    private static final String LOCATION_AUDIENCE = "аудитория";
    private static final String LOCATION_BUILDING = "корпус";
    private static final String CROSSPAIR_PATTERN = "\uD83D\uDCA0 Занятие проходит %crossPair%";
    private static final String CROSSPAIR_PLACEHOLDER = "%crossPair%";
    private static final String CROSSPAIR_ALONE = "*только у вашей группы*";
    private static final String CROSSPAIR_TOGETHER = "*совместно с другой группой*";

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
        StringBuilder response = new StringBuilder();
        String subject = SUBJECT_PATTERN.replace(SUBJECT_PLACEHOLDER, fullSubjectName);
        String lessonTime = LESSON_TIME_PATTERN
                .replace(LESSON_TIME_START_PLACEHOLDER, start)
                .replace(LESSON_TIME_END_PLACEHOLDER, end);
        String formattedTeacherTitle = TEACHER_TITLE_PATTERN
                .replace(TEACHER_TITLE_PLACEHOLDER, Objects.toString(this.teacherTitle, EMPTY_STRING));
        String teacher = TEACHER_PATTERN
                .replace(TEACHER_PLACEHOLDER, Objects.toString(teacherName, TEACHER_IS_NOT_SPECIFIED))
                .replace(FORMATTED_TEACHER_TITLE_PLACEHOLDER, formattedTeacherTitle);
        String location = LOCATION_PATTERN.replace(LOCATION_PLACEHOLDER,
                formatAuditoryInfo(LOCATION_AUDIENCE, LOCATION_BUILDING));
        String crossPairText = CROSSPAIR_PATTERN
                .replace(CROSSPAIR_PLACEHOLDER, crossPair ? CROSSPAIR_TOGETHER : CROSSPAIR_ALONE);

        response.append(subject).append(NEW_LINE)
                .append(lessonTime).append(NEW_LINE)
                .append(teacher).append(NEW_LINE)
                .append(location).append(NEW_LINE).append(NEW_LINE)
                .append(crossPairText);
        return response.toString();
    }

    // TODO: 10/31/2020 refactor this:
    public String toString() {
        StringBuilder response = new StringBuilder();
        response.append(EmojiUtils.getEmojiOfDigit(columnPosition + 1)).append(" *").append(fullSubjectName).append("*\n")
                .append("⏳ С ").append(start).append(" до ").append(end).append('\n')
                .append("\uD83D\uDC68\u200D\uD83C\uDFEB ").append(Objects.toString(teacherName, "не указан")).append('\n')
                .append("\uD83D\uDCCD ").append(formatAuditoryInfo("аудитория", "корпус")).append('\n')
                .append("\uD83E\uDD32 ").append(crossPair ? "С другой группой" : "Только у вашей группы");
        return response.toString();
    }

    public String toShortString() {
        StringBuilder response = new StringBuilder();
        response.append(EmojiUtils.getEmojiOfDigit(columnPosition + 1)).append(" *").append(shortSubjectName).append("* \n")
                .append("\uD83D\uDC68\u200D\uD83C\uDFEB ").append(shortifyTeacherName()).append("\n")
                .append("\uD83C\uDFEB ").append(formatAuditoryInfo("ауд.", "корп."));
        return response.toString();
    }

    private String formatAuditoryInfo(String audienceLabel, String buildingLabel) {
        if (auditoryAddress == null || auditoryAddress.isBlank()) {
            return "не указана";
        }
        List<String> messages = new ArrayList<>();
        String[] locations = auditoryAddress.split(", ");
        for (String location : locations) {
            String[] locationData = location.split("\\|");
			String buildingPart = "-".equals(locationData[0]) ? "" : ", " + buildingLabel + " " + locationData[0];
            messages.add(audienceLabel + " " + locationData[1] + buildingPart);
        }
        return BaseUtils.capitalizeString(String.join("; ", messages));
    }

    private String shortifyTeacherName() {
        if (teacherName == null || teacherName.isBlank()) {
            return "не указан";
        }
        String[] words = teacherName.split(" ");
		if (words.length % 3 != 0) {
			return teacherName;
		}
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; words.length / 3 > i; i++) {
            stringBuilder.append(words[i * 3])
                    .append(" ")
                    .append(words[i * 3 + 1].charAt(0))
                    .append(". ")
                    .append(words[i * 3 + 2].charAt(0))
                    .append(".")
                    .append(", ");
        }
        return stringBuilder.substring(0, stringBuilder.lastIndexOf(","));

    }
}
