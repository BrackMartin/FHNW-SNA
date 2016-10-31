import javax.persistence.*;

@Entity
@Table(name = "mails")
public class Mail {
    @Id
    @GeneratedValue
    private Integer id;

    private String subject;

    @ManyToOne
    private Person recepient;

    @ManyToOne
    private Person sender;

    public Mail(String subject, Person sender, Person recepient) {
        this.subject = subject;
        this.sender = sender;
        this.recepient = recepient;
    }

    public Mail() {
    }
}
