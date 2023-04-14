package co.za.entelect.Facades;

import co.za.entelect.Dtos.Whatsapp.Incoming.IncomingMessageSentResponse;
import co.za.entelect.Dtos.Whatsapp.Incoming.IncomingWhatsappMessageDto;
import co.za.entelect.Dtos.Whatsapp.Outgoing.SendTextMessageDto;
import co.za.entelect.Dtos.Whatsapp.Outgoing.UpdateReadReceiptDto;
import co.za.entelect.Entities.MessageEntity;
import co.za.entelect.Entities.UserEntity;
import co.za.entelect.repositories.MessageRepository;
import co.za.entelect.repositories.UserRepository;
import co.za.entelect.services.WhatsappRequestEntityGenerator;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneOffset;


@Service
public class WhatsappMessageFacade {

    private final RestTemplate restTemplate;
    private final Dotenv dotEnv;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final WhatsappRequestEntityGenerator whatsappRequestEntityGenerator;

    @Autowired

    public WhatsappMessageFacade(RestTemplate restTemplate, Dotenv dotEnv, UserRepository userRepository,
                                 MessageRepository messageRepository, WhatsappRequestEntityGenerator whatsappRequestEntityGenerator) {
        this.restTemplate = restTemplate;
        this.dotEnv = dotEnv;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
        this.whatsappRequestEntityGenerator = whatsappRequestEntityGenerator;
    }

    public void handleIncomingMessage(IncomingWhatsappMessageDto incomingMessageDto) {
        String timestamp = incomingMessageDto.entry().get(0).changes().get(0).value().messages().get(0).timestamp();
        String messageId = incomingMessageDto.entry().get(0).changes().get(0).value().messages().get(0).id();
        String fromNumber = incomingMessageDto.entry().get(0).changes().get(0).value().messages().get(0).from();
        String userName = incomingMessageDto.entry().get(0).changes().get(0).value().contacts().get(0).profile().name();
        String messageText = incomingMessageDto.entry().get(0).changes().get(0).value().messages().get(0).text().body();
        String phoneNumberId = incomingMessageDto.entry().get(0).changes().get(0).value().metadata().phone_number_id();

        saveMessage(messageId, fromNumber, messageText, phoneNumberId, timestamp, userName);
        markMessageAsRead(messageId, phoneNumberId);
        sendTextResponse(fromNumber, messageText, phoneNumberId);
    }


    private void sendTextResponse(String fromNumber, String messageText, String phoneNumberId) {
        String whatsappToken = dotEnv.get("WHATSAPP_TOKEN");

        SendTextMessageDto sendTextMessageDto = generateResponse(fromNumber, messageText);
        HttpEntity<String> entity = whatsappRequestEntityGenerator.generateEntity(sendTextMessageDto,
                false);
        String url = String.format("https://graph.facebook.com/v12.0/%s/messages?access_token=%s", phoneNumberId,
                whatsappToken);

        try {
            restTemplate.exchange(url, HttpMethod.POST, entity, IncomingMessageSentResponse.class);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    private SendTextMessageDto generateResponse(String fromNumber, String messageText) {

        String response = "You said : " + messageText;

        SendTextMessageDto sendTextMessageDto = SendTextMessageDto.builder()
                .messaging_product("whatsapp")
                .to(fromNumber)
                .text(SendTextMessageDto.MessageText
                        .builder()
                        .body(response).build())
                .build();

        return sendTextMessageDto;
    }

    private void saveMessage(String messageId, String fromNumber, String messageText, String phoneNumberId,
                             String timeStamp, String userName) {

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
    }


    private void markMessageAsRead(String messageId, String phoneNumberId) {

        UpdateReadReceiptDto updateReadReceiptDto = UpdateReadReceiptDto.builder()
                .messagingProduct("whatsapp")
                .status("read")
                .messageId(messageId)
                .build();

        HttpEntity<String> entity = whatsappRequestEntityGenerator.generateEntity(updateReadReceiptDto, true);
        String url = String.format("https://graph.facebook.com/v16.0/%s/messages", phoneNumberId);

        try {
            restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}
