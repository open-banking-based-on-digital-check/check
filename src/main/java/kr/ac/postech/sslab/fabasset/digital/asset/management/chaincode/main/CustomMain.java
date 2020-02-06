package kr.ac.postech.sslab.fabasset.digital.asset.management.chaincode.main;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.ac.postech.sslab.fabasset.digital.asset.management.chaincode.extension.EERC721;
import kr.ac.postech.sslab.fabasset.digital.asset.management.chaincode.extension.XNFT;
import kr.ac.postech.sslab.fabasset.digital.asset.management.chaincode.extension.XType;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ResponseUtils;

import java.io.IOException;
import java.util.*;

import static kr.ac.postech.sslab.fabasset.digital.asset.management.chaincode.constant.Function.*;
import static kr.ac.postech.sslab.fabasset.digital.asset.management.chaincode.constant.Message.ARG_MESSAGE;
import static io.netty.util.internal.StringUtil.isNullOrEmpty;

public class CustomMain extends Main {
	private static ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Response invoke(ChaincodeStub stub) {
        try {
            String func = stub.getFunction();
            List<String> args = stub.getParameters();
            String response;

            switch (func) {
                case BALANCE_OF_FUNCTION_NAME:
                    if (args.size() == 1) {
                        return super.invoke(stub);
                    }
                    response = xBalanceOf(stub, args);
                    break;

                case TOKEN_IDS_OF_FUNCTION_NAME:
                    response = tokenIdsOf(stub, args);
                    break;

                case DIVIDE_FUNCTION_NAME:
                    response = divide(stub, args);
                    break;

                case DEACTIVATE_FUNCTION_NAME:
                    response = deactivate(stub, args);
                    break;

                case QUERY_FUNCTION_NAME:
                    response = query(stub, args);
                    break;

                case QUERY_HISTORY_FUNCTION_NAME:
                    response = queryHistory(stub, args);
                    break;

                case MINT_FUNCTION_NAME:
                    if (args.size() == 2) {
                        return super.invoke(stub);
                    }
                    response = xMint(stub, args);
                    break;

                case SET_URI_FUNCTION_NAME:
                    response = setURI(stub, args);
                    break;

                case GET_URI_FUNCTION_NAME:
                    response = getURI(stub, args);
                    break;

                case SET_XATTR_FUNCTION_NAME:
                    response = setXAttr(stub, args);
                    break;

                case GET_XATTR_FUNCTION_NAME:
                    response = getXAttr(stub, args);
                    break;

                case ENROLL_TOKEN_TYPE_FUNCTION_NAME:
                    response = enrollTokenType(stub, args);
                    break;

                case DROP_TOKEN_TYPE_FUNCTION_NAME:
                    response = dropTokenType(stub, args);
                    break;

                case TOKEN_TYPES_OF_FUNCTION_NAME:
                    response = tokenTypesOf(stub);
                    break;

                case UPDATE_TOKEN_TYPE_FUNCTION_NAME:
                    response = updateTokenType(stub, args);
                    break;

                case RETRIEVE_TOKEN_TYPE_FUNCTION_NAME:
                    response = retrieveTokenType(stub, args);
                    break;

                case EMROLL_ATTRIBUTE_OF_TOKEN_TYPE_FUNCTION_NAME:
                    response = enrollAttributeOfTokenType(stub, args);
                    break;

                case DROP_ATTRIBUTE_OF_TOKEN_TYPE_FUNCTION_NAME:
                    response = dropAttributeOfTokenType(stub, args);
                    break;

                case UPDATE_ATTRIBUTE_OF_TOKEN_TYPE_FUNCTION_NAME:
                    response = updateAttributeOfTokenType(stub, args);
                    break;

                case RETRIEVE_ATTRIBUTE_OF_TOKEN_TYPE_FUNCTION_NAME:
                    response = retrieveAttributeOfTokenType(stub, args);
                    break;

                default:
                    return super.invoke(stub);
            }

            return ResponseUtils.newSuccessResponse(response);
        } catch (Exception e) {
            return ResponseUtils.newErrorResponse(e.getMessage());
        }
    }

    private String xBalanceOf(ChaincodeStub stub, List<String> args) throws IOException {
        if (args.size() != 2 || isNullOrEmpty(args.get(0))
                || isNullOrEmpty(args.get(1))) {
            throw new IllegalArgumentException(String.format(ARG_MESSAGE + "1 or 2"));
        }

        String owner = args.get(0);
        String type = args.get(1);

        return Long.toString(EERC721.balanceOf(stub, owner, type));
    }

