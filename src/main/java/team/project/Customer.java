
package team.project;

public class Customer extends User {
    private String address; 

    public Customer() {
        super();
        this.role = "CUSTOMER";
    }
    public Customer(String name, String email, String password, String phone, String address) {
        super(name, email, password, phone,"CUSTOMER");
        this.address = address;
    }

    public Customer(int id, String name, String email, String password, String phone, String address) {
        super(id, name, email, password, phone, "CUSTOMER");
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", role=" + role +
                ", address='" + address + '\'' +
                '}';
    }
}