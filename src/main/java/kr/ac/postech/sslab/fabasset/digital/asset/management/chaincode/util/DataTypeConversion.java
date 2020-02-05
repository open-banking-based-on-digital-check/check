package kr.ac.postech.sslab.fabasset.digital.asset.management.chaincode.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static kr.ac.postech.sslab.fabasset.digital.asset.management.chaincode.constant.DataType.*;
import static kr.ac.postech.sslab.fabasset.digital.asset.management.chaincode.constant.DataType.LIST_BOOLEAN;
import static kr.ac.postech.sslab.fabasset.digital.asset.management.chaincode.constant.Message.NO_DATA_TYPE_MESSAGE;

public class DataTypeConversion {
    private static final Log LOG = LogFactory.getLog(DataTypeConversion.class);

    private static final String EMPTY_LIST = "[]";

    private DataTypeConversion() {}

    public static Object strToDataType(String dataType, String value) {
        switch (dataType) {
            case INTEGER:
                return Integer.parseInt(value);

            case DOUBLE:
                return Double.parseDouble(value);

            case BYTE:
                return Byte.parseByte(value);

            case STRING:
                return value;

            case BOOLEAN:
                return Boolean.parseBoolean(value);

            case LIST_INTEGER:
                return toListInteger(value);

            case LIST_DOUBLE:
                return toListDouble(value);

            case LIST_BYTE:
                return toListByte(value);

            case LIST_STRING:
                return toListString(value);

            case LIST_BOOLEAN:
                return toListBoolean(value);

            default:
                LOG.error(NO_DATA_TYPE_MESSAGE);
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
