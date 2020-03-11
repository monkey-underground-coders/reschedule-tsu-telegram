package space.delusive.tversu.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Group {
    private String level;
    private String name;
    private int subgroups;
    private int course;
}
