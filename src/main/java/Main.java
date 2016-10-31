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

/**
 * Created by MartinBrack on 31.10.16.
 */
public class Main {
    public static void main(String[] args) {
        downloadMails();
    }

    private static void downloadMails() {
        try {
            ExchangeService service = new ExchangeService(ExchangeVersion.Exchange2007_SP1);
            ExchangeCredentials credentials = new WebCredentials("martin.luepold@students.fhnw.ch", "password");
            service.setCredentials(credentials);
            service.autodiscoverUrl("martin.luepold@students.fhnw.ch");
            findItems(service);
        }catch (Exception exception) {
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
        }catch(Exception exception) {
            System.out.println(exception.getMessage());
        }
    }
}
