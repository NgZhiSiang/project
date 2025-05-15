
package team.project;

public class Admin extends User {
    public Admin() {
        super();
        this.role = "Admin";
    }

    public Admin(String name, String email, String password, String phone) {
        super(name, email, password, phone, "Admin");
    }

    public Admin(int id, String name, String email, String password, String phone) {
        super(id, name, email, password, phone, "Admin");
    }

    @Override
    public String toString() {
        return "Admin{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", role=" + role +
                '}';
    }

}
