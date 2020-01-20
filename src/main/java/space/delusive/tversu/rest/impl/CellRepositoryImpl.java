package space.delusive.tversu.rest.impl;

import kong.unirest.GenericType;
import kong.unirest.Unirest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import space.delusive.tversu.entity.Cell;
import space.delusive.tversu.manager.DataManager;
import space.delusive.tversu.rest.CellRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CellRepositoryImpl implements CellRepository {
    private final DataManager config;

    @Override
    public List<Cell> getCells(String facultyId, String groupId) {
        return Unirest.get(config.getString("rest.get.cells.url"))
                .routeParam("faculty", facultyId)
                .routeParam("group", groupId)
                .asObject(new GenericType<List<Cell>>() {})
                .getBody();
    }
}
