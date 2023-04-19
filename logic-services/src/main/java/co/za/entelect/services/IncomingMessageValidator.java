package co.za.entelect.services;

import co.za.entelect.entities.LeaveTypeEntity;
import co.za.entelect.entities.RequestedLeaveEntity;
import co.za.entelect.entities.UserEntity;
import co.za.entelect.enums.ConversationStateEnum;
import co.za.entelect.enums.LeaveTypeEnum;
import co.za.entelect.enums.UserChoiceEnum;
import co.za.entelect.Exceptions.DateException;
import co.za.entelect.repositories.ILeaveTypeRepository;
import co.za.entelect.repositories.IRequestedLeaveRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Service
public class IncomingMessageValidator {
    private final IRequestedLeaveRepository requestedLeaveRepository;
    private final ILeaveTypeRepository leaveTypeRepository;

    @Autowired
    public IncomingMessageValidator(IRequestedLeaveRepository requestedLeaveRepository,
                                    ILeaveTypeRepository iLeaveTypeRepository) {
        this.requestedLeaveRepository = requestedLeaveRepository;
        this.leaveTypeRepository = iLeaveTypeRepository;
    }

    public String validateEmployeeEmail(String employeeEmail) {
        String cleanedEmail = employeeEmail.strip().toLowerCase();
        boolean isValidEmail = employeeEmail.matches("^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+$") && employeeEmail.contains("@entelect.co.za");
        if (!isValidEmail) {
            return null;
        }
        return cleanedEmail;
    }

    public Date validateStartDate(String date) {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        dateFormat.setLenient(false);
        try {
            Date parsedDate = dateFormat.parse(date);
            Date todaysDate = getCurrentDateWithoutTime();
            if (parsedDate.before(todaysDate)) {
                throw new DateException.DateInPastException("");
            }
            return dateFormat.parse(date);
        } catch (ParseException e) {
            throw new DateException.InvalidDateFormatException("");
        }
    }

    public Date validateEndDate(String date, UserEntity user) {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        dateFormat.setLenient(false);
        try {
            Date parsedDate = dateFormat.parse(date);

            RequestedLeaveEntity requestedLeaveEntity = requestedLeaveRepository
                    .findTopByUserIdAndRequestJourneyCompletedStatusOrderByRequestCreatedDateDesc(
                            user.getId(), false);

            Date startDate = requestedLeaveEntity.getStartDate();

            if (!parsedDate.after(startDate)) {
                throw new DateException.DateBeforeStartDateException("");
            }

            int totalDays = (int) ((parsedDate.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24));
            requestedLeaveEntity.setDayCount(totalDays);
            requestedLeaveRepository.save(requestedLeaveEntity);

            return dateFormat.parse(date);
        } catch (ParseException e) {
            throw new DateException.InvalidDateFormatException("");
        }
    }

    private Date getCurrentDateWithoutTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public LeaveTypeEntity validateLeaveType(String messageText) {
        try {
            LeaveTypeEnum validLeaveType = LeaveTypeEnum.fromId(Integer.parseInt(messageText));
            if (validLeaveType == null) {
                return null;
            }
            return leaveTypeRepository.findById((long) validLeaveType.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Leave type not found"));
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
            return ConversationStateEnum.CANCEL;
        }
        return null;
    }

    public UserChoiceEnum validateChoice(String messageText) {
        String cleanedMessage = messageText.strip().toLowerCase();
        try {
            return UserChoiceEnum.fromId(Integer.parseInt(cleanedMessage));
        } catch (Exception e) {
            return null;
        }
    }
}
