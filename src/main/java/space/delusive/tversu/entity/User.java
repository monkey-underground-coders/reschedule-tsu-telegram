package space.delusive.tversu.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import space.delusive.tversu.BotState;

import javax.persistence.*;
import java.sql.Date;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    private long id;

    @Enumerated(EnumType.ORDINAL)
    private BotState state;

    private String faculty;

    private String program;

    private int course;

    private String group;

    private int subgroup;

    @Column(name = "register_date")
    private Date registerDate;

    @Column(name = "last_message_date")
    private Date lastMessageDate;
}
