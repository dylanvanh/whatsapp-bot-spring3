package co.za.entelect.Facades;

import co.za.entelect.Dtos.Whatsapp.Incoming.IncomingMessageSentResponse;
import co.za.entelect.Dtos.Whatsapp.Incoming.IncomingWhatsappMessageDto;
import co.za.entelect.Dtos.Whatsapp.Outgoing.SendTextMessageDto;
import co.za.entelect.Dtos.Whatsapp.Outgoing.UpdateReadReceiptDto;
import co.za.entelect.Entities.ConversationStateEntity;
import co.za.entelect.Entities.LeaveTypeEntity;
import co.za.entelect.Entities.MessageEntity;
import co.za.entelect.Entities.UserEntity;
import co.za.entelect.Enums.ConversationStateEnum;
import co.za.entelect.repositories.IConversationStateRepository;
import co.za.entelect.repositories.IMessageRepository;
import co.za.entelect.repositories.IUserRepository;
import co.za.entelect.services.IncomingMessageValidator;
import co.za.entelect.services.WhatsappMessageUtils;
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


@Service
public class WhatsappMessageFacade {

    private final RestTemplate restTemplate;
    private final Dotenv dotEnv;
    private final IUserRepository userRepository;
    private final IMessageRepository messageRepository;
    private final WhatsappRequestEntityGenerator whatsappRequestEntityGenerator;
    private final IConversationStateRepository conversationStateRepository;
    private final WhatsappMessageUtils whatsappMessageUtils;
    private final IncomingMessageValidator incomingMessageValidator;

    @Autowired

    public WhatsappMessageFacade(RestTemplate restTemplate, Dotenv dotEnv, IUserRepository userRepository,
                                 IMessageRepository messageRepository, WhatsappRequestEntityGenerator whatsappRequestEntityGenerator,
                                 IConversationStateRepository conversationStateRepository, WhatsappMessageUtils whatsappMessageUtils, IncomingMessageValidator incomingMessageValidator) {
        this.restTemplate = restTemplate;
        this.dotEnv = dotEnv;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
        this.whatsappRequestEntityGenerator = whatsappRequestEntityGenerator;
        this.conversationStateRepository = conversationStateRepository;
        this.whatsappMessageUtils = whatsappMessageUtils;
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

        // Check if on last state -> reset
        if (user.getConversationState().getId() == ConversationStateEnum.END.ordinal()) {
            ConversationStateEntity empNumberState = conversationStateRepository.findById(2L).orElseThrow(()
                    -> new EntityNotFoundException(("Could not find conversation state with id 2")));
            user.setConversationState(empNumberState);
        } else {

            ConversationStateEntity nextConversationState = conversationStateRepository.findById(user.getConversationState()
                    .getId() + 1).orElseThrow(() -> new EntityNotFoundException(
                    ("Could not find conversation state with id " + user.getConversationState().getId() + 1)));
            user.setConversationState(nextConversationState);
        }

        userRepository.save(user);
    }


    private String validateMessageAndDetermineResponse(String messageText, UserEntity user) {
        ConversationStateEnum currentState = incomingMessageValidator.validateCancelRequested(messageText, user);
        if (currentState == null) {
            currentState = ConversationStateEnum.fromId(user.getConversationState().getId().intValue());
        }
        switch (currentState) {
            case GREETING -> {
                incrementConversationStateForUser(user);
                return "Welcome to the Entelect Leave Bot. Please enter your employee email";
            }
            case EMPLOYEE_EMAIL -> {
                String validEmployeeEmail = incomingMessageValidator.validateEmployeeEmail(messageText);
                if (validEmployeeEmail != null) {
                    whatsappMessageUtils.createInitialRequestedLeaveEntity(user, validEmployeeEmail);
                    incrementConversationStateForUser(user);
                    return "Please enter your leave start date in the format dd/mm/yyyy";
                } else {
                    return """
                            Invalid employee email. Please try again\s
                            \s
                            Cancel - CANCEL""";
                }
            }
            case START_DATE -> {
                Date validStartDate = incomingMessageValidator.validateDate(messageText);
                if (validStartDate != null) {
                    incrementConversationStateForUser(user);
                    whatsappMessageUtils.addStartDateToRequestedLeave(user, validStartDate);
                    return "Please enter your leave end date in the format dd/mm/yyyy";
                } else {
                    return """
                            Invalid date. Please try again in format dd/mm/yyyy:\s
                            \s
                            Cancel action - CANCEL\s
                            """;
                }
            }
            case END_DATE -> {
                Date validEndDate = incomingMessageValidator.validateDate(messageText);
                if (validEndDate != null) {
                    incrementConversationStateForUser(user);
                    whatsappMessageUtils.addEndDateToRequestedLeave(user, validEndDate);
                    return """
                            Invalid leave type. Please try again:\s
                            ANNUAL - 1\s
                            SICK - 2\s
                            FAMILY RESPONSIBILITY - 3\s
                            BIRTHDAY - 4\s
                            STUDY - 5\s
                            PARENTAL - 6\s
                            MATERNAL - 7\s
                            \s
                            CANCEL - CANCEL\s
                            """;
                } else {
                    return """
                            Invalid date. Please try again in format dd/mm/yyyy:\s
                            Date - dd/mm/yyyy\s
                            \s
                            Cancel action - CANCEL\s
                            """;
                }
            }
            case LEAVE_TYPE -> {
                LeaveTypeEntity validLeaveType = incomingMessageValidator.validateLeaveType(messageText);
                if (validLeaveType != null) {
                    whatsappMessageUtils.addLeaveTypeToRequestedLeave(user, validLeaveType);
                    incrementConversationStateForUser(user);
                    return """
                            Please confirm your leave request by typing :\s
                            CONFIRM - confirm\s
                            \s
                            CANCEL - cancel\s
                            """;
                } else {
                    return """
                            Please choose your leave type:\s
                            ANNUAL - 1\s
                            SICK - 2\s
                            FAMILY RESPONSIBILITY - 3\s
                            BIRTHDAY - 4\s
                            STUDY - 5\s
                            PARENTAL - 6\s
                            MATERNAL - 7\s
                            \s
                            CANCEL - CANCEL\s
                            """;
                }
            }
            case CONFIRMATION -> {
                boolean validConfirmation = incomingMessageValidator.validateConfirmation(messageText);
                if (validConfirmation) {
                    whatsappMessageUtils.completeLeaveRequest(user);
                    incrementConversationStateForUser(user);
                    return "Your leave request has been submitted";
                } else {
                    return "Invalid confirmation. Please try again by typing confirm";
                }
            }
            case END -> {
                return "This is the end of the road for you cowboy. \n" +
                        "Put down your gun and walk away \n " +
                        "You have no more options \n" +
                        "You have no more choices \n" +
                        "You have no more chances \n" +
                        "Surrender while you still can";
            }
            default -> {
                return "I don't understand";
            }
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
