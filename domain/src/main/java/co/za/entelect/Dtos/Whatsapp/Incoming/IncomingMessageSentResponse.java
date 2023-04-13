package co.za.entelect.Dtos.Whatsapp.Incoming;

import java.util.List;

public record IncomingMessageSentResponse(String messaging_product, List<Contact> contacts, List<Message> messages) {
    public record Contact(String input, String wa_id) {
    }

    public record Message(String id) {
    }
}
