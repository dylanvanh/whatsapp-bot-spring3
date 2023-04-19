package co.za.entelect.dtos.whatsapp.outgoing;

import co.za.entelect.dtos.whatsapp.IWhatsappDto;
import lombok.Builder;

@Builder
public record SendTextMessageDto(String messaging_product, String to, MessageText text) implements IWhatsappDto {

    @Builder
    public record MessageText(String body) {
    }
}
