package kr.ac.postech.sslab.fabasset.chaincode;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.ac.postech.sslab.fabasset.chaincode.manager.OperatorManager;
import kr.ac.postech.sslab.fabasset.chaincode.manager.TokenTypeManager;
import kr.ac.postech.sslab.fabasset.chaincode.protocol.Extension;
import kr.ac.postech.sslab.fabasset.chaincode.protocol.TokenTypeManagement;
import kr.ac.postech.sslab.fabasset.chaincode.protocol.Default;
import kr.ac.postech.sslab.fabasset.chaincode.protocol.ERC721;
import kr.ac.postech.sslab.fabasset.chaincode.util.DataTypeConversion;
import org.hyperledger.fabric.shim.ChaincodeBase;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ResponseUtils;

import static kr.ac.postech.sslab.fabasset.chaincode.constant.Function.*;
import static io.netty.util.internal.StringUtil.isNullOrEmpty;
import static kr.ac.postech.sslab.fabasset.chaincode.constant.Message.*;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.*;


public class Main extends ChaincodeBase {
    private static ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Response init(ChaincodeStub stub) {

        try {
            String func = stub.getFunction();

            if (!func.equals(INIT_FUNCTION_NAME)) {
                return ResponseUtils.newErrorResponse(INIT_FUNCTION_MESSAGE);
            }

            List<String> args = stub.getParameters();
            if (!args.isEmpty()) {
                throw new InvalidParameterException(String.format(ARG_MESSAGE, "0"));
            }

            OperatorManager operatorManager = OperatorManager.load(stub);
            Map<String, Map<String, Boolean>> operatorManagerTable = operatorManager.getTable();
            if (operatorManagerTable.isEmpty()) {
                operatorManager.store(stub);
            }

            TokenTypeManager tokenTypeManager = TokenTypeManager.load(stub);
            Map<String, Map<String, List<String>>> tokenTypeManagerTable = tokenTypeManager.getTable();
            if (tokenTypeManagerTable.isEmpty()) {
                tokenTypeManager.store(stub);
            }

            return ResponseUtils.newSuccessResponse();
        } catch (Exception e) {
            return ResponseUtils.newErrorResponse(e.getMessage());
        }
    }

    @Override
    public Response invoke(ChaincodeStub stub) {
        try {
            String func = stub.getFunction();
            List<String> args = stub.getParameters();
            String response;

            switch (func) {
                case BALANCE_OF_FUNCTION_NAME:
                    response = balanceOf(stub, args);
                    break;

                case OWNER_OF_FUNCTION_NAME:
                    response = ownerOf(stub, args);
                    break;

                case TRANSFER_FROM_FUNCTION_NAME:
                    response = transferFrom(stub, args);
                    break;

                case APPROVE_FUNCTION_NAME:
                    response = approve(stub, args);
                    break;

                case SET_APPROVAL_FOR_ALL_FUNCTION_NAME:
                    response = setApprovalForAll(stub, args);
                    break;

                case GET_APPROVED_FUNCTION_NAME:
                    response = getApproved(stub, args);
                    break;

                case IS_APPROVED_FOR_ALL_FUNCTION_NAME:
                    response = isApprovedForAll(stub, args);
                    break;

                case MINT_FUNCTION_NAME:
                    response = mint(stub, args);
                    break;

                case BURN_FUNCTION_NAME:
                    response = burn(stub, args);
                    break;

                case GET_TYPE_FUNCTION_NAME:
                    response = getType(stub, args);
                    break;

                case TOKEN_IDS_OF_FUNCTION_NAME:
                    response = tokenIdsOf(stub, args);
                    break;

                case QUERY_FUNCTION_NAME:
                    response = query(stub, args);
                    break;

                case HISTORY_FUNCTION_NAME:
                    response = history(stub, args);
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

                case TOKEN_TYPES_OF_FUNCTION_NAME:
                    response = tokenTypesOf(stub);
                    break;

                case ENROLL_TOKEN_TYPE_FUNCTION_NAME:
                    response = enrollTokenType(stub, args);
                    break;

                case DROP_TOKEN_TYPE_FUNCTION_NAME:
                    response = dropTokenType(stub, args);
                    break;

                case RETRIEVE_TOKEN_TYPE_FUNCTION_NAME:
                    response = retrieveTokenType(stub, args);
                    break;

                case RETRIEVE_ATTRIBUTE_OF_TOKEN_TYPE_FUNCTION_NAME:
                    response = retrieveAttributeOfTokenType(stub, args);
                    break;

                default:
                    return ResponseUtils.newErrorResponse(NO_FUNCTION_MESSAGE);
            }

            return ResponseUtils.newSuccessResponse(response);
        } catch (Exception e) {
            return ResponseUtils.newErrorResponse(e.getMessage());
        }
    }

