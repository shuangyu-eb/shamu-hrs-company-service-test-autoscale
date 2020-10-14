package shamu.company.helpers.dynamodb;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.AttributeUpdate;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.ScanFilter;
import com.amazonaws.services.dynamodbv2.document.ScanOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.internal.IteratorSupport;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import shamu.company.utils.UuidUtil;

@Component
public class DynamoDBManager {

  private final Table table;

  private static final String PRIMARY_KEY = "id";

  private static final String STATUS = "status";

  private static final String STATUS_DONE = "DONE";

  public DynamoDBManager(@Value("${aws.dynamodb.table}") final String dynamoDBTableName) {
    final AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
    final DynamoDB dynamoDB = new DynamoDB(client);
    table = dynamoDB.getTable(dynamoDBTableName);
  }

  public void deleteDynamoRecord(final String id) {
    final DeleteItemSpec deleteItemSpec = new DeleteItemSpec();
    deleteItemSpec.withPrimaryKey(PRIMARY_KEY, id);
    table.deleteItem(deleteItemSpec);
  }

  public void updateDynamoRecord(final String id) {
    final AttributeUpdate attributeUpdate = new AttributeUpdate(STATUS);
    attributeUpdate.put(STATUS_DONE);

    final UpdateItemSpec updateItemSpec = new UpdateItemSpec();
    updateItemSpec.withPrimaryKey(PRIMARY_KEY, id).withAttributeUpdate(attributeUpdate);
    table.updateItem(updateItemSpec);
  }

  public String getExistsCompanyId() {
    synchronized (this) {
      final ScanFilter scanFilter = new ScanFilter(STATUS).eq(STATUS_DONE);
      final ItemCollection<ScanOutcome> items = table.scan(scanFilter);
      final IteratorSupport iterator = items.iterator();
      if (iterator.hasNext()) {
        final Item item = (Item) iterator.next();
        final String id = item.getString(PRIMARY_KEY);
        deleteDynamoRecord(id);
        return id;
      }
      // if the db doesn't hold any record here, just return a new uuid
      return UuidUtil.getUuidString().toUpperCase();
    }
  }
}
