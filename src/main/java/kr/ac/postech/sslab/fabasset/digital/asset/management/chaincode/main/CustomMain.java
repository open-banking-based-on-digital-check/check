package kr.ac.postech.sslab.fabasset.digital.asset.management.chaincode.main;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.ac.postech.sslab.fabasset.digital.asset.management.chaincode.extension.Extension;
import kr.ac.postech.sslab.fabasset.digital.asset.management.chaincode.extension.TokenTypeManagement;
import kr.ac.postech.sslab.fabasset.digital.asset.management.chaincode.standard.Default;
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

                case RETRIEVE_TOKEN_TYPE_FUNCTION_NAME:
                    response = retrieveTokenType(stub, args);
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

        return Long.toString(Extension.balanceOf(stub, owner, type));
    }

    private String tokenIdsOf(ChaincodeStub stub, List<String> args) throws IOException {
        List<String> tokenIds;

        if (args.size() == 1) {
            if (isNullOrEmpty(args.get(0))) {
                throw new IllegalArgumentException(String.format(ARG_MESSAGE, "1"));
            }

            String owner = args.get(0);
            tokenIds = Default.tokenIdsOf(stub, owner);
        }
        else if (args.size() == 2) {
            if (isNullOrEmpty(args.get(0)) || isNullOrEmpty(args.get(1))) {
                throw new IllegalArgumentException(String.format(ARG_MESSAGE, "2"));
            }

            String owner = args.get(0);
            String type = args.get(1);
            tokenIds = Extension.tokenIdsOf(stub, owner, type);
        }
        else {
            throw new IllegalArgumentException(String.format(ARG_MESSAGE + "1 or 2"));
        }

        return tokenIds.toString();
    }

    private String query(ChaincodeStub stub, List<String> args) throws IOException {
        if (args.size() != 1 || isNullOrEmpty(args.get(0))) {
            throw new IllegalArgumentException(String.format(ARG_MESSAGE, "1"));
        }

        String tokenId = args.get(0);

        return Extension.query(stub, tokenId);
    }

    private String queryHistory(ChaincodeStub stub, List<String> args) {
        if (args.size() != 1 || isNullOrEmpty(args.get(0))) {
            throw new IllegalArgumentException(String.format(ARG_MESSAGE, "1"));
        }

        String tokenId = args.get(0);

        return Extension.queryHistory(stub, tokenId).toString();
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

        return Boolean.toString(Extension.mint(stub, tokenId, type, owner, xattr, uri));
    }

    private String setURI(ChaincodeStub stub, List<String> args) throws IOException {
        if (args.size() != 3 || isNullOrEmpty(args.get(0))
                || isNullOrEmpty(args.get(1)) || isNullOrEmpty(args.get(2))) {
            throw new IllegalArgumentException(String.format(ARG_MESSAGE, "3"));
        }

        String tokenId = args.get(0);
        String index = args.get(1);
        String value = args.get(2);

        return Boolean.toString(Extension.setURI(stub, tokenId, index, value));
    }

    private String getURI(ChaincodeStub stub, List<String> args) throws IOException {
        if (args.size() != 2 || isNullOrEmpty(args.get(0))
                || isNullOrEmpty(args.get(1))) {
            throw new IllegalArgumentException(String.format(ARG_MESSAGE, "2"));
        }
        String tokenId = args.get(0);
        String index = args.get(1);
        return Extension.getURI(stub, tokenId, index);
    }

    private String setXAttr(ChaincodeStub stub, List<String> args) throws IOException {
        if (args.size() != 3 || isNullOrEmpty(args.get(0))
                || isNullOrEmpty(args.get(1)) || isNullOrEmpty(args.get(2))) {
            throw new IllegalArgumentException(String.format(ARG_MESSAGE, "3"));
        }

        String tokenId = args.get(0);
        String index = args.get(1);
        String value = args.get(2);

        return Boolean.toString(Extension.setXAttr(stub, tokenId, index, value));
    }

    private String getXAttr(ChaincodeStub stub, List<String> args)  throws IOException {
        if (args.size() != 2 || isNullOrEmpty(args.get(0))
                || isNullOrEmpty(args.get(1))) {
            throw new IllegalArgumentException(String.format(ARG_MESSAGE, "2"));
        }

        String tokenId = args.get(0);
        String index = args.get(1);

        return Extension.getXAttr(stub, tokenId, index);
    }

    private String enrollTokenType(ChaincodeStub stub, List<String> args) throws IOException {
        if (args.size() != 3
                || isNullOrEmpty(args.get(0)) || isNullOrEmpty(args.get(1)) || isNullOrEmpty(args.get(2))) {
            throw new IllegalArgumentException(String.format(ARG_MESSAGE, "3"));
        }

        String admin = args.get(0);
        String type = args.get(1);
        String json = args.get(2);

        return Boolean.toString(TokenTypeManagement.enrollTokenType(stub, admin, type, json));
    }

    private String dropTokenType(ChaincodeStub stub, List<String> args) throws IOException {
        if (args.size() != 2
                || isNullOrEmpty(args.get(0)) || isNullOrEmpty(args.get(1))) {
            throw new IllegalArgumentException(String.format(ARG_MESSAGE, "2"));
        }

        String admin = args.get(0);
        String type = args.get(1);

        return Boolean.toString(TokenTypeManagement.dropTokenType(stub, admin, type));
    }

    private String tokenTypesOf(ChaincodeStub stub) throws IOException {
        List<String> tokenTypes = TokenTypeManagement.tokenTypesOf(stub);
        return tokenTypes.toString();
    }

    private String retrieveTokenType(ChaincodeStub stub, List<String> args) throws IOException {
        if (args.size() != 1 || isNullOrEmpty(args.get(0))) {
            throw new IllegalArgumentException(String.format(ARG_MESSAGE, "1"));
        }

        String type = args.get(0);
        Map<String, List<String>> map = TokenTypeManagement.retrieveTokenType(stub, type);

        return objectMapper.writeValueAsString(map);
    }

    private String retrieveAttributeOfTokenType(ChaincodeStub stub, List<String> args) throws IOException {
        if (args.size() != 2 || isNullOrEmpty(args.get(0)) || isNullOrEmpty(args.get(1))) {
            throw new IllegalArgumentException(String.format(ARG_MESSAGE, "2"));
        }

        String tokenType = args.get(0);
        String attribute = args.get(1);

        return TokenTypeManagement.retrieveAttributeOfTokenType(stub, tokenType, attribute).toString();
    }

    private List<String> strToList(String str) {
        return Arrays.asList(str.substring(1, str.length() - 1).split(", "));
    }

    public static void main(String[] args) {
        new CustomMain().start(args);
    }
}
