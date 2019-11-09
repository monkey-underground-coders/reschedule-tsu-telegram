package space.delusive.tversu.rest.impl;

import kong.unirest.GenericType;
import kong.unirest.Unirest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import space.delusive.tversu.entity.Cell;
import space.delusive.tversu.manager.IDataManager;
import space.delusive.tversu.rest.CellRepository;

import java.util.List;

@Component
public class CellRepositoryImpl implements CellRepository {
    private final IDataManager config;

    @Autowired
    public CellRepositoryImpl(IDataManager config) {
        this.config = config;
    }

    @Override
    public List<Cell> getCells(String facultyId, String groupId) {
        return Unirest.get(config.getString("rest.get.cells.url"))
                .routeParam("faculty", facultyId)
                .routeParam("group", groupId)
                .asObject(new GenericType<List<Cell>>() {})
                .getBody();
    }
}
