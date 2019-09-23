package space.delusive.tversu.entity;

import java.sql.Date;

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

    public User(long id, int state, String faculty, String program, int course, String group, int subgroup, Date registerDate, Date lastMessageDate) {
        this.id = id;
        this.state = state;
        this.faculty = faculty;
        this.program = program;
        this.course = course;
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

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
    }

    public int getCourse() {
        return course;
    }

    public void setCourse(int course) {
        this.course = course;
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
