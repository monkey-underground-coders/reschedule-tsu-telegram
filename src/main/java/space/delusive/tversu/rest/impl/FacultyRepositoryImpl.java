package space.delusive.tversu.rest.impl;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import space.delusive.tversu.dto.WeekSign;
import space.delusive.tversu.exception.FailureRequestException;
import space.delusive.tversu.manager.DataManager;
import space.delusive.tversu.rest.FacultyRepository;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Получение инфы о факультетах с rest-сервера
 *
 * @author Delusive-
 */
@Component
@RequiredArgsConstructor
public class FacultyRepositoryImpl implements FacultyRepository {
    private final DataManager config;

    /**
     * Получение списка доступных факультетов
     *
     * @return Список факультетов
     * @throws FailureRequestException Если статус ответа не из "двухсотых"
     */
    @Override
    public List<String> getFaculties() throws FailureRequestException {
        HttpResponse<JsonNode> response = Unirest.get(config.getString("rest.get.faculties.url")).asJson();
        if (!response.isSuccess())
            throw new FailureRequestException(String.format("Status: %s; Text: %s", response.getStatus(), response.getStatusText()));
        var jsonFaculties = response.getBody().getObject().getJSONArray("faculties");
        var faculties = new ArrayList<String>();
        for (int i = 0; i < jsonFaculties.length(); i++) {
            faculties.add(jsonFaculties.getString(i));
        }
        return faculties;
    }


    @Override
    public Set<String> getPrograms(String faculty) throws FailureRequestException {
        HttpResponse<JsonNode> response = Unirest.get(config.getString("rest.get.groups.url")).routeParam("faculty", faculty).asJson();
        if (!response.isSuccess())
            throw new FailureRequestException(String.format("Status: %s; Text: %s", response.getStatus(), response.getStatusText()));
        var jsonGroups = response.getBody().getObject().getJSONArray("groups");
        var programs = new HashSet<String>();
        for (int i = 0; i < jsonGroups.length(); i++) {
            programs.add(jsonGroups.getJSONObject(i).getString("level"));
        }
        return programs;
    }

    @Override
    public Set<Integer> getCourses(String faculty, String program) throws FailureRequestException {
        HttpResponse<JsonNode> response = Unirest.get(config.getString("rest.get.groups.url")).routeParam("faculty", faculty).asJson();
        if (!response.isSuccess())
            throw new FailureRequestException(String.format("Status: %s; Text: %s", response.getStatus(), response.getStatusText()));
        var jsonGroups = response.getBody().getObject().getJSONArray("groups");
        var courses = new HashSet<Integer>();
        for (int i = 0; i < jsonGroups.length(); i++) {
            JSONObject jsonObject = jsonGroups.getJSONObject(i);
            if (program.equals(jsonObject.getString("level"))) {
                courses.add(jsonObject.getInt("course"));
            }
        }
        return courses;
    }

    @Override
    public Set<String> getGroups(String faculty, String program, int course) throws FailureRequestException {
        HttpResponse<JsonNode> response = Unirest.get(config.getString("rest.get.groups.url")).routeParam("faculty", faculty).asJson();
        if (!response.isSuccess())
            throw new FailureRequestException(String.format("Status: %s; Text: %s", response.getStatus(), response.getStatusText()));
        var jsonGroups = response.getBody().getObject().getJSONArray("groups");
        var groups = new HashSet<String>();
        for (int i = 0; i < jsonGroups.length(); i++) {
            JSONObject jsonObject = jsonGroups.getJSONObject(i);
            if (program.equals(jsonObject.getString("level")) && jsonObject.getInt("course") == course) {
                groups.add(jsonObject.getString("name"));
            }
        }
        return groups;
    }

    @Override
    public int getSubgroupsCount(String faculty, String program, int course, String group) throws FailureRequestException {
        HttpResponse<JsonNode> response = Unirest.get(config.getString("rest.get.groups.url")).routeParam("faculty", faculty).asJson();
        if (!response.isSuccess())
            throw new FailureRequestException(String.format("Status: %s; Text: %s", response.getStatus(), response.getStatusText()));
        var jsonGroups = response.getBody().getObject().getJSONArray("groups");
        int subgroups = 0;
        for (int i = 0; i < jsonGroups.length(); i++) {
            JSONObject jsonObject = jsonGroups.getJSONObject(i);
            if (program.equals(jsonObject.getString("level")) && jsonObject.getInt("course") == course && group.equals(jsonObject.getString("name")) && jsonObject.getInt("subgroups") > subgroups) {
                subgroups = jsonObject.getInt("subgroups");
            }
        }
        return subgroups;
    }

    @Override
    public WeekSign getCurrentWeekSign(String faculty) {
        LocalDate localDate = LocalDate.now(ZoneId.of("Europe/Moscow"));
        String weekSign = Unirest.get(config.getString("rest.get.week.sign.url"))
                .routeParam("faculty", faculty)
                .queryString("day", localDate.format(DateTimeFormatter.ISO_DATE))
                .asJson()
                .getBody()
                .getObject()
                .get("weekSign")
                .toString();
        return WeekSign.valueOf(weekSign);
    }
}