    private String tokenIdsOf(ChaincodeStub stub, List<String> args) throws IOException {
        List<String> tokenIds;

        if (args.size() == 1) {
            if (isNullOrEmpty(args.get(0))) {
                throw new IllegalArgumentException(String.format(ARG_MESSAGE, "1"));
            }

            String owner = args.get(0);
            tokenIds = EERC721.tokenIdsOf(stub, owner);
        }
        else if (args.size() == 2) {
            if (isNullOrEmpty(args.get(0)) || isNullOrEmpty(args.get(1))) {
                throw new IllegalArgumentException(String.format(ARG_MESSAGE, "2"));
            }

            String owner = args.get(0);
            String type = args.get(1);
            tokenIds = EERC721.tokenIdsOf(stub, owner, type);
        }
        else {
            throw new IllegalArgumentException(String.format(ARG_MESSAGE + "1 or 2"));
        }

        return tokenIds.toString();
    }

    private String divide(ChaincodeStub stub, List<String> args) throws IOException {
        if (args.size() != 4
                || isNullOrEmpty(args.get(0))
                || isNullOrEmpty(args.get(1))
                || isNullOrEmpty(args.get(2))
                || isNullOrEmpty(args.get(3))) {
            throw new IllegalArgumentException(String.format(ARG_MESSAGE, "4"));
        }

        String tokenId = args.get(0);
        List<String> newIds = strToList(args.get(1));
        List<String> values = strToList(args.get(2));
        String index = args.get(3);

        return Boolean.toString(EERC721.divide(stub, tokenId, newIds, values, index));
    }

    private String deactivate(ChaincodeStub stub, List<String> args) throws IOException {
        if (args.size() != 1 || isNullOrEmpty(args.get(0))) {
            throw new IllegalArgumentException(String.format(ARG_MESSAGE, "1"));
        }

        String tokenId = args.get(0);

        return Boolean.toString(EERC721.deactivate(stub, tokenId));
    }

    private String query(ChaincodeStub stub, List<String> args) throws IOException {
        if (args.size() != 1 || isNullOrEmpty(args.get(0))) {
            throw new IllegalArgumentException(String.format(ARG_MESSAGE, "1"));
        }

        String tokenId = args.get(0);

        return EERC721.query(stub, tokenId);
    }

    private String queryHistory(ChaincodeStub stub, List<String> args) {
        if (args.size() != 1 || isNullOrEmpty(args.get(0))) {
            throw new IllegalArgumentException(String.format(ARG_MESSAGE, "1"));
        }

        String tokenId = args.get(0);

        return EERC721.queryHistory(stub, tokenId).toString();
    }

    private String xMint(ChaincodeStub stub, List<String> args) throws IOException {
        if (args.size() != 5 || isNullOrEmpty(args.get(0))
                || isNullOrEmpty(args.get(1)) || isNullOrEmpty(args.get(2))
                || isNullOrEmpty(args.get(3)) || isNullOrEmpty(args.get(4))) {
            throw new IllegalArgumentException(String.format(ARG_MESSAGE, "5"));
        }

        String tokenId = args.get(0);
        String type = args.get(1);
        String owner = args.get(2);
        Map<String, Object> xattr =
                objectMapper.readValue(args.get(3), new TypeReference<HashMap<String, Object>>(){});
        Map<String, String> uri =
                objectMapper.readValue(args.get(4), new TypeReference<HashMap<String, String>>(){});

        return Boolean.toString(XNFT.mint(stub, tokenId, type, owner, xattr, uri));
    }

    private String setURI(ChaincodeStub stub, List<String> args) throws IOException {
        if (args.size() != 3 || isNullOrEmpty(args.get(0))
                || isNullOrEmpty(args.get(1)) || isNullOrEmpty(args.get(2))) {
            throw new IllegalArgumentException(String.format(ARG_MESSAGE, "3"));
        }

        String tokenId = args.get(0);
        String index = args.get(1);
        String value = args.get(2);

        return Boolean.toString(XNFT.setURI(stub, tokenId, index, value));
    }

    private String getURI(ChaincodeStub stub, List<String> args) throws IOException {
        if (args.size() != 2 || isNullOrEmpty(args.get(0))
                || isNullOrEmpty(args.get(1))) {
            throw new IllegalArgumentException(String.format(ARG_MESSAGE, "2"));
        }
        String tokenId = args.get(0);
        String index = args.get(1);
        return XNFT.getURI(stub, tokenId, index);
    }

    private String setXAttr(ChaincodeStub stub, List<String> args) throws IOException {
        if (args.size() != 3 || isNullOrEmpty(args.get(0))
                || isNullOrEmpty(args.get(1)) || isNullOrEmpty(args.get(2))) {
            throw new IllegalArgumentException(String.format(ARG_MESSAGE, "3"));
        }

        String tokenId = args.get(0);
        String index = args.get(1);
        String value = args.get(2);

        return Boolean.toString(XNFT.setXAttr(stub, tokenId, index, value));
    }

    private String getXAttr(ChaincodeStub stub, List<String> args)  throws IOException {
        if (args.size() != 2 || isNullOrEmpty(args.get(0))
                || isNullOrEmpty(args.get(1))) {
            throw new IllegalArgumentException(String.format(ARG_MESSAGE, "2"));
        }

        String tokenId = args.get(0);
        String index = args.get(1);

        return XNFT.getXAttr(stub, tokenId, index);
    }

