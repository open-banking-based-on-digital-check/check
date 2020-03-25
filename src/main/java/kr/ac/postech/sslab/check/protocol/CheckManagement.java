package kr.ac.postech.sslab.check.protocol;

import com.google.protobuf.ByteString;
import kr.ac.postech.sslab.check.manager.BankManager;
import kr.ac.postech.sslab.fabasset.chaincode.client.Address;
import kr.ac.postech.sslab.fabasset.chaincode.manager.TokenManager;
import kr.ac.postech.sslab.fabasset.chaincode.manager.TokenTypeManager;
import kr.ac.postech.sslab.fabasset.chaincode.protocol.Default;
import kr.ac.postech.sslab.fabasset.chaincode.protocol.Extension;
import kr.ac.postech.sslab.fabasset.chaincode.util.DataTypeConversion;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.io.IOException;
import java.util.*;

import static kr.ac.postech.sslab.fabasset.chaincode.constant.Key.*;

public class CheckManagement {
    private static final String CHECK_TYPE = "check";

    private static final String BANK_KEY = "bank";
    private static final String BALANCE_KEY = "balance";
    private static final String PARENT_KEY = "parent";

    private CheckManagement() {}

    private static void eventIssue(ChaincodeStub stub, String from, String to, String id) {
        String message = String.format("Issue %s: from %s to %s", id, from, to);
        stub.setEvent("Issue", ByteString.copyFromUtf8(message).toByteArray());
    }

    public static boolean issue(ChaincodeStub stub, String id, String issuer, Map<String, Object> xattr) throws IOException {
        BankManager bankManager = BankManager.load(stub);

        String caller = Address.getMyAddress(stub);
        if (!bankManager.hasBank(caller)) {
            return false;
        }

        TokenTypeManager manager = TokenTypeManager.load(stub);
        Map<String, List<String>> attributes = manager.getType(CHECK_TYPE);
        if (attributes == null) {
            return false;
        }

        for (Map.Entry<String, List<String>> attribute: attributes.entrySet()) {
            String attributeName = attribute.getKey();
            List<String> info = attribute.getValue();
            String dataType = info.get(0);
            String initialValueString = info.get(1);

            if (!xattr.containsKey(attributeName) && !attributeName.equals(ADMIN_KEY)) {
                Object initialValue = DataTypeConversion.strToDataType(dataType, initialValueString);
                if (initialValue == null) {
                    return false;
                }

                xattr.put(attributeName, initialValue);
            }
        }

        Map<String, String> uri = new HashMap<>();
        uri.put(PATH_KEY, "");
        uri.put(HASH_KEY, "");

        TokenManager nft = new TokenManager();
        if (nft.hasToken(stub, id)) {
            return false;
        }

        nft.setId(id);
        nft.setType(CHECK_TYPE);
        nft.setOwner(issuer);
        nft.setApprovee("");
        nft.setXAttr(xattr);
        nft.setURI(uri);
        nft.store(stub);

        eventIssue(stub, caller, issuer, id);

        return true;
    }

    public static boolean merge(ChaincodeStub stub, String newTokenId, List<String> mergedTokenIds) throws IOException {
        if (mergedTokenIds.size() != 2 || stub.getStringState(mergedTokenIds.get(0)).length() == 0 || stub.getStringState(mergedTokenIds.get(1)).length() == 0) {
            return false;
        }

        String[] guranteeBanks = new String[2];
        guranteeBanks[0] = (String) Extension.getXAttr(stub, mergedTokenIds.get(0), BANK_KEY);
        guranteeBanks[1] = (String) Extension.getXAttr(stub, mergedTokenIds.get(1), BANK_KEY);
        if (!guranteeBanks[0].equals(guranteeBanks[1])) {
            return false;
        }

        int[] balances = new int[2];
        balances[0] = (int) Extension.getXAttr(stub, mergedTokenIds.get(0), BALANCE_KEY);
        balances[1] = (int) Extension.getXAttr(stub, mergedTokenIds.get(1), BALANCE_KEY);
        int totalBalance = balances[0] + balances[1];

        Map<String, Object> xattr = new HashMap<>();
        xattr.put(BANK_KEY, guranteeBanks[0]);
        xattr.put(BALANCE_KEY, totalBalance);
        xattr.put(PARENT_KEY, mergedTokenIds);

        Extension.mint(stub, newTokenId, CHECK_TYPE, xattr, null);

        Default.burn(stub, mergedTokenIds.get(0));
        Default.burn(stub, mergedTokenIds.get(1));

        return true;
    }

    public static boolean divide(ChaincodeStub stub, String id, List<String> newIds, List<Integer> balances) throws IOException {
        if (newIds.size() != 2 || balances.size() != 2) {
            return false;
        }

        TokenManager nft = TokenManager.load(stub, id);
        String guranteedBank = (String) nft.getXAttr(BANK_KEY);
        int balance = (int) nft.getXAttr(BALANCE_KEY);

        if (balance != balances.get(0) + balances.get(1)) {
            return false;
        }

        for (int i = 0; i < 2; i++) {
            Map<String, Object> xattr = new HashMap<>();
            xattr.put(BALANCE_KEY, balances.get(i));
            xattr.put(BANK_KEY, guranteedBank);
            List<String> parent = new ArrayList<>();
            parent.add(id);
            xattr.put(PARENT_KEY, parent);

            Extension.mint(stub, newIds.get(i), CHECK_TYPE, xattr, null);
        }

        Default.burn(stub, id);

        return true;
    }
}
