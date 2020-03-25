package kr.ac.postech.sslab.check;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.ac.postech.sslab.check.protocol.BankManagement;
import kr.ac.postech.sslab.check.protocol.CheckManagement;
import kr.ac.postech.sslab.fabasset.chaincode.Main;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ResponseUtils;

import java.io.IOException;
import java.util.*;

import static io.netty.util.internal.StringUtil.isNullOrEmpty;
import static kr.ac.postech.sslab.fabasset.chaincode.constant.Message.ARG_MESSAGE;

public class CheckMain extends Main {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String REGISTER_BANK_FUNCTION_NAME = "registerBank";

    private static final String RETRIEVE_REGISTERED_BANKS_FUNCTION_NAME = "retrieveRegisteredBanks";

    private static final String ISSUE_FUNCTION_NAME = "issue";

    private static final String MERGE_FUNCTION_NAME = "merge";

    private static final String DIVIDE_FUNCTION_NAME = "divide";

    @Override
    public Response invoke(ChaincodeStub stub) {
        try {
            String func = stub.getFunction();
            List<String> args = stub.getParameters();
            String response;

            switch (func) {
                case REGISTER_BANK_FUNCTION_NAME:
                    response = Boolean.toString(registerBank(stub, args));
                    break;

                case RETRIEVE_REGISTERED_BANKS_FUNCTION_NAME:
                    response = retrieveRegisteredBanks(stub, args).toString();
                    break;

                case ISSUE_FUNCTION_NAME:
                    response = Boolean.toString(issue(stub, args));
                    break;

                case MERGE_FUNCTION_NAME:
                    response = Boolean.toString(merge(stub, args));
                    break;

                case DIVIDE_FUNCTION_NAME:
                    response = Boolean.toString(divide(stub, args));
                    break;

                default:
                    return super.invoke(stub);
            }

            return ResponseUtils.newSuccessResponse(response);
        } catch (Exception e) {
            return ResponseUtils.newErrorResponse(e.getMessage());
        }
    }

    private boolean registerBank(ChaincodeStub stub, List<String> args) throws IOException {
        if (args.size() != 1 || isNullOrEmpty(args.get(0))) {
            throw new IllegalArgumentException(String.format(ARG_MESSAGE, "1"));
        }

        String bank = args.get(0);

        return BankManagement.registerBank(stub, bank);
    }

    private List<String> retrieveRegisteredBanks(ChaincodeStub stub, List<String> args) throws IOException {
        if (args.size() != 0) {
            throw new IllegalArgumentException(String.format(ARG_MESSAGE, "0"));
        }

        return BankManagement.retrieveRegisteredBanks(stub);
    }

    private boolean issue(ChaincodeStub stub, List<String> args) throws IOException{
        if (args.size() != 3 || isNullOrEmpty(args.get(0)) || isNullOrEmpty(args.get(1)) || isNullOrEmpty(args.get(2))) {
            throw new IllegalArgumentException(String.format(ARG_MESSAGE, "3"));
        }

        String id = args.get(0);
        String issuer = args.get(1);
        String xattrString = args.get(2);
        Map<String, Object> xattr = objectMapper.readValue(xattrString, new TypeReference<HashMap<String, Object>>() {});

        return CheckManagement.issue(stub, id, issuer, xattr);
    }


    private boolean merge(ChaincodeStub stub, List<String> args) throws IOException {
        if (args.size() != 2 || isNullOrEmpty(args.get(0)) || isNullOrEmpty(args.get(1))) {
            throw new IllegalArgumentException(String.format(ARG_MESSAGE, "2"));
        }

        String newTokenId = args.get(0);
        String mergedTokenIdsString = args.get(1);
        List<String> mergedTokenIds = new ArrayList<>(toList(mergedTokenIdsString));

        return CheckManagement.merge(stub, newTokenId, mergedTokenIds);
    }

    private static List<String> toList(String value) {
        return Arrays.asList(value.substring(1, value.length() - 1).split(", "));
    }

    private boolean divide(ChaincodeStub stub, List<String> args) throws IOException {
        if (args.size() != 3 || isNullOrEmpty(args.get(0)) || isNullOrEmpty(args.get(1)) || isNullOrEmpty(args.get(2))) {
            throw new IllegalArgumentException(String.format(ARG_MESSAGE, "3"));
        }

        String id = args.get(0);
        String newIdsString = args.get(1);
        List<String> newIds = new ArrayList<>(toList(newIdsString));
        String balancesString = args.get(2);
        List<String> strings = new ArrayList<>(toList(balancesString));
        List<Integer> balances = new ArrayList<>();
        for (String string: strings) {
            int integer = Integer.parseInt(string);
            balances.add(integer);
        }

        return CheckManagement.divide(stub, id, newIds, balances);
    }

    public static void main(String[] args) {
        new CheckMain().start(args);
    }
}
