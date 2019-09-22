package space.delusive.tversu.entity;

import java.sql.Date;

public class User extends Entity {
    private long id;
    private int state;
    private String faculty;
    private String group;
    private int subgroup;
    private Date registerDate;
    private Date lastMessageDate;

    public User(long id, int state, String faculty, String group, int subgroup, Date registerDate, Date lastMessageDate) {
        this.id = id;
        this.state = state;
        this.faculty = faculty;
        this.group = group;
        this.subgroup = subgroup;
        this.registerDate = registerDate;
        this.lastMessageDate = lastMessageDate;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFaculty() {
        return faculty;
    }

    public void setFaculty(String faculty) {
        this.faculty = faculty;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getSubgroup() {
        return subgroup;
    }

    public void setSubgroup(int subgroup) {
        this.subgroup = subgroup;
    }

    public Date getRegisterDate() {
        return registerDate;
    }

    public void setRegisterDate(Date registerDate) {
        this.registerDate = registerDate;
    }

    public Date getLastMessageDate() {
        return lastMessageDate;
    }

    public void setLastMessageDate(Date lastMessageDate) {
        this.lastMessageDate = lastMessageDate;
    }
}
