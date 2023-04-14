package co.za.entelect.Dtos.Whatsapp.Outgoing;

import co.za.entelect.Dtos.Whatsapp.IWhatsappDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record UpdateReadReceiptDto (
        @JsonProperty("messaging_product") String messagingProduct,
        String status,
        @JsonProperty("message_id") String messageId) implements IWhatsappDto {
}
