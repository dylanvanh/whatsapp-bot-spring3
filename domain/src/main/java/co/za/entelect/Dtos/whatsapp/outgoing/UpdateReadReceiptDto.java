package co.za.entelect.Dtos.whatsapp.outgoing;

import co.za.entelect.Dtos.whatsapp.IWhatsappDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record UpdateReadReceiptDto (
        @JsonProperty("messaging_product") String messagingProduct,
        String status,
        @JsonProperty("message_id") String messageId) implements IWhatsappDto {
}
