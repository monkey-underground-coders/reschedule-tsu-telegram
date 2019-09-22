package space.delusive.tversu.dao.impl;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import space.delusive.tversu.dao.IFacultyDao;
import space.delusive.tversu.exception.NotSuccessRequestException;
import space.delusive.tversu.manager.IDataManager;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Получение инфы о факультетах с rest-сервера
 *
 * @author Delusive-
 * @version 1.0
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
    public Collection<String> getFaculties() throws NotSuccessRequestException {
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
}
