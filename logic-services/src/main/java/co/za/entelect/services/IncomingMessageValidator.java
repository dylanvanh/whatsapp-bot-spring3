package co.za.entelect.services;

import co.za.entelect.Entities.ConversationStateEntity;
import co.za.entelect.Entities.LeaveTypeEntity;
import co.za.entelect.Entities.RequestedLeaveEntity;
import co.za.entelect.Entities.UserEntity;
import co.za.entelect.Enums.ConversationStateEnum;
import co.za.entelect.Enums.LeaveTypeEnum;
import co.za.entelect.repositories.IConversationStateRepository;
import co.za.entelect.repositories.ILeaveTypeRepository;
import co.za.entelect.repositories.IRequestedLeaveRepository;
import co.za.entelect.repositories.IUserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class IncomingMessageValidator {
    private final IRequestedLeaveRepository requestedLeaveRepository;
    private final IConversationStateRepository conversationStateRepository;
    private final IUserRepository userRepository;
    private final ILeaveTypeRepository leaveTypeRepository;

    public IncomingMessageValidator(IRequestedLeaveRepository requestedLeaveRepository, IConversationStateRepository conversationStateRepository, IUserRepository iUserRepository, ILeaveTypeRepository iLeaveTypeRepository) {
        this.requestedLeaveRepository = requestedLeaveRepository;
        this.conversationStateRepository = conversationStateRepository;
        this.userRepository = iUserRepository;
        this.leaveTypeRepository = iLeaveTypeRepository;
    }

    public String validateEmployeeEmail(String employeeEmail) {
        String cleanedEmail = employeeEmail.strip().toLowerCase();
        //validate if the string is an email
        boolean isValidEmail = employeeEmail.matches("^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+$") && employeeEmail.contains("@entelect.co.za");
        if (!isValidEmail) {
            return null;
        }
        return cleanedEmail;
    }

    public Date validateDate(String date) {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        try {
            return dateFormat.parse(date);
        } catch (ParseException e) {
            return null;
        }
    }


    public LeaveTypeEntity validateLeaveType(String messageText) {
        try {
            LeaveTypeEnum validLeaveType = LeaveTypeEnum.fromId(Integer.parseInt(messageText));
            if (validLeaveType == null) {
                return null;
            }
            LeaveTypeEntity leaveTypeEntity = leaveTypeRepository.findById((long) validLeaveType.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Leave type not found"));
            return leaveTypeEntity;
        } catch (Exception e) {
            return null;
        }
    }

    public boolean validateConfirmation(String messageText) {
        String cleanedMessageText = messageText.strip().toLowerCase();
        return cleanedMessageText.equals("confirm");
    }

    public ConversationStateEnum validateCancelRequested(String messageText, UserEntity user) {
        String cleanedMessageText = messageText.strip().toLowerCase();
        if (cleanedMessageText.equals("cancel")) {
            //delete existing requested leave entity
            RequestedLeaveEntity requestedLeaveEntity = requestedLeaveRepository.
                    findTopByUserIdAndRequestJourneyCompletedStatusOrderByRequestCreatedDateDesc(user.getId(),
                            false);
            requestedLeaveRepository.delete(requestedLeaveEntity);

            if (user.getEmail() != null) {
                //reset to the START_DATE state -> already have user details
                long newConversationStateId = ConversationStateEnum.START_DATE.getId();
                ConversationStateEntity conversationStateEntity = conversationStateRepository.findById(
                        newConversationStateId).orElseThrow(
                        () -> new EntityNotFoundException("START_DATE entity not found"));
                user.setConversationState(conversationStateEntity);
                userRepository.save(user);
                return ConversationStateEnum.START_DATE;
            } else {
                //reset conversation state to GREETING
                long newConversationStateId = ConversationStateEnum.GREETING.getId();
                ConversationStateEntity conversationStateEntity = conversationStateRepository.findById(
                        newConversationStateId).orElseThrow(
                        () -> new EntityNotFoundException("GREETING entity not found"));
                user.setConversationState(conversationStateEntity);
                userRepository.save(user);
                return ConversationStateEnum.GREETING;
            }
        }
        return null;
    }
}
