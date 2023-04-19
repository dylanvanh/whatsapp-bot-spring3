package co.za.entelect.dtos.whatsapp.outgoing;

import co.za.entelect.dtos.whatsapp.IWhatsappDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record UpdateReadReceiptDto (
        @JsonProperty("messaging_product") String messagingProduct,
        String status,
        @JsonProperty("message_id") String messageId) implements IWhatsappDto {
}
