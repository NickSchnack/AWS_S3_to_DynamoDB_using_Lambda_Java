package example;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "thinkcodedev-demo-tickets_tickets")
public class Ticket {

    private String ticket_id;
    private String customer_id;
    private String ticket_subject;
    private String ticket_description;
    private String ticket_dest_email;

    @DynamoDBHashKey(attributeName = "ticket_id")
    @DynamoDBAttribute(attributeName = "ticket_id")
    public String get_ticket_id() { return ticket_id; }
    public void set_ticket_id(String ticket_id) { this.ticket_id = ticket_id; }

    @DynamoDBAttribute(attributeName = "customer_id")
    public String get_customer_id() { return customer_id; }
    public void set_customer_id(String customer_id) { this.customer_id = customer_id; }

    @DynamoDBAttribute(attributeName = "ticket_subject")
    public String get_ticket_subject() { return ticket_subject; }
    public void set_ticket_subject(String ticket_subject) { this.ticket_subject = ticket_subject; }

    @DynamoDBAttribute(attributeName = "ticket_description")
    public String get_ticket_description() { return ticket_description; }
    public void set_ticket_description(String ticket_description) { this.ticket_description = ticket_description; }

    @DynamoDBAttribute(attributeName = "ticket_dest_email")
    public String get_ticket_dest_email() { return ticket_dest_email; }
    public void set_ticket_dest_email(String ticket_dest_email) { this.ticket_dest_email = ticket_dest_email; }

}
