package co.za.entelect.services;

import co.za.entelect.Dtos.Whatsapp.Incoming.IncomingMessageSentResponse;
import co.za.entelect.Dtos.Whatsapp.Incoming.IncomingWhatsappMessageDto;
import co.za.entelect.Dtos.Whatsapp.Outgoing.SendTextMessageDto;
import co.za.entelect.Dtos.Whatsapp.Outgoing.UpdateReadReceiptDto;
import co.za.entelect.Entities.MessageEntity;
import co.za.entelect.Entities.UserEntity;
import co.za.entelect.repositories.MessageRepository;
import co.za.entelect.repositories.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import org.hibernate.sql.Update;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneOffset;


@Service
public class MessageService {

    private final RestTemplate restTemplate;
    private final Dotenv dotEnv;

    private final UserRepository userRepository;
    private final MessageRepository messageRepository;

    @Autowired
    public MessageService(RestTemplate restTemplate, Dotenv dotEnv, UserRepository userRepository,
                          MessageRepository messageRepository) {
        this.restTemplate = restTemplate;
        this.dotEnv = dotEnv;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
    }

    public void handleIncomingMessage(IncomingWhatsappMessageDto incomingMessageDto) {
        String timestamp = incomingMessageDto.entry().get(0).changes().get(0).value().messages().get(0).timestamp();
        String messageId = incomingMessageDto.entry().get(0).changes().get(0).value().messages().get(0).id();
        String fromNumber = incomingMessageDto.entry().get(0).changes().get(0).value().messages().get(0).from();
        String userName = incomingMessageDto.entry().get(0).changes().get(0).value().contacts().get(0).profile().name();
        String messageText = incomingMessageDto.entry().get(0).changes().get(0).value().messages().get(0).text().body();
        String phoneNumberId = incomingMessageDto.entry().get(0).changes().get(0).value().metadata().phone_number_id();

        markMessageAsRead(messageId, phoneNumberId);
        sendTextResponse(messageId, fromNumber, messageText, phoneNumberId, timestamp, userName);
    }


    private void sendTextResponse(String messageId, String fromNumber, String messageText, String phoneNumberId,
                                  String timestamp, String userName) {
        String whatsappToken = dotEnv.get("WHATSAPP_TOKEN");

        SendTextMessageDto sendTextMessageDto = generateResponse(messageId, fromNumber, messageText, phoneNumberId,
                timestamp, userName);
        HttpEntity<String> entity = generateEntity(sendTextMessageDto);
        String url = String.format("https://graph.facebook.com/v12.0/%s/messages?access_token=%s", phoneNumberId,
                whatsappToken);

        try {
            restTemplate.exchange(url, HttpMethod.POST, entity, IncomingMessageSentResponse.class);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }


    private SendTextMessageDto generateResponse(String messageId, String fromNumber, String messageText,
                                                String phoneNumberId, String timeStamp, String userName) {

        String response = "You said : " + messageText;

        UserEntity existingUser = userRepository.findByPhoneNumberId(phoneNumberId);
        if (existingUser == null) {
            existingUser = UserEntity.builder()
                    .phone(fromNumber)
                    .phoneNumberId(phoneNumberId)
                    .name(userName)
                    .build();
            userRepository.save(existingUser);
        }

        MessageEntity newMessage = MessageEntity.builder()
                .message(messageText)
                .receivedDateTime(LocalDateTime.ofEpochSecond(Long.parseLong(timeStamp), 0, ZoneOffset.UTC))
                .messageId(messageId)
                .user(existingUser)
                .build();
        messageRepository.save(newMessage);

        SendTextMessageDto sendTextMessageDto = SendTextMessageDto.builder()
                .messaging_product("whatsapp")
                .to(fromNumber)
                .text(SendTextMessageDto.MessageText
                        .builder()
                        .body(response).build())
                .build();

        return sendTextMessageDto;
    }


    private void markMessageAsRead(String messageId, String phoneNumberId) {

        //Update read receipt to read
        UpdateReadReceiptDto updateReadReceiptDto = UpdateReadReceiptDto.builder()
                .messagingProduct("whatsapp")
                .status("read")
                .messageId(messageId)
                .build();

        HttpEntity<String> entity = generateEntity(updateReadReceiptDto);
        String url = String.format("https://graph.facebook.com/v16.0/%s/messages", phoneNumberId);

        try {
            restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


    private HttpHeaders generateBasicHeader() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        return httpHeaders;
    }


    private HttpHeaders generateHeaderWithAuth() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.setBearerAuth(dotEnv.get("WHATSAPP_TOKEN"));
        return httpHeaders;
    }

    private HttpEntity<String> generateEntity(UpdateReadReceiptDto updateReadReceiptDto) {
        HttpHeaders httpHeaders = generateHeaderWithAuth();

        String body;
        try {
            body = new ObjectMapper().writeValueAsString(updateReadReceiptDto);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }

        HttpEntity<String> entity = new HttpEntity<>(body, httpHeaders);
        return entity;
    }


    private HttpEntity<String> generateEntity(SendTextMessageDto sendTextMessageDto) {
        HttpHeaders httpHeaders = generateBasicHeader();

        String body;
        try {
            body = new ObjectMapper().writeValueAsString(sendTextMessageDto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        HttpEntity<String> entity = new HttpEntity<>(body, httpHeaders);
        return entity;
    }
}
