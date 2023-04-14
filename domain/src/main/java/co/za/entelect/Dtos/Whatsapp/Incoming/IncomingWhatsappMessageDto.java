package co.za.entelect.Dtos.Whatsapp.Incoming;

import java.util.List;

public record IncomingWhatsappMessageDto(String object, List<Entry> entry) {

    public record Entry(String id, List<Change> changes) {
    }

    public record Change(Value value, String field) {
    }

    public record Value(String messaging_product, Metadata metadata, List<Contact> contacts, List<Message> messages) {
    }

    public record Metadata(String display_phone_number, String phone_number_id) {
    }

    public record Contact(Profile profile, String wa_id) {
    }

    public record Profile(String name) {
    }

    public record Message(String from, String id, String timestamp, Text text, String type) {
    }

    public record Text(String body) {
    }
}
