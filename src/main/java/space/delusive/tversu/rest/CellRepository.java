package space.delusive.tversu.rest;

import space.delusive.tversu.entity.Cell;

import java.util.List;

public interface CellRepository {
    List<Cell> getCells(String facultyId, String groupId);
}
