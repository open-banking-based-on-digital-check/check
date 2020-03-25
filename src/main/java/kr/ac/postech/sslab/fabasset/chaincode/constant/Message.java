package kr.ac.postech.sslab.fabasset.chaincode.constant;

public final class Message {
    private Message() {}

    public static final String INIT_FUNCTION_MESSAGE = "Function other than init is not supported";

    public static final String ARG_MESSAGE = "The argument(s) must be exactly %s non-empty string(s)";

    public static final String NO_FUNCTION_MESSAGE = "There is no such function";
}
