package co.za.entelect.services;

import co.za.entelect.Entities.LeaveTypeEntity;
import co.za.entelect.Entities.RequestedLeaveEntity;
import co.za.entelect.Entities.UserEntity;
import co.za.entelect.repositories.IRequestedLeaveRepository;
import co.za.entelect.repositories.IUserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;

@Service
public class WhatsappMessageUtils {

    private final IRequestedLeaveRepository requestedLeaveRepository;
    private final IUserRepository userRepository;

    public WhatsappMessageUtils(IRequestedLeaveRepository requestedLeaveRepository, IUserRepository userRepository) {
        this.requestedLeaveRepository = requestedLeaveRepository;
        this.userRepository = userRepository;
    }

    public void createInitialRequestedLeaveEntity(UserEntity user, String validEmployeeEmail) {

        //check for existing email
        UserEntity existingUser = userRepository.findByEmail(validEmployeeEmail);
        if (existingUser != null) {
            user = existingUser;
        } else {
            user.setEmail(validEmployeeEmail);
            userRepository.save(user);
        }

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
}
