import javax.persistence.Id;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "Persons")
public class Person {
    @Id
    private String email;
    private String name;

    public Person(String email, String name) {
        this.email = email;
        this.name = name;
    }

    public Person() {
    }
}
