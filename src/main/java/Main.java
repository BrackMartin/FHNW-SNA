import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.PropertySet;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.enumeration.property.BasePropertySet;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.enumeration.search.LogicalOperator;
import microsoft.exchange.webservices.data.core.enumeration.search.SortDirection;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.core.service.schema.ItemSchema;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.search.FindItemsResults;
import microsoft.exchange.webservices.data.search.ItemView;
import microsoft.exchange.webservices.data.search.filter.SearchFilter;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class Main {

    private static Session session;

    public static void main(String[] args) {
        //Creating Session Factory / Session / Transaction
        initializeSession();
//        downloadMails();
        try {

            Person p1 = new Person("martin.luepold@students.fhnw.ch", "Martin Lüpold");
            Person p2 = new Person("andreas.gasser@students.fhnw.ch", "Andreas Gasser");
            Person p3 = new Person("luzian.seiler@students.fhnw.ch", "Luzian Seiler");
            Person p4 = new Person("matthias.langhard@students.fhnw.ch", "Matthias Langhard");
            Mail mail1 = new Mail("Hello Gasser", p1, p2);
            Mail mail2 = new Mail("Hello Matthias", p3, p4);
            Mail mail3 = new Mail("Hello Martin", p4, p1);
            Mail mail4 = new Mail("Hello Luzian", p3, p3);

            session.save(p1);
            session.save(p2);
            session.save(p3);
            session.save(p4);
            session.save(mail1);
            session.save(mail2);
            session.save(mail3);
            session.save(mail4);

            endSession();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            endSession();
        }


    }

    private static void downloadMails() {
        try {
            ExchangeService service = new ExchangeService(ExchangeVersion.Exchange2007_SP1);
            ExchangeCredentials credentials = new WebCredentials("martin.luepold@students.fhnw.ch", "password");
            service.setCredentials(credentials);
            service.autodiscoverUrl("martin.luepold@students.fhnw.ch");
            findItems(service);
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
        }
    }

    private static void findItems(ExchangeService service) {
        try {

            ItemView view = new ItemView(9999);
            view.getOrderBy().add(ItemSchema.DateTimeReceived, SortDirection.Ascending);
            view.setPropertySet(new PropertySet(BasePropertySet.IdOnly, ItemSchema.Subject, ItemSchema.DateTimeReceived));

            FindItemsResults<Item> findResults =
                    service.findItems(WellKnownFolderName.Inbox,
                            new SearchFilter.SearchFilterCollection(
                                    LogicalOperator.Or, new SearchFilter.ContainsSubstring(ItemSchema.Subject, "EWS"),
                                    new SearchFilter.ContainsSubstring(ItemSchema.Subject, "API")), view);

            //MOOOOOOST IMPORTANT: load items properties, before
            service.loadPropertiesForItems(findResults, PropertySet.FirstClassProperties);
            System.out.println("Total number of items found: " + findResults.getTotalCount());

            for (Item item : findResults) {
                System.out.println(item.getSubject());
            }
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
        }
    }


    //Other Methods
    private static void initializeSession() {
        //Creating Session Factory / Session / Transaction
        SessionFactory sessionFactory = HibernateUtil.createSessionFactory();
        session = sessionFactory.openSession();
        session.beginTransaction();
    }

    static void endSession() {
        session.flush();
        session.close();
        HibernateUtil.destroyServiceRegistry();
    }
}