    private String enrollTokenType(ChaincodeStub stub, List<String> args) throws IOException {
        if (args.size() != 3
                || isNullOrEmpty(args.get(0)) || isNullOrEmpty(args.get(1)) || isNullOrEmpty(args.get(2))) {
            throw new IllegalArgumentException(String.format(ARG_MESSAGE, "3"));
        }

        String admin = args.get(0);
        String type = args.get(1);
        String json = args.get(2);

        return Boolean.toString(XType.enrollTokenType(stub, admin, type, json));
    }

    private String dropTokenType(ChaincodeStub stub, List<String> args) throws IOException {
        if (args.size() != 2
                || isNullOrEmpty(args.get(0)) || isNullOrEmpty(args.get(1))) {
            throw new IllegalArgumentException(String.format(ARG_MESSAGE, "2"));
        }

        String admin = args.get(0);
        String type = args.get(1);

        return Boolean.toString(XType.dropTokenType(stub, admin, type));
    }

    private String tokenTypesOf(ChaincodeStub stub) throws IOException {
        List<String> tokenTypes = XType.tokenTypesOf(stub);
        return tokenTypes.toString();
    }

    private static String updateTokenType(ChaincodeStub stub, List<String> args) throws IOException {
        if (args.size() != 3
                || isNullOrEmpty(args.get(0)) || isNullOrEmpty(args.get(1)) || isNullOrEmpty(args.get(2))) {
            throw new IllegalArgumentException(String.format(ARG_MESSAGE, "3"));
        }

        String admin = args.get(0);
        String type = args.get(1);
        String attributesStr = args.get(2);
        Map<String, List<String>> attributes
                = objectMapper.readValue(attributesStr, new TypeReference<HashMap<String, List<String>>>() {});

        return Boolean.toString(XType.updateTokenType(stub, admin, type, attributes));
    }

    private String retrieveTokenType(ChaincodeStub stub, List<String> args) throws IOException {
        if (args.size() != 1 || isNullOrEmpty(args.get(0))) {
            throw new IllegalArgumentException(String.format(ARG_MESSAGE, "1"));
        }

        String type = args.get(0);
        Map<String, List<String>> map = XType.retrieveTokenType(stub, type);

        return objectMapper.writeValueAsString(map);
    }

    private String enrollAttributeOfTokenType(ChaincodeStub stub, List<String> args) throws IOException {
        if (args.size() != 5
                || isNullOrEmpty(args.get(0)) || isNullOrEmpty(args.get(1)) || isNullOrEmpty(args.get(2))
                || isNullOrEmpty(args.get(3)) || isNullOrEmpty(args.get(4))) {
            throw new IllegalArgumentException(String.format(ARG_MESSAGE, "5"));
        }

        String admin = args.get(0);
        String tokenType = args.get(1);
        String attribute = args.get(2);
        String dataType = args.get(3);
        String initialValue = args.get(4);

        return Boolean.toString(XType.enrollAttributeOfTokenType(stub, admin, tokenType, attribute, dataType, initialValue));
    }

    private String dropAttributeOfTokenType(ChaincodeStub stub, List<String> args) throws IOException {
        if (args.size() != 3
                || isNullOrEmpty(args.get(0)) || isNullOrEmpty(args.get(1)) || isNullOrEmpty(args.get(2))) {
            throw new IllegalArgumentException(String.format(ARG_MESSAGE, "3"));
        }

        String admin = args.get(0);
        String tokenType = args.get(1);
        String attribute = args.get(2);

        return Boolean.toString(XType.dropAttributeOfTokenType(stub, admin, tokenType, attribute));
    }

    private String updateAttributeOfTokenType(ChaincodeStub stub, List<String> args) throws IOException {
        if (args.size() != 4
                || isNullOrEmpty(args.get(0)) || isNullOrEmpty(args.get(1)) || isNullOrEmpty(args.get(2))
                || isNullOrEmpty(args.get(3))) {
            throw new IllegalArgumentException(String.format(ARG_MESSAGE, "4"));
        }

        String admin = args.get(0);
        String tokenType = args.get(1);
        String attribute = args.get(2);
        String pairStr = args.get(3);
        List<String> pair = strToList(pairStr);

        return Boolean.toString(XType.updateAttributeOfTokenType(stub, admin, tokenType, attribute, pair));
    }

    private String retrieveAttributeOfTokenType(ChaincodeStub stub, List<String> args) throws IOException {
        if (args.size() != 2 || isNullOrEmpty(args.get(0)) || isNullOrEmpty(args.get(1))) {
            throw new IllegalArgumentException(String.format(ARG_MESSAGE, "2"));
        }

        String tokenType = args.get(0);
        String attribute = args.get(1);

        return XType.retrieveAttributeOfTokenType(stub, tokenType, attribute).toString();
    }

    private List<String> strToList(String str) {
        return Arrays.asList(str.substring(1, str.length() - 1).split(", "));
    }

    public static void main(String[] args) {
        new CustomMain().start(args);
    }
}
