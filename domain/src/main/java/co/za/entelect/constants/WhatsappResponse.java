package co.za.entelect.constants;

public class WhatsappResponse {

    public static final String GREETING = """
            Welcome to Entelect Leave Bot.\s
            Please enter your employee email address to continue.\s
            """;

    public static final String START_DATE_INITIAL = """
            Please provide your leave start date in the format \s
            \s
            dd/mm/yyyy\s
            \s
            Cancel - CANCEL
            """;

    public static final String START_DATE_INVALID_FORMAT = """
            Invalid start date. Please try again in format dd/mm/yyyy:\s
            \s
            Cancel - CANCEL\s
            """;

    public static final String START_DATE_IN_PAST = """
            Start date cannot be in the past. Please try again:\s
            \s
            Cancel - CANCEL\s
            """;

    public static final String END_DATE_INITIAL = """
            Please provide your leave end date in the format dd/mm/yyyy \s
            \s
            Cancel - CANCEL
            """;

    public static final String END_DATE_INVALID_FORMAT = """
            Invalid end date. Please try again in format dd/mm/yyyy:\s
            \s
            CANCEL - CANCEL
            """;

    public static final String END_DATE_BEFORE_START_DATE = """
            End date cannot be before or the same as the start date. Please try again:\s
            \s
            CANCEL - CANCEL
            """;

    public static final String INVALID_EMAIL = """
            Please select an option:\s
            \s
            1 - Request Leave\s
            \s
            2 - View Requested Leave\s
            \s
            CANCEL- CANCEL
            """;
    public static final String CHOICE_INITIAL = """
            Please select an option:\s
            \s
            1 - Request Leave\s
            \s
            2 - View Requested Leave\s
            \s
            CANCEL - CANCEL\s
            """;

    public static final String CHOICE_INVALID =
            """
                    Invalid choice. Please try again:\s
                    \s
                    1 - Request Leave\s
                    \s
                    2 - View Requested Leave\s
                    \s
                    Cancel - CANCEL\s
                    """;

    public static final String LEAVE_TYPE_INITIAL = """
            Please choose the leave type:\s
            ANNUAL - 1\s
            SICK - 2\s
            FAMILY RESPONSIBILITY - 3\s
            BIRTHDAY - 4\s
            STUDY - 5\s
            PARENTAL - 6\s
            MATERNAL - 7\s
            \s
            CANCEL - CANCEL\s
            """;

    public static final String LEAVE_TYPE_INVALID = """
            Invalid leave type provided. Please try again:\s
            ANNUAL - 1\s
            SICK - 2\s
            FAMILY RESPONSIBILITY - 3\s
            BIRTHDAY - 4\s
            STUDY - 5\s
            PARENTAL - 6\s
            MATERNAL - 7\s
            \s
            CANCEL - CANCEL\s
            """;

    public static final String CONFIRMATION_INITIAL = """
            Please confirm your leave request by typing :\s
            CONFIRM - confirm\s
            \s
            CANCEL - cancel\s
            """;

    public static final String CONFIRMATION_VALID = """
            Your leave request has been submitted:\s
            \s
            Please select an option:\s
            \s
            1 - Request Leave\s
            \s
            2 - View Requested Leave\s
            """;

    public static final String CONFIRMATION_INVALID = """
            Invalid confirmation provided.\s
            \s
            Confirm - CONFIRM\s
            \s
            Cancel - CANCEL\s
            """;

    public static final String CANCEL = """
            Your request has been cancelled.\s
            \s
            Please select an option:\s
            \s
            1 - Request Leave\s
            \s
            2 - View Requested Leave\s
            \s
            Cancel - CANCEL\s
            """;

    public static final String COMMENT_INITIAL = """
            Please provide a comment for your leave request:\s
            \s
            Cancel - CANCEL\s
            """;

    public static final String COMMENT_INVALID = """
            Invalid comment provided , maximum 500 characters allowed.
            Please try again:\s
            \s
            Cancel - CANCEL\s
            """;
}
