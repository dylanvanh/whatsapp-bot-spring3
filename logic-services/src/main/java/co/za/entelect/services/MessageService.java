package co.za.entelect.services;

import co.za.entelect.Dtos.Whatsapp.Incoming.IncomingMessageSentResponse;
import co.za.entelect.Dtos.Whatsapp.Incoming.IncomingWhatsappMessageDto;
import co.za.entelect.Dtos.Whatsapp.Outgoing.SentTextMessageDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
public class MessageService {

    private final RestTemplate restTemplate;
    private final Dotenv dotEnv;

    @Autowired
    public MessageService(RestTemplate restTemplate, Dotenv dotEnv) {
        this.restTemplate = restTemplate;
        this.dotEnv = dotEnv;
    }

    public boolean handleIncomingMessage(IncomingWhatsappMessageDto incomingMessageDto) {

        //get important details
        String timestamp = incomingMessageDto.entry().get(0).changes().get(0).value().messages().get(0).timestamp();
        String id = incomingMessageDto.entry().get(0).changes().get(0).value().messages().get(0).id();
        String fromNumber = incomingMessageDto.entry().get(0).changes().get(0).value().messages().get(0).from();
        String messageText = incomingMessageDto.entry().get(0).changes().get(0).value().messages().get(0).text().body();
        String phoneNumberId = incomingMessageDto.entry().get(0).changes().get(0).value().metadata().phone_number_id();
        SentTextMessageDto sendTextMessageDto = SentTextMessageDto.builder().messaging_product("whatsapp").to(fromNumber)
                .text(SentTextMessageDto.MessageText.builder().body("You said : " + messageText).build()).build();

        String whatsappToken = dotEnv.get("WHATSAPP_TOKEN");

        HttpEntity entity = generateEntity(sendTextMessageDto);
        String url = String.format("https://graph.facebook.com/v12.0/%s/messages?access_token=%s", phoneNumberId, whatsappToken);

        System.out.println(url);
        System.out.println(fromNumber);

        try {
            restTemplate.exchange(url, HttpMethod.POST, entity, IncomingMessageSentResponse.class);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return true;

    }

    private HttpHeaders generateHeader() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        return httpHeaders;
    }

    private HttpEntity<String> generateEntity(SentTextMessageDto sendTextMessageDto) {
        HttpHeaders httpHeaders = generateHeader();

        String body;
        try {
            body = new ObjectMapper().writeValueAsString(sendTextMessageDto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        HttpEntity<String> entity = new HttpEntity<>(body, httpHeaders);
        return entity;
    }


    private String generateBody() {
        return "s";
    }


}
