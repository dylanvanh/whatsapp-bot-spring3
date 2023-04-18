package co.za.entelect.services;

import co.za.entelect.Enums.LeaveTypeEnum;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class IncomingMessageValidator {

    public boolean validateEmployeeNumber(String employeeNumber) {
        return employeeNumber.length() == 2;
    }

    public Date validateDate(String date) {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        try {
            return dateFormat.parse(date);
        } catch (ParseException e) {
            return null;
        }
    }


    public boolean validateLeaveType(String messageText) {
        try {
            LeaveTypeEnum validLeaveType = LeaveTypeEnum.fromId(Integer.parseInt(messageText));
            if (validLeaveType == null) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
