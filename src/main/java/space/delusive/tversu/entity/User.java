package space.delusive.tversu.entity;

public class User extends Entity {
    private long id;
    private String faculty;
    private String group;

    public User(long id, String faculty, String group) {
        this.id = id;
        this.faculty = faculty;
        this.group = group;
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
}
