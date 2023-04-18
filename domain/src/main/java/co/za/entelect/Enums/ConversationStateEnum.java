package co.za.entelect.Enums;

public enum ConversationStateEnum {
    GREETING(1),
    EMPLOYEE_NUMBER(2),
    START_DATE(3),
    END_DATE(4),
    LEAVE_TYPE(5),
    CONFIRMATION(6),
    END(7);

    private final int id;

    ConversationStateEnum(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static ConversationStateEnum fromId(int id) {
        for (ConversationStateEnum state : ConversationStateEnum.values()) {
            if (state.getId() == id) {
                return state;
            }
        }
        throw new IllegalArgumentException("Invalid ID: " + id);
    }
}
