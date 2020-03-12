package kr.ac.postech.sslab.fabasset.chaincode.util;

import kr.ac.postech.sslab.fabasset.chaincode.constant.DataType;
import kr.ac.postech.sslab.fabasset.chaincode.constant.Message;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataTypeConversion {
    private static final Log LOG = LogFactory.getLog(DataTypeConversion.class);

    private static final String EMPTY_LIST = "[]";

    private DataTypeConversion() {}

    public static Object strToDataType(String dataType, String value) {
        switch (dataType) {
            case DataType.INTEGER:
                return Integer.parseInt(value);

            case DataType.DOUBLE:
                return Double.parseDouble(value);

            case DataType.BYTE:
                return Byte.parseByte(value);

            case DataType.STRING:
                return value;

            case DataType.BOOLEAN:
                return Boolean.parseBoolean(value);

            case DataType.LIST_INTEGER:
                return toListInteger(value);

            case DataType.LIST_DOUBLE:
                return toListDouble(value);

            case DataType.LIST_BYTE:
                return toListByte(value);

            case DataType.LIST_STRING:
                return toListString(value);

            case DataType.LIST_BOOLEAN:
                return toListBoolean(value);

            default:
                LOG.error(Message.NO_DATA_TYPE_MESSAGE);
                return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static String dataTypeToStr(String dataType, Object value) {
        switch (dataType) {
            case DataType.INTEGER:
                return Integer.toString((int) value);

            case DataType.DOUBLE:
                return Double.toString((double) value);

            case DataType.BYTE:
                return Byte.toString((byte) value);

            case DataType.STRING:
                return (String) value;

            case DataType.BOOLEAN:
                return Boolean.toString((boolean) value);

            case DataType.LIST_INTEGER:
                List<Integer> integers = (List<Integer>) value;
                return integers != null ? integers.toString() : null;

            case DataType.LIST_DOUBLE:
                List<Double> doubles = (List<Double>) value;
                return doubles != null ? doubles.toString() : null;

            case DataType.LIST_BYTE:
                List<Byte> bytes = (List<Byte>) value;
                return bytes != null ? bytes.toString() : null;

            case DataType.LIST_STRING:
                List<String> strings = (List<String>) value;
                return strings != null ? strings.toString() : null;

            case DataType.LIST_BOOLEAN:
                List<Boolean> booleans = (List<Boolean>) value;
                return booleans != null ? booleans.toString() : null;

            default:
                return null;
        }
    }

    private static List<Integer> toListInteger(String value) {
        List<Integer> integers = new ArrayList<>();
        if (value == null || value.equals(EMPTY_LIST)) {
            return integers;
        }

        List<String> strings = toList(value);
        for (String string : strings) {
            integers.add(Integer.parseInt(string));
        }

        return integers;
    }

    private static List<Double> toListDouble(String value) {
        List<Double> doubles = new ArrayList<>();
        if (value == null || value.equals(EMPTY_LIST)) {
            return doubles;
        }

        List<String> strings = toList(value);
        for (String string : strings) {
            doubles.add(Double.parseDouble(string));
        }

        return doubles;
    }

    private static List<Byte> toListByte(String value) {
        List<Byte> bytes = new ArrayList<>();
        if (value == null || value.equals(EMPTY_LIST)) {
            return bytes;
        }

        List<String> strings = toList(value);
        for (String string : strings) {
            bytes.add(Byte.parseByte(string));
        }

        return bytes;
    }

    private static List<String> toListString(String value) {
        List<String> strings = new ArrayList<>();
        if (value == null || value.equals(EMPTY_LIST)) {
            return strings;
        }

        strings = toList(value);
        return strings;
    }

    private static List<Boolean> toListBoolean(String value) {
        List<Boolean> booleans = new ArrayList<>();
        if (value == null || value.equals(EMPTY_LIST)) {
            return booleans;
        }

        List<String> strings = toList(value);
        for (String string : strings) {
            booleans.add(Boolean.parseBoolean(string));
        }

        return booleans;
    }

    private static List<String> toList(String value) {
        return Arrays.asList(value.substring(1, value.length() - 1).split(", "));
    }
}
