package co.za.entelect;

import co.za.entelect.dtos.whatsapp.incoming.IncomingWhatsappMessageDto;
import co.za.entelect.facades.WhatsappMessageFacade;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/whatsapp")
public class WhatsappController {

    private final WhatsappMessageFacade _messageService;
    private final Dotenv dotEnv;

    @Autowired
    public WhatsappController(WhatsappMessageFacade messageService, Dotenv dotenv) {
        this._messageService = messageService;
        this.dotEnv = dotenv;
    }

    @PostMapping
    public ResponseEntity<?> handleWebhook(@RequestBody IncomingWhatsappMessageDto incomingMessageDto) {

        if (incomingMessageDto == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        _messageService.handleIncomingMessage(incomingMessageDto);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<String> verifyWebhook(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String token,
            @RequestParam("hub.challenge") String challenge) {

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
