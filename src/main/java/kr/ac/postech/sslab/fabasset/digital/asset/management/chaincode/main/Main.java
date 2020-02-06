package kr.ac.postech.sslab.fabasset.digital.asset.management.chaincode.main;

import com.fasterxml.jackson.core.JsonProcessingException;
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

                default:
                    return ResponseUtils.newErrorResponse(NO_FUNCTION_MESSAGE);
            }

            return ResponseUtils.newSuccessResponse(response);
        } catch (Exception e) {
            return ResponseUtils.newErrorResponse(e.getMessage());
        }
    }

    private String balanceOf(ChaincodeStub stub, List<String> args) {
        if (args.size() != 1 || isNullOrEmpty(args.get(0))) {
            throw new IllegalArgumentException(String.format(ARG_MESSAGE, "1"));
        }

        String owner = args.get(0);

        return Long.toString(ERC721.balanceOf(stub, owner));
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

    private String mint(ChaincodeStub stub, List<String> args) throws JsonProcessingException {
        if (args.size() != 2 || isNullOrEmpty(args.get(0))
                || isNullOrEmpty(args.get(1))) {
            throw new IllegalArgumentException(String.format(ARG_MESSAGE, "2"));
        }

        String tokenId = args.get(0);
        String owner = args.get(1);

        return Boolean.toString(Default.mint(stub, tokenId, owner));
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

    public static void main(String[] args) {
        new Main().start(args);
    }

}
