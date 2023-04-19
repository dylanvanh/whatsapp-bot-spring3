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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
public class WhatsappMessageUtils {

    private final IRequestedLeaveRepository requestedLeaveRepository;
    private final IUserRepository userRepository;
    private final IConversationStateRepository conversationStateRepository;

    @Autowired
    public WhatsappMessageUtils(IRequestedLeaveRepository requestedLeaveRepository, IUserRepository userRepository,
                                IConversationStateRepository iConversationStateRepository) {
        this.requestedLeaveRepository = requestedLeaveRepository;
        this.userRepository = userRepository;
        this.conversationStateRepository = iConversationStateRepository;
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

    public UserEntity getOrCreateUserEntity(String phoneNumberId, String fromNumber, String userName) {
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
            ConversationStateEntity choiceConversationState = conversationStateRepository.
                    findById((long) ConversationStateEnum.CHOICE.getId()).orElseThrow(
                            () -> new EntityNotFoundException("CHOICE Conversation state not found"));
            user.setConversationState(choiceConversationState);
            userRepository.save(user);
        }
    }

    public String getRequestedLeaveForUser(UserEntity user) {
        // #TODO - get requested leave for user
        //get all requested leave for user
        List<RequestedLeaveEntity> requestedLeaveList = requestedLeaveRepository.findAllByUserId(user.getId());

        return requestedLeaveList.toString();
    }
}
