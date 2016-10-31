package ch.fhnw.sna.mailcollector.models;

import java.util.ArrayList;
import java.util.Date;

public class Mail {
    private String _id;
    private String _subject;
    private String _body;
    private Date _sentTime;
    private boolean _hasAttachment;
    private Person _sender;
    private ArrayList<Person> _receivers;

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

    public ArrayList<Person> getReceivers() {
        return _receivers;
    }
}
