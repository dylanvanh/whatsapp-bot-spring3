package co.za.entelect;

import co.za.entelect.Dtos.Whatsapp.Incoming.IncomingWhatsappMessageDto;
import co.za.entelect.services.MessageService;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/whatsapp")
public class WhatsappController {

    private final MessageService _messageService;
    private final Dotenv dotEnv;

    @Autowired
    public WhatsappController(MessageService messageService, Dotenv dotenv) {
        this._messageService = messageService;
        this.dotEnv = dotenv;
    }

    @PostMapping
    public ResponseEntity<?> handleWebhook(@RequestBody IncomingWhatsappMessageDto incomingMessageDto) {
        System.out.println("POST REQUEST HIT");


        if (incomingMessageDto != null) {
            System.out.println(incomingMessageDto);
            _messageService.handleIncomingMessage(incomingMessageDto);
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

        String verifyToken = dotEnv.get("VERIFY_TOKEN");

        if (mode != null && token != null) {
            if ("subscribe".equals(mode) && verifyToken.equals(token)) {
                return new ResponseEntity<>(challenge, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        }

        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
}