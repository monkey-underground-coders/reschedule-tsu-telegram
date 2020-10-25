package space.delusive.tversu.rest;

import space.delusive.tversu.dto.Cell;
import space.delusive.tversu.exception.SoldisWhatTheFuckException;

import java.util.List;

public interface CellRepository {
    List<Cell> getCells(String facultyId, String groupId) throws SoldisWhatTheFuckException;
}
