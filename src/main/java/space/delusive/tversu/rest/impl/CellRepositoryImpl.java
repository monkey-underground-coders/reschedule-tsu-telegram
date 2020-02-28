package space.delusive.tversu.rest.impl;

import kong.unirest.GenericType;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import space.delusive.tversu.entity.Cell;
import space.delusive.tversu.exception.SoldisWhatTheFuckException;
import space.delusive.tversu.manager.DataManager;
import space.delusive.tversu.rest.CellRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CellRepositoryImpl implements CellRepository {
    private static final int NOT_FOUND_STATUS_CODE = 404;
    private final DataManager config;

    @Override
    public List<Cell> getCells(String facultyId, String groupId) throws SoldisWhatTheFuckException {
        HttpResponse<List<Cell>> body = Unirest.get(config.getString("rest.get.cells.url"))
                .routeParam("faculty", facultyId)
                .routeParam("group", groupId)
                .asObject(new GenericType<>() {});
        if (body.getStatus() == NOT_FOUND_STATUS_CODE) {
            throw new SoldisWhatTheFuckException("Seems like groups were renamed. Thanks Soldis...");
        }
        return body.getBody();
    }
}
