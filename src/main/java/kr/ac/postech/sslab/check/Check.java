package kr.ac.postech.sslab.check;

import kr.ac.postech.sslab.fabasset.chaincode.protocol.ERC721;
import kr.ac.postech.sslab.fabasset.chaincode.protocol.Extension;
import org.hyperledger.fabric.shim.ChaincodeStub;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

class Check {
    private static final String CHECK_TYPE = "check";

    private static final String BANK_KEY = "bank";

    private static final String BALANCE_KEY = "balance";

    private static final String SENDER_KEY = "sender";

    private static final String DATE_AND_TIME_FORMAT = "yyyy/MM/dd HH:mm:ss";

    boolean issue(ChaincodeStub stub, String bank, String issuer, long balance) throws IOException {
        List<String> issuerIds = Extension.tokenIdsOf(stub, issuer, CHECK_TYPE);

        boolean hasBank = false;
        String id = null;
        for (String issuerId: issuerIds) {
            String whichBank = Extension.getXAttr(stub, issuerId, BANK_KEY);

            if (whichBank == null) {
                return false;
            }

            hasBank = whichBank.equals(bank);

            if (hasBank) {
                id = issuerId;
                break;
            }
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DATE_AND_TIME_FORMAT);
        LocalDateTime now = LocalDateTime.now();
        String nowDataAndTime = dtf.format(now);

        if (hasBank) {
            String currentBalanceString = Extension.getXAttr(stub, id, BALANCE_KEY);
            if (currentBalanceString == null) {
                return false;
            }

            long currentBalance = Long.parseLong(currentBalanceString);
            currentBalance += balance;
            Extension.setXAttr(stub, id, BALANCE_KEY, Long.toString(currentBalance));

            List<String> sender_info = new ArrayList<>();
            sender_info.add(bank);
            sender_info.add(Long.toString(balance));
            sender_info.add(nowDataAndTime);
            Extension.setXAttr(stub, id, SENDER_KEY, sender_info.toString());
        }
        else {
            Map<String, Object> xattr = new HashMap<>();
            xattr.put(BANK_KEY, bank);
            xattr.put(BALANCE_KEY, balance);

            xattr.put(SENDER_KEY, new ArrayList<>(Arrays.asList(bank, balance, nowDataAndTime)));

            do {
                long unixTime = Instant.now().getEpochSecond();
                id = Long.toString(unixTime);
            } while (stub.getStringState(id).length() == 0);

            Extension.mint(stub, id, CHECK_TYPE, xattr, null);
            ERC721.transferFrom(stub, bank, issuer, id);
        }

        return true;
    }

    boolean send(ChaincodeStub stub, String sender, String receiver, long balance) {
        return true;
    }

    boolean redeem(ChaincodeStub stub, String redeemer, String bank, String account, long balance) {
        return true;
    }
}
