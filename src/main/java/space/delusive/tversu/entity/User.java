package space.delusive.tversu.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;

@Getter
@Setter
@AllArgsConstructor
public class User extends Entity {
    private long id;
    private int state;
    private String faculty;
    private String program;
    private int course;
    private String group;
    private int subgroup;
    private Date registerDate;
    private Date lastMessageDate;
}
