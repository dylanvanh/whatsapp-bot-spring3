package co.za.entelect.enums;

public enum UserChoiceEnum {
    MAKE_LEAVE_REQUEST(1),
    VIEW_LEAVE_REQUESTS(2);

    private final int id;

    UserChoiceEnum(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static UserChoiceEnum fromId(int id) {
        for (UserChoiceEnum state : UserChoiceEnum.values()) {
            if (state.getId() == id) {
                return state;
            }
        }
        throw new IllegalArgumentException("Invalid ID: " + id);
    }
}
