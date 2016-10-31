package ch.fhnw.sna.mailcollector.models;

public class Person {
    private String _email;
    private String _name;

    public Person(String email, String name) {
        this._email = email;
        this._name = name;
    }

    public String getEmail() {
        return _email;
    }

    public String getName() {
        return _name;
    }
}