    private String balanceOf(ChaincodeStub stub, List<String> args) {
        if (args.size() == 1) {
            if(isNullOrEmpty(args.get(0))) {
                throw new IllegalArgumentException(String.format(ARG_MESSAGE, "1"));
            }

            String owner = args.get(0);

            return Long.toString(ERC721.balanceOf(stub, owner));
        } else if (args.size() == 2) {
            if (isNullOrEmpty(args.get(0)) || isNullOrEmpty(args.get(1))) {
                throw new IllegalArgumentException(String.format(ARG_MESSAGE, "2"));
            }

            String owner = args.get(0);
            String type = args.get(1);

            return Long.toString(Extension.balanceOf(stub, owner, type));
        }

        throw new IllegalArgumentException(String.format(ARG_MESSAGE, "1 or 2"));
    }

    private String ownerOf(ChaincodeStub stub, List<String> args) throws IOException {
        if (args.size() != 1 || isNullOrEmpty(args.get(0))) {
            throw new IllegalArgumentException(String.format(ARG_MESSAGE, "1"));
        }

        String id = args.get(0);

        return ERC721.ownerOf(stub, id);
    }

    private String transferFrom(ChaincodeStub stub, List<String> args) throws IOException {
        if (args.size() != 3 || isNullOrEmpty(args.get(0)) || isNullOrEmpty(args.get(1)) || isNullOrEmpty(args.get(2))) {
            throw new IllegalArgumentException(String.format(ARG_MESSAGE, "3"));
        }

        String from = args.get(0);
        String to = args.get(1);
        String id = args.get(2);

        return Boolean.toString(ERC721.transferFrom(stub, from, to, id));
    }

    private String approve(ChaincodeStub stub, List<String> args) throws IOException {
        if (args.size() != 2 || isNullOrEmpty(args.get(0)) || isNullOrEmpty(args.get(1))) {
            throw new IllegalArgumentException(String.format(ARG_MESSAGE, "2"));
        }

        String approved = args.get(0);
        String id = args.get(1);

        return Boolean.toString(ERC721.approve(stub, approved, id));
    }

    private String setApprovalForAll(ChaincodeStub stub, List<String> args) throws IOException {
        if (args.size() != 2 || isNullOrEmpty(args.get(0)) || isNullOrEmpty(args.get(1))) {
            throw new IllegalArgumentException(String.format(ARG_MESSAGE, "2"));
        }

        String operator = args.get(0);
        boolean approved = Boolean.parseBoolean(args.get(1));

        return Boolean.toString(ERC721.setApprovalForAll(stub, operator, approved));
    }

    private String getApproved(ChaincodeStub stub, List<String> args) throws IOException {
        if (args.size() != 1 || isNullOrEmpty(args.get(0))) {
            throw new IllegalArgumentException(String.format(ARG_MESSAGE, "1"));
        }

        String id = args.get(0);

        return ERC721.getApproved(stub, id);
    }

    private String isApprovedForAll(ChaincodeStub stub, List<String> args) throws IOException {
        if (args.size() != 2 || isNullOrEmpty(args.get(0)) || isNullOrEmpty(args.get(1))) {
            throw new IllegalArgumentException(String.format(ARG_MESSAGE, "2"));
        }

        String owner = args.get(0);
        String operator = args.get(1);

        return Boolean.toString(ERC721.isApprovedForAll(stub, owner, operator));
    }

