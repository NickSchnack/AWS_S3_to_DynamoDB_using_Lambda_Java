package example;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.event.S3EventNotification.S3EventNotificationRecord;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.util.*;

public class LambdaFunction {

    private String databaseTable = "thinkcodedev-demo-tickets_tickets";
    private boolean print_verbose = true;

    public void handleRequest(S3Event event) {
        try {
            print_to_log("INFO", "RECEIVED EVENT", event.toJson(), print_verbose);
            String bucketname = get_bucketname_from_event(event);
            String filename = get_filename_from_event(event);
            Ticket ticket = get_ticket_from_filename(bucketname, filename);
            write_ticket_to_database_table(ticket, databaseTable);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void print_to_log(String type, String subject, String event, boolean print_verbose) {
        List<String> verbose_types = new ArrayList<String>(List.of(
                "error",
                "err",
                "fail"
        ));

        for (int i=0; i<verbose_types.size(); i++) {
            verbose_types.set(i, verbose_types.get(i).toLowerCase());
        }

        if (!print_verbose && !verbose_types.contains(type.toLowerCase())) {
            return;
        }

        System.out.println("[" + type.toUpperCase() + "] " + subject);
        System.out.println(event);
    }

    private String get_bucketname_from_event(S3Event event) {
        S3EventNotificationRecord record = event.getRecords().get(0);
        String bucketname = record.getS3().getBucket().getName();
        print_to_log("INFO", "RETRIEVED BUCKET NAME FROM EVENT", bucketname, print_verbose);
        return bucketname;
    }

    private String get_filename_from_event(S3Event event) {
        S3EventNotificationRecord record = event.getRecords().get(0);
        String filename = record.getS3().getObject().getKey();
        print_to_log("INFO", "RETRIEVED FILENAME NAME FROM EVENT", filename, print_verbose);
        return filename;
    }

    private Ticket get_ticket_from_filename(String bucketname, String filename) {
        AmazonS3 s3_client = AmazonS3ClientBuilder.standard().build();
        S3ObjectInputStream inputStream = null;
        FileOutputStream fos = null;
        String json_contents = "{ unknown }";
        Ticket newTicket = new Ticket();

        try {
            S3Object object = s3_client.getObject(bucketname, filename);
            inputStream = object.getObjectContent();

            Scanner s = new Scanner(inputStream).useDelimiter("\\A");
            json_contents = s.hasNext() ? s.next() : "";

            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(json_contents);

            newTicket.set_customer_id(json.get("customer_id").toString());
            newTicket.set_ticket_subject(json.get("ticket_subject").toString());
            newTicket.set_ticket_description(json.get("ticket_description").toString());
            newTicket.set_ticket_dest_email(json.get("ticket_dest_email").toString());
        }
        catch (Exception e) {
            System.out.println(e.getMessage());

        }
        finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (fos != null) {
                    fos.close();
                }
            }
            catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }

        print_to_log("INFO", "RETRIEVED DATA FROM FILE", json_contents, print_verbose);
        return newTicket;
    }

    private void write_ticket_to_database_table(Ticket ticket, String table_name) {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
        DynamoDBMapper mapper = new DynamoDBMapper(client);
        ticket.set_ticket_id(get_random_uuid());
        mapper.save(ticket);
        print_to_log("INFO", "DATA WRITTEN TO DATABASE USING RANDOM UUID", ticket.get_ticket_id(), print_verbose);
    }

    private String get_random_uuid() {
        return UUID.randomUUID().toString();
    }
}