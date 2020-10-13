package shamu.company.helpers;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.AttributeUpdate;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile({"dev", "default"})
@Component
public class DynamoDBHelper {

  private final Table table;

  private static final String PRIMARY_KEY = "id";

  private static final String STATUS = "status";

  public DynamoDBHelper(@Value("${aws.dynamodb.table}") final String dynamoDBTableName) {
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
    attributeUpdate.put("DONE");

    final UpdateItemSpec updateItemSpec = new UpdateItemSpec();
    updateItemSpec.withPrimaryKey(PRIMARY_KEY, id).withAttributeUpdate(attributeUpdate);
    table.updateItem(updateItemSpec);
  }
}
