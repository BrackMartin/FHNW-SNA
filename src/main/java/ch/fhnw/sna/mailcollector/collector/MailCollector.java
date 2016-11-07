package ch.fhnw.sna.mailcollector.collector;

import ch.fhnw.sna.mailcollector.models.Mail;
import ch.fhnw.sna.mailcollector.models.Person;
import ch.fhnw.sna.mailcollector.util.HibernateUtil;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.PropertySet;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.enumeration.property.BasePropertySet;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.enumeration.search.ComparisonMode;
import microsoft.exchange.webservices.data.core.enumeration.search.ContainmentMode;
import microsoft.exchange.webservices.data.core.enumeration.search.LogicalOperator;
import microsoft.exchange.webservices.data.core.enumeration.search.SortDirection;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.core.service.schema.EmailMessageSchema;
import microsoft.exchange.webservices.data.core.service.schema.ItemSchema;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.property.complex.EmailAddress;
import microsoft.exchange.webservices.data.search.FindItemsResults;
import microsoft.exchange.webservices.data.search.ItemView;
import microsoft.exchange.webservices.data.search.filter.SearchFilter;
import microsoft.exchange.webservices.data.search.filter.SearchFilter.SearchFilterCollection;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MailCollector {

    private String _username;
    private String _password;

    private Service<String> _downloadService;

    private List<String> emails = new ArrayList<>();

    /**
     * Default Constructor
     *
     * @param username Mail address of exchange server
     * @param password Password for exchange server
     */
    public MailCollector(String username, String password) {
        this._username = username;
        this._password = password;
    }

    /**
     * Starts downloading the mails from the exchange server
     */
    public void downloadMails() throws Exception {
        ExchangeService exchangeService = new ExchangeService(ExchangeVersion.Exchange2007_SP1);
        ExchangeCredentials credentials = new WebCredentials(this._username, this._password);
        exchangeService.setCredentials(credentials);
        exchangeService.autodiscoverUrl(this._username);

        findItems(exchangeService);
    }


    /**
     * Downloads all mails in a separate thread
     *
     * @return
     * @throws Exception
     */
    public Service<String> downloadMailsAsync() throws Exception {
        this._downloadService = new Service<String>() {
            @Override
            protected Task<String> createTask() {
                try {
                    return new Task<String>() {
                        @Override
                        protected String call() throws Exception {
                            downloadMails();
                            return "success";
                        }
                    };
                } catch (Exception exception) {
                    return new Task<String>() {
                        @Override
                        protected String call() throws Exception {
                            return exception.getMessage();
                        }
                    };
                }
            }
        };

        return this._downloadService;
    }

    /**
     * Aborts all running services
     */
    public void abortServices() {
        if (this._downloadService != null && this._downloadService.isRunning()) this._downloadService.cancel();
    }

    /**
     * Uses findItems with filters to download specific mails
     *
     * @param exchangeService
     */
    private void findItems(ExchangeService exchangeService) throws Exception {

        // item view
        ItemView itemView = new ItemView(9999);
        itemView.getOrderBy().add(ItemSchema.DateTimeReceived, SortDirection.Descending);
        itemView.setPropertySet(new PropertySet(BasePropertySet.FirstClassProperties));

        // filters
        SearchFilter searchFiltersInbox = getSearchFiltersFrom();
        SearchFilter searchFiltersTo = getSearchFiltersTo();

        FindItemsResults<Item> findResults = exchangeService.findItems(WellKnownFolderName.Inbox, searchFiltersInbox, itemView);
        FindItemsResults<Item> findResultsTo = exchangeService.findItems(WellKnownFolderName.SentItems, searchFiltersTo, itemView);
        processItems(findResults, exchangeService);
        processItems(findResultsTo, exchangeService);
    }

    /**
     * Saves result in database
     *
     * @param findResults
     * @param exchangeService
     */
    private void processItems(FindItemsResults<Item> findResults, ExchangeService exchangeService) throws Exception {

        //create session
        HibernateUtil.initializeSession();
        Session session = HibernateUtil.session;

        PropertySet propSet = new PropertySet(BasePropertySet.FirstClassProperties);
        propSet.add(ItemSchema.Subject);
        propSet.add(EmailMessageSchema.Body);
        propSet.add(ItemSchema.HasAttachments);
        propSet.add(EmailMessageSchema.From);
        propSet.add(EmailMessageSchema.ToRecipients);
        propSet.add(ItemSchema.DateTimeSent);

        exchangeService.loadPropertiesForItems(findResults.getItems(), propSet);
        System.out.println("Total number of items found: " + findResults.getTotalCount());
        HashMap<String, Person> people = new HashMap<>();
        ArrayList<Mail> mails = new ArrayList<>();

        for (Item item : findResults) {
            if (!(item instanceof EmailMessage)) continue;
            // Bugfix
            try {
                item.getBody();
            } catch (Exception exception) {
                item.load();
            }

            EmailMessage emailMessage = (EmailMessage) item;
            EmailAddress emailAddressInfo = emailMessage.getFrom();
            Person sender = new Person(emailAddressInfo.getAddress(), emailAddressInfo.getName());
            try {
                if (emailAddressInfo.getAddress() != null && !emails.contains(emailAddressInfo.getAddress())) {
                    emails.add(emailAddressInfo.getAddress());
                    //System.out.println("Persisting " + emailAddressInfo.getAddress() + " to db");
                    session.saveOrUpdate(sender);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }


            // Set receivers of Mail
            ArrayList<Person> receivers = new ArrayList<>();
            for (EmailAddress emailAddressReceiver : emailMessage.getToRecipients().getItems()) {
                Person person = new Person(emailAddressReceiver.getAddress(), emailAddressReceiver.getName());
                people.put(emailAddressReceiver.getAddress(), person);
                receivers.add(person);

                if (emailAddressReceiver.getAddress() != null && !emails.contains(emailAddressReceiver.getAddress())) {
                    emails.add(emailAddressReceiver.getAddress());
                    //System.out.println("Persisting " + emailAddressReceiver.getAddress() + " to db");
                    session.saveOrUpdate(person);
                }
            }

            people.put(emailAddressInfo.getAddress(), sender);
            mails.add(new Mail(emailMessage.getId().getUniqueId(), emailMessage.getSubject(), emailMessage.getBody().toString(), emailMessage.getDateTimeSent(), sender, receivers, emailMessage.getHasAttachments()));

            for (Mail mail : mails) {
                if (mail.getSender() != null && !mail.getReceivers().contains(null))
                    session.save(mail);
            }

        }

        System.out.println("finished collecting");
        HibernateUtil.endSession();
    }

    /**
     * Creates filters for inbox folder
     *
     * @return
     */
    private SearchFilter getSearchFiltersFrom() {

        return new SearchFilterCollection(
                LogicalOperator.Or,
                new SearchFilter.ContainsSubstring(EmailMessageSchema.From, "@fhnw.ch", ContainmentMode.Substring, ComparisonMode.IgnoreCase),
                new SearchFilter.ContainsSubstring(EmailMessageSchema.From, "@students.fhnw.ch", ContainmentMode.Substring, ComparisonMode.IgnoreCase)
        );
    }

    /**
     * Creates filters for inbox folder
     *
     * @return
     */
    private SearchFilter getSearchFiltersTo() {

        return new SearchFilterCollection(
                LogicalOperator.Or,
                new SearchFilter.ContainsSubstring(EmailMessageSchema.ToRecipients, "@fhnw.ch", ContainmentMode.Substring, ComparisonMode.IgnoreCase),
                new SearchFilter.ContainsSubstring(EmailMessageSchema.ToRecipients, "@students.fhnw.ch", ContainmentMode.Substring, ComparisonMode.IgnoreCase)
        );
    }

}
