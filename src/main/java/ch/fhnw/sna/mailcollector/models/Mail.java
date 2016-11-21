package ch.fhnw.sna.mailcollector.models;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "mails")
public class Mail {
    @Id
    @GeneratedValue
    private Integer id;

    private String _id;
    private String _subject;
    @Lob
    @Column(length = 100000)
    private String _body;
    private Date _sentTime;
    private boolean _hasAttachment;
    @ManyToOne
    private Person _sender;
    @ManyToMany
    private List<Person> _receivers;

    public Mail(String id, String subject, String body, Date sentTime, Person sender, ArrayList<Person> receivers, boolean hasAttachment) {
        this._id = id;
        this._subject = subject;
        this._body = body;
        this._sentTime = sentTime;
        this._hasAttachment = hasAttachment;
        this._sender = sender;
        this._receivers = receivers;
    }

    public String getId() {
        return _id;
    }

    public String getSubject() {
        return _subject;
    }

    public String getBody() {
        return _body;
    }

    public Date getSentTime() {
        return _sentTime;
    }

    public boolean hasAttachment() {
        return _hasAttachment;
    }

    public Person getSender() {
        return _sender;
    }

    public void addReceiver(Person person) {
        _receivers.add(person);
    }

    public List<Person> getReceivers() {
        return _receivers;
    }
}