    private String mint(ChaincodeStub stub, List<String> args) throws IOException {
        if (args.size() == 1) {
            if(isNullOrEmpty(args.get(0))) {
                throw new IllegalArgumentException(String.format(ARG_MESSAGE, "1"));
            }

            String id = args.get(0);

            return Boolean.toString(Default.mint(stub, id));
        }
        else if (args.size() == 4) {
            if (isNullOrEmpty(args.get(0)) || isNullOrEmpty(args.get(1)) || isNullOrEmpty(args.get(2)) || isNullOrEmpty(args.get(3))) {
                throw new IllegalArgumentException(String.format(ARG_MESSAGE, "4"));
            }

            String id = args.get(0);
            String type = args.get(1);
            Map<String, Object> xattr =
                    objectMapper.readValue(args.get(2), new TypeReference<HashMap<String, Object>>() {});
            Map<String, String> uri =
                    objectMapper.readValue(args.get(3), new TypeReference<HashMap<String, String>>() {});

            return Boolean.toString(Extension.mint(stub, id, type, xattr, uri));
        }

        throw new IllegalArgumentException(String.format(ARG_MESSAGE, "1 or 4"));
    }

    private String burn(ChaincodeStub stub, List<String> args) throws IOException {
        if (args.size() != 1 || isNullOrEmpty(args.get(0))) {
            throw new IllegalArgumentException(String.format(ARG_MESSAGE, "1"));
        }

        String id = args.get(0);

        return Boolean.toString(Default.burn(stub, id));
    }

    private String getType(ChaincodeStub stub, List<String> args) throws IOException {
        if (args.size() != 1 || isNullOrEmpty(args.get(0))) {
            throw new IllegalArgumentException(String.format(ARG_MESSAGE, "1"));
        }

        String id = args.get(0);

        return Default.getType(stub, id);
    }

    private String tokenIdsOf(ChaincodeStub stub, List<String> args) {
        if (args.size() == 1) {
            if (isNullOrEmpty(args.get(0))) {
                throw new IllegalArgumentException(String.format(ARG_MESSAGE, "1"));
            }

            String owner = args.get(0);

            return Default.tokenIdsOf(stub, owner).toString();
        }
        else if (args.size() == 2) {
            if (isNullOrEmpty(args.get(0)) || isNullOrEmpty(args.get(1))) {
                throw new IllegalArgumentException(String.format(ARG_MESSAGE, "2"));
            }

            String owner = args.get(0);
            String type = args.get(1);

            return Extension.tokenIdsOf(stub, owner, type).toString();
        }

        throw new IllegalArgumentException(String.format(ARG_MESSAGE, "1 or 2"));
    }

    private String query(ChaincodeStub stub, List<String> args) throws IOException {
        if (args.size() != 1 || isNullOrEmpty(args.get(0))) {
            throw new IllegalArgumentException(String.format(ARG_MESSAGE, "1"));
        }

        String id = args.get(0);

        return Default.query(stub, id);
    }

    private String history(ChaincodeStub stub, List<String> args) {
        if (args.size() != 1 || isNullOrEmpty(args.get(0))) {
            throw new IllegalArgumentException(String.format(ARG_MESSAGE, "1"));
        }

        String id = args.get(0);

        return Default.history(stub, id).toString();
    }

    private String setURI(ChaincodeStub stub, List<String> args) throws IOException {
        if (args.size() == 2) {
            if (isNullOrEmpty(args.get(0)) || isNullOrEmpty(args.get(1))) {
                throw new IllegalArgumentException(String.format(ARG_MESSAGE, "2"));
            }
            String id = args.get(0);
            Map<String, String> uri =
                    objectMapper.readValue(args.get(1), new TypeReference<HashMap<String, String>>() {});

            return Boolean.toString(Extension.setURI(stub, id, uri));
        }
        else if (args.size() == 3) {
            if (isNullOrEmpty(args.get(0)) || isNullOrEmpty(args.get(1)) || isNullOrEmpty(args.get(2))) {
                throw new IllegalArgumentException(String.format(ARG_MESSAGE, "3"));
            }

            String id = args.get(0);
            String index = args.get(1);
            String value = args.get(2);

            return Boolean.toString(Extension.setURI(stub, id, index, value));

        }

        throw new IllegalArgumentException(String.format(ARG_MESSAGE, "2 or 3"));
    }

