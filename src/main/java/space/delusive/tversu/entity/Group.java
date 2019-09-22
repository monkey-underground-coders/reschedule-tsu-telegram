package space.delusive.tversu.entity;

public class Group extends Entity {
    private String level;
    private String name;
    private int subgroups;
    private int course;

    public Group(String level, String name, int subgroups, int course) {
        this.level = level;
        this.name = name;
        this.subgroups = subgroups;
        this.course = course;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSubgroups() {
        return subgroups;
    }

    public void setSubgroups(int subgroups) {
        this.subgroups = subgroups;
    }

    public int getCourse() {
        return course;
    }

    public void setCourse(int course) {
        this.course = course;
    }
}
