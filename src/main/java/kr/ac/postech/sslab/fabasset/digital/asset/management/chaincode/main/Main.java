package kr.ac.postech.sslab.fabasset.digital.asset.management.chaincode.main;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.ac.postech.sslab.fabasset.digital.asset.management.chaincode.extension.Extension;
import kr.ac.postech.sslab.fabasset.digital.asset.management.chaincode.extension.TokenTypeManagement;
import kr.ac.postech.sslab.fabasset.digital.asset.management.chaincode.standard.Default;
import kr.ac.postech.sslab.fabasset.digital.asset.management.chaincode.standard.ERC721;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ResponseUtils;

import static kr.ac.postech.sslab.fabasset.digital.asset.management.chaincode.constant.Function.*;
import static kr.ac.postech.sslab.fabasset.digital.asset.management.chaincode.constant.Message.ARG_MESSAGE;
import static kr.ac.postech.sslab.fabasset.digital.asset.management.chaincode.constant.Message.NO_FUNCTION_MESSAGE;
import static io.netty.util.internal.StringUtil.isNullOrEmpty;

import java.io.IOException;
import java.util.*;


public class Main extends CustomChaincodeBase {
    private static ObjectMapper objectMapper = new ObjectMapper();

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

                case QUERY_HISTORY_FUNCTION_NAME:
                    response = queryHistory(stub, args);
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
                    return ResponseUtils.newErrorResponse(NO_FUNCTION_MESSAGE);
            }

            return ResponseUtils.newSuccessResponse(response);
        } catch (Exception e) {
            return ResponseUtils.newErrorResponse(e.getMessage());
        }
    }

    private String balanceOf(ChaincodeStub stub, List<String> args) throws IOException {
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

        String tokenId = args.get(0);

        return ERC721.ownerOf(stub, tokenId);
    }

    private String transferFrom(ChaincodeStub stub, List<String> args) throws IOException {
        if (args.size() != 3 || isNullOrEmpty(args.get(0))
                || isNullOrEmpty(args.get(1)) || isNullOrEmpty(args.get(2))) {
            throw new IllegalArgumentException(String.format(ARG_MESSAGE, "3"));
        }

        String from = args.get(0);
        String to = args.get(1);
        String tokenId = args.get(2);

        return Boolean.toString(ERC721.transferFrom(stub, from, to, tokenId));
    }

    private String approve(ChaincodeStub stub, List<String> args) throws IOException {
        if (args.size() != 2 || isNullOrEmpty(args.get(0))
                || isNullOrEmpty(args.get(1))) {
            throw new IllegalArgumentException(String.format(ARG_MESSAGE, "2"));
        }

        String approved = args.get(0);
        String tokenId = args.get(1);

        return Boolean.toString(ERC721.approve(stub, approved, tokenId));
    }

    private String setApprovalForAll(ChaincodeStub stub, List<String> args) throws IOException {
        if (args.size() != 3 || isNullOrEmpty(args.get(0))
                || isNullOrEmpty(args.get(1)) || isNullOrEmpty(args.get(2))) {
            throw new IllegalArgumentException(String.format(ARG_MESSAGE, "3"));
        }

        String caller = args.get(0);
        String operator = args.get(1);
        boolean approved = Boolean.parseBoolean(args.get(2));

        return Boolean.toString(ERC721.setApprovalForAll(stub, caller, operator, approved));
    }

    private String getApproved(ChaincodeStub stub, List<String> args) throws IOException {
        if (args.size() != 1 || isNullOrEmpty(args.get(0))) {
            throw new IllegalArgumentException(String.format(ARG_MESSAGE, "1"));
        }

        String tokenId = args.get(0);

        return ERC721.getApproved(stub, tokenId);
    }

    private String isApprovedForAll(ChaincodeStub stub, List<String> args) throws IOException {
        if (args.size() != 2 || isNullOrEmpty(args.get(0))
                || isNullOrEmpty(args.get(1))) {
            throw new IllegalArgumentException(String.format(ARG_MESSAGE, "2"));
        }

        String owner = args.get(0);
        String operator = args.get(1);

        return Boolean.toString(ERC721.isApprovedForAll(stub, owner, operator));
    }

    private String mint(ChaincodeStub stub, List<String> args) throws IOException {
        if (args.size() == 2) {
            if(isNullOrEmpty(args.get(0)) || isNullOrEmpty(args.get(1))) {
                throw new IllegalArgumentException(String.format(ARG_MESSAGE, "2"));
            }

            String tokenId = args.get(0);
            String owner = args.get(1);
            return Boolean.toString(Default.mint(stub, tokenId, owner));
        }
        else if (args.size() == 5) {
            if (isNullOrEmpty(args.get(0)) || isNullOrEmpty(args.get(1)) || isNullOrEmpty(args.get(2))
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

        throw new IllegalArgumentException(String.format(ARG_MESSAGE, "2 or 5"));
    }

    private String burn(ChaincodeStub stub, List<String> args) throws IOException {
        if (args.size() != 1 || isNullOrEmpty(args.get(0))) {
            throw new IllegalArgumentException(String.format(ARG_MESSAGE, "1"));
        }

        String tokenId = args.get(0);

        return Boolean.toString(Default.burn(stub, tokenId));
    }

    private String getType(ChaincodeStub stub, List<String> args) throws IOException {
        if (args.size() != 1 || isNullOrEmpty(args.get(0))) {
            throw new IllegalArgumentException(String.format(ARG_MESSAGE, "1"));
        }

        String tokenId = args.get(0);

        return Default.getType(stub, tokenId);
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
            throw new IllegalArgumentException(String.format(ARG_MESSAGE, "1 or 2"));
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

    public static void main(String[] args) {
        new Main().start(args);
    }
}
