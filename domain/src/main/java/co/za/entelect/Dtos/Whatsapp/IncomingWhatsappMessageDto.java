package co.za.entelect.Dtos.Whatsapp;

public class IncomingWhatsappMessageDto {
    public String object;
    public WhatsAppBusinessAccountEntry[] entry;
}

class WhatsAppBusinessAccountEntry {
    public String id;
    public WhatsAppBusinessAccountChange[] changes;
}

class WhatsAppBusinessAccountChange {
    public WhatsAppBusinessAccountChangeValue value;
    public String field;
}

class WhatsAppBusinessAccountChangeValue {
    public String messaging_product;
    public WhatsAppBusinessAccountChangeValueMetadata metadata;
    public WhatsAppBusinessAccountContact[] contacts;
    public WhatsAppBusinessAccountMessage[] messages;
}

class WhatsAppBusinessAccountChangeValueMetadata {
    public String display_phone_number;
    public String phone_number_id;
}

class WhatsAppBusinessAccountContact {
    public WhatsAppBusinessAccountContactProfile profile;
    public String wa_id;
}

class WhatsAppBusinessAccountContactProfile {
    public String name;
}

class WhatsAppBusinessAccountMessage {
    public String from;
    public String id;
    public String timestamp;
    public WhatsAppBusinessAccountMessageText text;
    public String type;
}

class WhatsAppBusinessAccountMessageText {
    public String body;
}
