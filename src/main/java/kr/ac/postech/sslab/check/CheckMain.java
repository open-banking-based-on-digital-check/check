package kr.ac.postech.sslab.check;

import kr.ac.postech.sslab.fabasset.chaincode.Main;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ResponseUtils;

import java.io.IOException;
import java.util.List;

import static io.netty.util.internal.StringUtil.isNullOrEmpty;
import static kr.ac.postech.sslab.fabasset.chaincode.constant.Message.ARG_MESSAGE;

public class CheckMain extends Main {
    private static final String ISSUE_FUNCTION_NAME = "issue";

    private static final String SEND_FUNCTION_NAME = "send";

    private static final String REDEEM_FUNCTION_NAME = "redeem";

    private Check check = new Check();

    @Override
    public Response invoke(ChaincodeStub stub) {
        try {
            String func = stub.getFunction();
            List<String> args = stub.getParameters();
            String response;

            switch (func) {
                case ISSUE_FUNCTION_NAME:
                    response = Boolean.toString(issue(stub, args));
                    break;

                case SEND_FUNCTION_NAME:
                    response = Boolean.toString(send(stub, args));
                    break;

                case REDEEM_FUNCTION_NAME:
                    response = Boolean.toString(redeem(stub, args));
                    break;

                default:
                    return super.invoke(stub);
            }

            return ResponseUtils.newSuccessResponse(response);
        } catch (Exception e) {
            return ResponseUtils.newErrorResponse(e.getMessage());
        }
    }

    private boolean issue(ChaincodeStub stub, List<String> args) throws IOException{
        if (args.size() != 3 || isNullOrEmpty(args.get(0))
                || isNullOrEmpty(args.get(1)) || isNullOrEmpty(args.get(2))) {
            throw new IllegalArgumentException(String.format(ARG_MESSAGE, "3"));
        }

        String bank = args.get(0);
        String issuer = args.get(1);
        String balance = args.get(2);

        return check.issue(stub, bank, issuer, Integer.parseInt(balance));
    }

    private boolean send(ChaincodeStub stub, List<String> args) throws IOException {
        if (args.size() != 3 || isNullOrEmpty(args.get(0))
                || isNullOrEmpty(args.get(1)) || isNullOrEmpty(args.get(2))) {
            throw new IllegalArgumentException(String.format(ARG_MESSAGE, "3"));
        }

        String sender = args.get(0);
        String receiver = args.get(1);
        String balance = args.get(2);

        return check.send(stub, sender, receiver, Integer.parseInt(balance));
    }

    private boolean redeem(ChaincodeStub stub, List<String> args) {
        if (args.size() != 4 || isNullOrEmpty(args.get(0)) || isNullOrEmpty(args.get(1))
                || isNullOrEmpty(args.get(2)) || isNullOrEmpty(args.get(3))) {
            throw new IllegalArgumentException(String.format(ARG_MESSAGE, "4"));
        }

        String redeemer = args.get(0);
        String bank = args.get(1);
        String account = args.get(2);
        String balance = args.get(3);

        return check.redeem(stub, redeemer, bank, account, Integer.parseInt(balance));
    }
    public static void main(String[] args) {
        new CheckMain().start(args);
    }
}
