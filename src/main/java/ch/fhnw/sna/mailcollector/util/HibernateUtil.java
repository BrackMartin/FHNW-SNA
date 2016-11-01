package ch.fhnw.sna.mailcollector.util;

import ch.fhnw.sna.mailcollector.models.Mail;
import ch.fhnw.sna.mailcollector.models.Person;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;


public class HibernateUtil {

    private static SessionFactory sessionFactory;
    private static ServiceRegistry serviceRegistry;
    public static Session session;

    public static SessionFactory createSessionFactory() {
        Configuration configuration = new Configuration();
        configuration.addAnnotatedClass(Mail.class);
        configuration.addAnnotatedClass(Person.class);
        configuration.configure();
        serviceRegistry = new StandardServiceRegistryBuilder().applySettings(
                configuration.getProperties()).build();
        sessionFactory = configuration.buildSessionFactory(serviceRegistry);
        return sessionFactory;
    }

    public static void destroyServiceRegistry() {
        StandardServiceRegistryBuilder.destroy(serviceRegistry);
    }

    //Other Methods
    public static void initializeSession() {
        //Creating Session Factory / Session / Transaction
        SessionFactory sessionFactory = HibernateUtil.createSessionFactory();

        session = sessionFactory.openSession();
        session.beginTransaction();
    }

    public static void endSession() {
        session.flush();
        session.close();
        HibernateUtil.destroyServiceRegistry();
    }

}