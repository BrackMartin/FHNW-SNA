package ch.fhnw.sna.mailcollector.collector;

import ch.fhnw.sna.mailcollector.models.Mail;
import ch.fhnw.sna.mailcollector.models.Person;
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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class MailCollector {

    private String _username;
    private String _password;

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
     * Uses findItems with filters to download specific mails
     *
     * @param exchangeService
     */
    private void findItems(ExchangeService exchangeService) throws Exception {

        // item view
        ItemView itemView = new ItemView(9999);
        itemView.getOrderBy().add(ItemSchema.DateTimeReceived, SortDirection.Descending);
        itemView.setPropertySet(new PropertySet(BasePropertySet.FirstClassProperties));

        PropertySet propSet = new PropertySet(BasePropertySet.FirstClassProperties);
        propSet.add(ItemSchema.Subject);
        propSet.add(EmailMessageSchema.Body);
        propSet.add(ItemSchema.HasAttachments);
        propSet.add(EmailMessageSchema.From);
        propSet.add(EmailMessageSchema.ToRecipients);
        propSet.add(ItemSchema.DateTimeSent);

        // filters
        SearchFilter searchFiltersInbox = getSearchFiltersFrom();

        FindItemsResults<Item> findResults = exchangeService.findItems(WellKnownFolderName.Inbox, searchFiltersInbox, itemView);
        exchangeService.loadPropertiesForItems(findResults.getItems(), propSet);
        System.out.println("Total number of items found: " + findResults.getTotalCount());
        HashMap<String, Person> people = new HashMap<>();
        ArrayList<Mail> mails = new ArrayList<>();

        for (Item item : findResults) {
            if (!(item instanceof EmailMessage)) continue;

            // Bugfix
            try {
                item.getBody();
            }catch (Exception exception) {
                item.load();
            }

            EmailMessage emailMessage = (EmailMessage) item;
            EmailAddress emailAddressInfo = emailMessage.getFrom();
            Person sender = new Person(emailAddressInfo.getAddress(), emailAddressInfo.getName());

            // Set receivers of Mail
            ArrayList<Person> receivers = new ArrayList<>();
            for (EmailAddress emailAddressReceiver: emailMessage.getToRecipients().getItems()) {
                Person person = new Person(emailAddressReceiver.getAddress(), emailAddressReceiver.getName());
                people.put(emailAddressReceiver.getAddress(), person);
                receivers.add(person);
            }

            people.put(emailAddressInfo.getAddress(), sender);
            mails.add(new Mail(emailMessage.getId().getUniqueId(), emailMessage.getSubject(), emailMessage.getBody().toString(), emailMessage.getDateTimeSent(), sender, receivers, emailMessage.getHasAttachments()));
        }

        System.out.println("finished collecting");
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
}
