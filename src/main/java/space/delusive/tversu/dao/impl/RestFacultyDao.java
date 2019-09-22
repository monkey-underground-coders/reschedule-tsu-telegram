package space.delusive.tversu.dao.impl;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import space.delusive.tversu.dao.IFacultyDao;
import space.delusive.tversu.exception.NotSuccessRequestException;
import space.delusive.tversu.manager.IDataManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Получение инфы о факультетах с rest-сервера
 *
 * @author Delusive-
 * @version 1.2
 */
public class RestFacultyDao implements IFacultyDao {
    private final IDataManager config;

    public RestFacultyDao(IDataManager config) {
        this.config = config;
    }

    /**
     * Получение списка доступных факультетов
     *
     * @return Список факультетов
     * @throws NotSuccessRequestException Если статус ответа не из "двухсотых"
     */
    @Override
    public List<String> getFaculties() throws NotSuccessRequestException {
        HttpResponse<JsonNode> response = Unirest.get(config.getString("rest.get.faculties.url")).asJson();
        if (!response.isSuccess())
            throw new NotSuccessRequestException(String.format("Status: %s; Text: %s", response.getStatus(), response.getStatusText()));
        var jsonFaculties = response.getBody().getObject().getJSONArray("faculties");
        var faculties = new ArrayList<String>();
        for (int i = 0; i < jsonFaculties.length(); i++) {
            faculties.add(jsonFaculties.getString(i));
        }
        return faculties;
    }


    @Override
    public Set<String> getPrograms(String faculty) {
        HttpResponse<JsonNode> response = Unirest.get(config.getString("rest.get.groups.url")).routeParam("faculty", faculty).asJson();
        if (!response.isSuccess())
            throw new NotSuccessRequestException(String.format("Status: %s; Text: %s", response.getStatus(), response.getStatusText()));
        var jsonGroups = response.getBody().getObject().getJSONArray("groups");
        var programs = new HashSet<String>();
        for (int i = 0; i < jsonGroups.length(); i++) {
            programs.add(jsonGroups.getJSONObject(i).getString("level"));
        }
        return programs;
    }
}
