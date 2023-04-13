package co.za.entelect;

import co.za.entelect.Dtos.Whatsapp.IncomingWhatsappMessageDto;
import co.za.entelect.services.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/whatsapp")
public class WhatsappController {

    private final MessageService messageService;

    @Autowired
    public WhatsappController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    public ResponseEntity<Void> handleWebhook(@RequestBody IncomingWhatsappMessageDto incomingMessageDto) {
        System.out.println("POST REQUEST HIT");

        // Check the Incoming webhook message
        if (incomingMessageDto != null) {
            System.out.println("Incoming message: " + incomingMessageDto.toString());
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<String> verifyWebhook(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String token,
            @RequestParam("hub.challenge") String challenge) {

        System.out.println("HIT");

        String verifyToken = System.getenv("VERIFY_TOKEN");

        if (mode != null && token != null) {
            if ("subscribe".equals(mode) && verifyToken.equals(token)) {
                System.out.println("WEBHOOK_VERIFIED");
                return new ResponseEntity<>(challenge, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        }

        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
}
