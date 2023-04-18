package co.za.entelect.Enums;

public enum LeaveTypeEnum {
    ANNUAL(1),
    SICK(2),
    FAMILY_RESPONSIBILITY(3),
    BIRTHDAY(4),
    STUDY(5),
    PARENTAL(6),
    MATERNAL(7);

    private final int id;

    LeaveTypeEnum(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static LeaveTypeEnum fromId(int id) {
        for (LeaveTypeEnum leaveType : LeaveTypeEnum.values()) {
            if (leaveType.getId() == id) {
                return leaveType;
            }
        }
        return null;
    }
}
