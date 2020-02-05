package kr.ac.postech.sslab.fabasset.digital.asset.management.chaincode.constant;

public final class Message {
    private Message() {}

    public static final String INIT_FUNCTION_MESSAGE = "Function other than init is not supported";

    public static final String ARG_MESSAGE = "The argument(s) must be exactly %s non-empty string(s)";

    public static final String NO_FUNCTION_MESSAGE = "There is no such function";

    public static final String NO_DATA_TYPE_MESSAGE = "There is no such data type";

    public static final String DEACTIVATED_MESSAGE = "This token is deactivated";

    public static final String BASE_TYPE_ERROR_MESSAGE = "This function is not supported for 'base' token type";

    public static final String NO_TOKEN_TYPE_MESSAGE = "There is no such token type";

    public static final String NO_ATTRIBUTE_MESSAGE = "There is no such attribute";
}
