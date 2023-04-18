package co.za.entelect.Facades;

import co.za.entelect.Dtos.Whatsapp.Incoming.IncomingMessageSentResponse;
import co.za.entelect.Dtos.Whatsapp.Incoming.IncomingWhatsappMessageDto;
import co.za.entelect.Dtos.Whatsapp.Outgoing.SendTextMessageDto;
import co.za.entelect.Dtos.Whatsapp.Outgoing.UpdateReadReceiptDto;
import co.za.entelect.Entities.ConversationStateEntity;
import co.za.entelect.Entities.MessageEntity;
import co.za.entelect.Entities.UserEntity;
import co.za.entelect.Enums.ConversationStateEnum;
import co.za.entelect.repositories.ConversationStateRepository;
import co.za.entelect.repositories.IMessageRepository;
import co.za.entelect.repositories.IUserRepository;
import co.za.entelect.services.IncomingMessageValidator;
import co.za.entelect.services.WhatsappRequestEntityGenerator;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

import static co.za.entelect.Enums.ConversationStateEnum.GREETING;


@Service
public class WhatsappMessageFacade {

    private final RestTemplate restTemplate;
    private final Dotenv dotEnv;
    private final IUserRepository userRepository;
    private final IMessageRepository messageRepository;
    private final WhatsappRequestEntityGenerator whatsappRequestEntityGenerator;
    private final ConversationStateRepository conversationStateRepository;

    private final IncomingMessageValidator incomingMessageValidator;

    @Autowired

    public WhatsappMessageFacade(RestTemplate restTemplate, Dotenv dotEnv, IUserRepository userRepository,
                                 IMessageRepository messageRepository, WhatsappRequestEntityGenerator whatsappRequestEntityGenerator,
                                 ConversationStateRepository conversationStateRepository, IncomingMessageValidator incomingMessageValidator) {
        this.restTemplate = restTemplate;
        this.dotEnv = dotEnv;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
        this.whatsappRequestEntityGenerator = whatsappRequestEntityGenerator;
        this.conversationStateRepository = conversationStateRepository;
        this.incomingMessageValidator = incomingMessageValidator;
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

        SendTextMessageDto sendTextMessageDto = generateResponse(fromNumber, messageText, phoneNumberId);
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

    private SendTextMessageDto generateResponse(String fromNumber, String messageText, String phoneNumberId) {

        UserEntity user = userRepository.findByPhoneNumberId(phoneNumberId);
        String response = validateMessageAndDetermineResponse(messageText, user);

        SendTextMessageDto sendTextMessageDto = SendTextMessageDto.builder()
                .messaging_product("whatsapp")
                .to(fromNumber)
                .text(SendTextMessageDto.MessageText
                        .builder()
                        .body(response).build())
                .build();

        return sendTextMessageDto;
    }

    private void incrementConversationStateForUser(UserEntity user) {

        //        Check if on last state -> reset
        if (user.getConversationState().getId() == ConversationStateEnum.END.ordinal()) {
            ConversationStateEntity startingState = conversationStateRepository.findById(1L).orElseThrow(()
                    -> new EntityNotFoundException(("Could not find conversation state with id 1")));
            user.setConversationState(startingState);
        } else {

            ConversationStateEntity nextConversationState = conversationStateRepository.findById(user.getConversationState()
                    .getId() + 1).orElseThrow(() -> new EntityNotFoundException(
                    ("Could not find conversation state with id " + user.getConversationState().getId() + 1)));
            user.setConversationState(nextConversationState);
        }

        userRepository.save(user);
    }


    private String validateMessageAndDetermineResponse(String messageText, UserEntity user) {
        ConversationStateEnum currentState = ConversationStateEnum.fromId(user.getConversationState().getId().intValue());
        switch (currentState) {
            case GREETING:
                incrementConversationStateForUser(user);
                return "Welcome to the Entelect Leave Bot. Please enter your employee number";
            case EMPLOYEE_NUMBER:
                boolean validEmployeeNumber = incomingMessageValidator.validateEmployeeNumber(messageText);
                if (validEmployeeNumber) {
                    incrementConversationStateForUser(user);
                    return "Please enter your leave start date in the format dd/mm/yyyy";
                } else {
                    return "Invalid employee number. Please try again";
                }
            case START_DATE:
                Date validStartDate = incomingMessageValidator.validateDate(messageText);
                if (validStartDate != null) {
                    incrementConversationStateForUser(user);
                    return "Please enter your leave end date in the format dd/mm/yyyy";
                } else {
                    return "Invalid date. Please try again in format dd/mm/yyyy";
                }
            case END_DATE:
                Date validEndDate = incomingMessageValidator.validateDate(messageText);
                if (validEndDate != null) {
                    incrementConversationStateForUser(user);
                    return "Please enter your leave type: \n" +
                            "ANNUAL - 1 \n" +
                            "SICK - 2 \n" +
                            "FAMILY RESPONSIBILITY - 3 \n" +
                            "BIRTHDAY - 4 \n" +
                            "STUDY - 5 \n" +
                            "PARENTAL - 6 \n" +
                            "MATERNAL - 7 \n";
                } else {
                    return "Invalid date. Please try again in format dd/mm/yyyy";
                }
            case LEAVE_TYPE:
                boolean validLeaveType = incomingMessageValidator.validateLeaveType(messageText);
                if (validLeaveType) {
                    incrementConversationStateForUser(user);
                    return "Please confirm your leave request by typing CONFIRMATION";
                } else {
                    return "Invalid leave type. Please try again: \n" +
                            "ANNUAL - 1 \n" +
                            "SICK - 2 \n" +
                            "FAMILY RESPONSIBILITY - 3 \n" +
                            "BIRTHDAY - 4 \n" +
                            "STUDY - 5 \n" +
                            "PARENTAL - 6 \n" +
                            "MATERNAL - 7 \n";
                }
            case CONFIRMATION:
                return "ASD";
            case END:
                return "ASD";
            default:
                return "I don't understand";
        }
    }


    private UserEntity getOrCreateUserEntity(String phoneNumberId, String fromNumber, String userName) {
        UserEntity existingUser = userRepository.findByPhoneNumberId(phoneNumberId);
        if (existingUser == null) {
            ConversationStateEntity startingConversationState = conversationStateRepository.findById(1L).orElseThrow(()
                    -> new EntityNotFoundException(("Could not find conversation state with id 1")));
            existingUser = UserEntity.builder()
                    .phone(fromNumber)
                    .phoneNumberId(phoneNumberId)
                    .name(userName)
                    .conversationState(startingConversationState)
                    .build();
            userRepository.save(existingUser);
        }
        return existingUser;
    }

    private void saveMessage(String messageId, String fromNumber, String messageText, String phoneNumberId,
                             String timeStamp, String userName) {

        UserEntity existingUser = getOrCreateUserEntity(phoneNumberId, fromNumber, userName);

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
