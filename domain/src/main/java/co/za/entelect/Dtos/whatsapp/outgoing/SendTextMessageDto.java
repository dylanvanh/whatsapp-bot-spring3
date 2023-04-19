package co.za.entelect.Dtos.whatsapp.outgoing;

import co.za.entelect.Dtos.whatsapp.IWhatsappDto;
import lombok.Builder;

@Builder
public record SendTextMessageDto(String messaging_product, String to, MessageText text) implements IWhatsappDto {

    @Builder
    public record MessageText(String body) {
    }
}
