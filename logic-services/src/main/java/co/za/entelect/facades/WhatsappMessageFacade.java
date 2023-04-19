package co.za.entelect.facades;

import co.za.entelect.dtos.whatsapp.incoming.IncomingMessageSentResponse;
import co.za.entelect.dtos.whatsapp.incoming.IncomingWhatsappMessageDto;
import co.za.entelect.dtos.whatsapp.outgoing.SendTextMessageDto;
import co.za.entelect.dtos.whatsapp.outgoing.UpdateReadReceiptDto;
import co.za.entelect.exceptions.DateException;
import co.za.entelect.constants.WhatsappResponse;
import co.za.entelect.entities.ConversationStateEntity;
import co.za.entelect.entities.LeaveTypeEntity;
import co.za.entelect.entities.MessageEntity;
import co.za.entelect.entities.UserEntity;
import co.za.entelect.enums.ConversationStateEnum;
import co.za.entelect.enums.UserChoiceEnum;
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

        return SendTextMessageDto.builder()
                .messaging_product("whatsapp")
                .to(fromNumber)
                .text(SendTextMessageDto.MessageText
                        .builder()
                        .body(response).build())
                .build();
    }

    private void updateConversationStateForUser(UserEntity user, ConversationStateEnum conversationStateEnum) {
        ConversationStateEntity newConversationState = conversationStateRepository.findById(
                Long.valueOf(conversationStateEnum.getId())).orElseThrow(
                () -> new EntityNotFoundException(
                        ("Could not find conversation state with id " + conversationStateEnum.getId())
                ));
        user.setConversationState(newConversationState);
        userRepository.save(user);
    }


    private String validateMessageAndDetermineResponse(String messageText, UserEntity user) {
        ConversationStateEnum currentState = incomingMessageValidator.validateCancelRequested(messageText, user);
        if (currentState != ConversationStateEnum.CANCEL) {
            currentState = ConversationStateEnum.fromId(user.getConversationState().getId().intValue());
        }
        switch (currentState) {
            case GREETING -> {
                updateConversationStateForUser(user, ConversationStateEnum.EMPLOYEE_EMAIL);
                return WhatsappResponse.GREETING;
            }
            case EMPLOYEE_EMAIL -> {
                String validEmployeeEmail = incomingMessageValidator.validateEmployeeEmail(messageText);
                if (validEmployeeEmail != null) {
                    whatsappMessageUtils.updateEmployeeEmail(user, validEmployeeEmail);
                    updateConversationStateForUser(user, ConversationStateEnum.CHOICE);
                    return WhatsappResponse.CHOICE_INITIAL;
                } else {
                    return WhatsappResponse.INVALID_EMAIL;
                }
            }
            case CHOICE -> {
                UserChoiceEnum validChoice = incomingMessageValidator.validateChoice(messageText);
                if (validChoice != null) {
                    if (validChoice == UserChoiceEnum.MAKE_LEAVE_REQUEST) {
                        whatsappMessageUtils.generateNewRequestedLeaveEntity(user);
                        updateConversationStateForUser(user, ConversationStateEnum.START_DATE);
                        return WhatsappResponse.START_DATE_INITIAL;
                    } else if (validChoice == UserChoiceEnum.VIEW_LEAVE_REQUESTS) {
                        return whatsappMessageUtils.getRequestedLeaveForUser(user);
                    }
                } else {
                    return WhatsappResponse.CHOICE_INVALID;
                }
            }
            case START_DATE -> {
                try {
                    Date validStartDate = incomingMessageValidator.validateStartDate(messageText);
                    updateConversationStateForUser(user, ConversationStateEnum.END_DATE);
                    whatsappMessageUtils.addStartDateToRequestedLeave(user, validStartDate);
                    return WhatsappResponse.END_DATE_INITIAL;
                } catch (DateException.InvalidDateFormatException e) {
                    return WhatsappResponse.START_DATE_INVALID_FORMAT;
                } catch (DateException.DateInPastException e) {
                    return WhatsappResponse.START_DATE_IN_PAST;
                }
            }
            case END_DATE -> {
                try {
                    Date validEndDate = incomingMessageValidator.validateEndDate(messageText, user);
                    updateConversationStateForUser(user, ConversationStateEnum.LEAVE_TYPE);
                    whatsappMessageUtils.addEndDateToRequestedLeave(user, validEndDate);
                    return WhatsappResponse.LEAVE_TYPE_INITIAL;
                } catch (DateException.InvalidDateFormatException e) {
                    return WhatsappResponse.END_DATE_INVALID_FORMAT;
                } catch (DateException.DateBeforeStartDateException e) {
                    return WhatsappResponse.END_DATE_BEFORE_START_DATE;
                }
            }
            case LEAVE_TYPE -> {
                LeaveTypeEntity validLeaveType = incomingMessageValidator.validateLeaveType(messageText);
                if (validLeaveType != null) {
                    whatsappMessageUtils.addLeaveTypeToRequestedLeave(user, validLeaveType);
                    updateConversationStateForUser(user, ConversationStateEnum.CONFIRMATION);
                    return WhatsappResponse.CONFIRMATION_INITIAL;
                } else {
                    return WhatsappResponse.LEAVE_TYPE_INVALID;
                }
            }
            case CONFIRMATION -> {
                boolean validConfirmation = incomingMessageValidator.validateConfirmation(messageText);
                if (validConfirmation) {
                    whatsappMessageUtils.completeLeaveRequest(user);
                    updateConversationStateForUser(user, ConversationStateEnum.CHOICE);
                    return WhatsappResponse.CONFIRMATION_VALID;
                } else {
                    return WhatsappResponse.CONFIRMATION_INVALID;
                }
            }
            case END -> {
                return """
                         Invalid input provided. Please try again:\s
                         CONFIRM - confirm \s
                         \s
                         CANCEL - cancel
                        """;
            }
            case CANCEL -> {
                whatsappMessageUtils.cancelLeaveRequest(user);
                return WhatsappResponse.CANCEL;
            }
            default -> {
                return "I don't understand";
            }
        }
        return "ERROR";
    }

    private void saveMessage(String messageId, String fromNumber, String messageText, String phoneNumberId,
                             String timeStamp, String userName) {

        UserEntity existingUser = whatsappMessageUtils.getOrCreateUserEntity(phoneNumberId, fromNumber, userName);

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
