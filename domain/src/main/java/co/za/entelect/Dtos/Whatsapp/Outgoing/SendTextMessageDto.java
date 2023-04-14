package co.za.entelect.Dtos.Whatsapp.Outgoing;

import lombok.Builder;

@Builder
public record SendTextMessageDto(String messaging_product, String to, MessageText text) {

    @Builder
    public record MessageText(String body) {
    }
}
