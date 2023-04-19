package co.za.entelect.services;

import co.za.entelect.Entities.ConversationStateEntity;
import co.za.entelect.Entities.LeaveTypeEntity;
import co.za.entelect.Entities.RequestedLeaveEntity;
import co.za.entelect.Entities.UserEntity;
import co.za.entelect.Enums.ConversationStateEnum;
import co.za.entelect.repositories.IConversationStateRepository;
import co.za.entelect.repositories.IRequestedLeaveRepository;
import co.za.entelect.repositories.IUserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;

@Service
public class WhatsappMessageUtils {

    private final IRequestedLeaveRepository requestedLeaveRepository;
    private final IUserRepository userRepository;
    private final IConversationStateRepository iConversationStateRepository;

    public WhatsappMessageUtils(IRequestedLeaveRepository requestedLeaveRepository, IUserRepository userRepository,
                                IConversationStateRepository iConversationStateRepository) {
        this.requestedLeaveRepository = requestedLeaveRepository;
        this.userRepository = userRepository;
        this.iConversationStateRepository = iConversationStateRepository;
    }

    public void updateEmployeeEmail(UserEntity user, String validEmployeeEmail) {
        user.setEmail(validEmployeeEmail);
        userRepository.save(user);
    }

    public void generateNewRequestedLeaveEntity(UserEntity user) {
        RequestedLeaveEntity newLeaveEntity = RequestedLeaveEntity.builder()
                .userId(user.getId())
                .requestApprovedStatus(false)
                .requestJourneyCompletedStatus(false)
                .build();

        requestedLeaveRepository.save(newLeaveEntity);
    }

    public void addStartDateToRequestedLeave(UserEntity user, Date validStartDate) {
        RequestedLeaveEntity requestedLeaveEntity = requestedLeaveRepository.findTopByUserIdOrderByIdDesc(user.getId());
        requestedLeaveEntity.setStartDate(validStartDate);
        requestedLeaveRepository.save(requestedLeaveEntity);
    }

    public void addEndDateToRequestedLeave(UserEntity user, Date validEndDate) {
        RequestedLeaveEntity requestedLeaveEntity = requestedLeaveRepository.findTopByUserIdOrderByIdDesc(user.getId());
        requestedLeaveEntity.setEndDate(validEndDate);
        requestedLeaveRepository.save(requestedLeaveEntity);
    }

    public void addLeaveTypeToRequestedLeave(UserEntity user, LeaveTypeEntity validLeaveType) {
        RequestedLeaveEntity requestedLeaveEntity = requestedLeaveRepository.findTopByUserIdOrderByIdDesc(user.getId());
        requestedLeaveEntity.setLeaveType(validLeaveType);
        requestedLeaveRepository.save(requestedLeaveEntity);
    }

    public void completeLeaveRequest(UserEntity user) {
        RequestedLeaveEntity requestedLeaveEntity = requestedLeaveRepository.findTopByUserIdOrderByIdDesc(user.getId());
        requestedLeaveEntity.setRequestJourneyCompletedStatus(true);
        requestedLeaveEntity.setRequestCreatedDate(LocalDateTime.now());
        requestedLeaveRepository.save(requestedLeaveEntity);
    }

    public void cancelLeaveRequest(UserEntity user) {
        if (user.getEmail() != null) {
            RequestedLeaveEntity requestedLeaveEntity = requestedLeaveRepository.
                    findTopByUserIdAndRequestJourneyCompletedStatusOrderByRequestCreatedDateDesc(user.getId(),
                            false);
            requestedLeaveRepository.delete(requestedLeaveEntity);
            //reset to enter start date state
            ConversationStateEntity choiceConversationState = iConversationStateRepository.
                    findById((long) ConversationStateEnum.CHOICE.getId()).orElseThrow(
                            () -> new EntityNotFoundException("CHOICE Conversation state not found"));
            user.setConversationState(choiceConversationState);
            userRepository.save(user);
        }
//        else {
//            //reset to enter email state
//            ConversationStateEntity conversationStateEntity = iConversationStateRepository.
//                    findById((long) ConversationStateEnum.EMPLOYEE_EMAIL.getId()).orElseThrow(
//                            () -> new EntityNotFoundException("EMPLOYEE EMAIL Conversation state not found"));
//            user.setConversationState(conversationStateEntity);
//        }
    }

    public String getRequestedLeaveForUser(UserEntity user) {
        // #TODO - get requested leave for user
        return "TODO - getRequestedLeaveForUser";
    }
}