    private String getURI(ChaincodeStub stub, List<String> args) throws IOException {
        if (args.size() == 1) {
            if (isNullOrEmpty(args.get(0))) {
                throw new IllegalArgumentException(String.format(ARG_MESSAGE, "1"));
            }

            String id = args.get(0);

            return objectMapper.writeValueAsString(Extension.getURI(stub, id));
        }
        else if (args.size() == 2) {
            if (isNullOrEmpty(args.get(0)) || isNullOrEmpty(args.get(1))) {
                throw new IllegalArgumentException(String.format(ARG_MESSAGE, "2"));
            }

            String id = args.get(0);
            String index = args.get(1);

            return Extension.getURI(stub, id, index);
        }

        throw new IllegalArgumentException(String.format(ARG_MESSAGE, "1 or 2"));
    }

    private String setXAttr(ChaincodeStub stub, List<String> args) throws IOException {
        if (args.size() == 2) {
            if (isNullOrEmpty(args.get(0)) || isNullOrEmpty(args.get(1))) {
                throw new IllegalArgumentException(String.format(ARG_MESSAGE, "2"));
            }

            String id = args.get(0);
            Map<String, Object> xattr =
                    objectMapper.readValue(args.get(1), new TypeReference<HashMap<String, Object>>() {});

            return Boolean.toString(Extension.setXAttr(stub, id, xattr));
        }
        else if (args.size() == 3) {
            if (isNullOrEmpty(args.get(0)) || isNullOrEmpty(args.get(1)) || isNullOrEmpty(args.get(2))) {
                throw new IllegalArgumentException(String.format(ARG_MESSAGE, "3"));
            }

            String id = args.get(0);
            String index = args.get(1);

            TokenTypeManager manager = TokenTypeManager.load(stub);
            List<String> info = manager.getAttribute(Default.getType(stub, id), index);
            String dataType = info.get(0);
            Object value = DataTypeConversion.strToDataType(dataType, args.get(2));

            return Boolean.toString(Extension.setXAttr(stub, id, index, value));
        }

        throw new IllegalArgumentException(String.format(ARG_MESSAGE, "2 or 3"));
    }

    private String getXAttr(ChaincodeStub stub, List<String> args)  throws IOException {
        if (args.size() == 1) {
            if (isNullOrEmpty(args.get(0))) {
                throw new IllegalArgumentException(String.format(ARG_MESSAGE, "1"));
            }

            String id = args.get(0);

            return objectMapper.writeValueAsString(Extension.getXAttr(stub, id));
        }
        else if(args.size() == 2) {
            if (isNullOrEmpty(args.get(0)) || isNullOrEmpty(args.get(1))) {
                throw new IllegalArgumentException(String.format(ARG_MESSAGE, "2"));
            }

            String id = args.get(0);
            String index = args.get(1);

            Object value = Extension.getXAttr(stub, id, index);

            TokenTypeManager manager = TokenTypeManager.load(stub);
            List<String> info = manager.getAttribute(Default.getType(stub, id), index);
            String dataType = info.get(0);

            return DataTypeConversion.dataTypeToStr(dataType, value);
        }

        throw new IllegalArgumentException(String.format(ARG_MESSAGE, "1 or 2"));
    }

    private String tokenTypesOf(ChaincodeStub stub) throws IOException {
        List<String> tokenTypes = TokenTypeManagement.tokenTypesOf(stub);
        return tokenTypes.toString();
    }

    private String enrollTokenType(ChaincodeStub stub, List<String> args) throws IOException {
        if (args.size() != 2 || isNullOrEmpty(args.get(0)) || isNullOrEmpty(args.get(1))) {
            throw new IllegalArgumentException(String.format(ARG_MESSAGE, "2"));
        }

        String type = args.get(0);
        Map<String, List<String>> attributes = objectMapper.readValue(args.get(1),
                new TypeReference<HashMap<String, List<String>>>() {});

        return Boolean.toString(TokenTypeManagement.enrollTokenType(stub, type, attributes));
    }

    private String dropTokenType(ChaincodeStub stub, List<String> args) throws IOException {
        if (args.size() != 1 || isNullOrEmpty(args.get(0))) {
            throw new IllegalArgumentException(String.format(ARG_MESSAGE, "2"));
        }

        String type = args.get(0);

        return Boolean.toString(TokenTypeManagement.dropTokenType(stub, type));
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

        List<String> info = TokenTypeManagement.retrieveAttributeOfTokenType(stub, tokenType, attribute);
        if (info == null || info.size() == 0) {
            return null;
        }
        return info.toString();
    }

    public static void main(String[] args) {
        new Main().start(args);
    }
}
