package co.za.entelect.Dtos.Whatsapp.Outgoing;

import co.za.entelect.Dtos.Whatsapp.IWhatsappDto;
import lombok.Builder;

@Builder
public record SendTextMessageDto(String messaging_product, String to, MessageText text) implements IWhatsappDto {

    @Builder
    public record MessageText(String body) {
    }
}
