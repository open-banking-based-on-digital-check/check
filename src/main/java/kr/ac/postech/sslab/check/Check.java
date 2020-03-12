package kr.ac.postech.sslab.check;

import kr.ac.postech.sslab.fabasset.chaincode.protocol.ERC721;
import kr.ac.postech.sslab.fabasset.chaincode.protocol.Extension;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.javatuples.Triplet;

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
    private static final String RECEIVER_KEY = "receiver";

    private static final String DATE_AND_TIME_FORMAT = "yyyy/MM/dd HH:mm:ss";

    private String  getNowDateAndTime() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DATE_AND_TIME_FORMAT);
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }

    private void updateSender(ChaincodeStub stub, String sender, String balance) {

    }

    boolean issue(ChaincodeStub stub, String bank, String issuer, int balance) throws IOException {
        List<String> issuerIds = Extension.tokenIdsOf(stub, issuer, CHECK_TYPE);

        String nowDataAndTime = getNowDateAndTime();

        boolean hasBank = false;
        String id = null;
        for (String issuerId: issuerIds) {
            String whichBank = (String) Extension.getXAttr(stub, issuerId, BANK_KEY);

            if (whichBank == null) {
                return false;
            }

            hasBank = whichBank.equals(bank);

            if (hasBank) {
                id = issuerId;
                break;
            }
        }

        if (hasBank) {
            int currentBalance = (int) Extension.getXAttr(stub, id, BALANCE_KEY);
            currentBalance += balance;
            Extension.setXAttr(stub, id, BALANCE_KEY, Integer.toString(currentBalance));

            List<String> senderInfo = new ArrayList<>();
            senderInfo.add(bank);
            senderInfo.add(Integer.toString(balance));
            senderInfo.add(nowDataAndTime);
            Extension.setXAttr(stub, id, SENDER_KEY, senderInfo.toString());
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

    boolean send(ChaincodeStub stub, String sender, String receiver, int balance) throws IOException {
        List<String> senderIds = Extension.tokenIdsOf(stub, sender, CHECK_TYPE);
        List<String> receiverIds = Extension.tokenIdsOf(stub, receiver, CHECK_TYPE);

        String nowDataAndTime = getNowDateAndTime();

        int senderTotalBalance = 0;
        for (String senderId : senderIds) {
            int senderBalance = (int) Extension.getXAttr(stub, senderId, BALANCE_KEY);
            senderTotalBalance += senderBalance;
        }

        if (senderTotalBalance < balance) {
            return false;
        }

        // id bank balance
        List<Triplet<String, String, Integer>> senderBankToBalances = new LinkedList<>();

        for (String senderId : senderIds) {
            String senderBank = (String) Extension.getXAttr(stub, senderId, BANK_KEY);
            if (senderBank == null) {
                return false;
            }

            int senderBalance = (int) Extension.getXAttr(stub, senderId, BALANCE_KEY);

            senderBankToBalances.add(new Triplet<>(senderId, senderBank, senderBalance));
        }

        senderBankToBalances.sort(new BalanceComparator());

        List<Triplet<String, String, Integer>> receiverBankToBalances = new LinkedList<>();

        for (String receiverId : receiverIds) {
            String receiverBank = (String) Extension.getXAttr(stub, receiverId, BANK_KEY);
            if (receiverBank == null) {
                return false;
            }

            int receiverBalance = (int) Extension.getXAttr(stub, receiverId, BALANCE_KEY);

            receiverBankToBalances.add(new Triplet<>(receiverId, receiverBank, receiverBalance));
        }

        int remainingBalance = balance;
        for (Triplet<String, String, Integer> senderBankToBalance: senderBankToBalances) {
            if (remainingBalance > senderBankToBalance.getValue2()) {
                remainingBalance -= senderBankToBalance.getValue2();

                boolean hasBank = false;
                Triplet<String, String, Integer> currentReceiverBankToBalance = null;
                for (Triplet<String, String, Integer> receiverBankToBalance: receiverBankToBalances) {
                    hasBank = receiverBankToBalance.getValue1().equals(senderBankToBalance.getValue1());
                    if (hasBank) {
                        currentReceiverBankToBalance = receiverBankToBalance;
                        break;
                    }
                }

                if (hasBank) {
                    Extension.setXAttr(stub, senderBankToBalance.getValue0(), BALANCE_KEY, Integer.toString(0));

                    List<String> receiverInfo = new ArrayList<>();
                    receiverInfo.add(receiver);
                    receiverInfo.add(Integer.toString(balance));
                    receiverInfo.add(nowDataAndTime);
                    Extension.setXAttr(stub, senderBankToBalance.getValue0(), RECEIVER_KEY, receiverInfo.toString());

                    Extension.setXAttr(stub, currentReceiverBankToBalance.getValue0(), BALANCE_KEY,
                            Integer.toString(currentReceiverBankToBalance.getValue2() + senderBankToBalance.getValue2()));

                    List<String> senderInfo = new ArrayList<>();
                    senderInfo.add(sender);
                    senderInfo.add(Integer.toString(balance));
                    senderInfo.add(nowDataAndTime);
                    Extension.setXAttr(stub, currentReceiverBankToBalance.getValue0(), SENDER_KEY, senderInfo.toString());
                }
                else {
                    Extension.setXAttr(stub, senderBankToBalance.getValue0(), BALANCE_KEY, Integer.toString(0));

                    Map<String, Object> xattr = new HashMap<>();
                    xattr.put(BANK_KEY, senderBankToBalance.getValue1());
                    xattr.put(BALANCE_KEY, balance);
                    xattr.put(SENDER_KEY, new ArrayList<>(Arrays.asList(sender, balance, nowDataAndTime)));

                    String newId;
                    do {
                        long unixTime = Instant.now().getEpochSecond();
                        newId = Long.toString(unixTime);
                    } while (stub.getStringState(newId).length() == 0);

                    Extension.mint(stub, newId, CHECK_TYPE, xattr, null);
                    ERC721.transferFrom(stub, sender, receiver, newId);
                }
            }
            else if (remainingBalance < senderBankToBalance.getValue2()) {
                int newSenderBankToBalance = senderBankToBalance.getValue2() - remainingBalance;

                boolean hasBank = false;
                Triplet<String, String, Integer> currentReceiverBankToBalance = null;
                for (Triplet<String, String, Integer> receiverBankToBalance: receiverBankToBalances) {
                    hasBank = receiverBankToBalance.getValue1().equals(senderBankToBalance.getValue1());
                    if (hasBank) {
                        currentReceiverBankToBalance = receiverBankToBalance;
                        break;
                    }
                }

                if (hasBank) {
                    Extension.setXAttr(stub, senderBankToBalance.getValue0(), BALANCE_KEY, Integer.toString(newSenderBankToBalance));

                    List<String> receiverInfo = new ArrayList<>();
                    receiverInfo.add(receiver);
                    receiverInfo.add(Integer.toString(balance));
                    receiverInfo.add(nowDataAndTime);
                    Extension.setXAttr(stub, senderBankToBalance.getValue0(), RECEIVER_KEY, receiverInfo.toString());

                    Extension.setXAttr(stub, currentReceiverBankToBalance.getValue0(), BALANCE_KEY,
                            Integer.toString(currentReceiverBankToBalance.getValue2() + senderBankToBalance.getValue2()));

                    List<String> senderInfo = new ArrayList<>();
                    senderInfo.add(sender);
                    senderInfo.add(Integer.toString(balance));
                    senderInfo.add(nowDataAndTime);
                    Extension.setXAttr(stub, currentReceiverBankToBalance.getValue0(), SENDER_KEY, senderInfo.toString());
                }
                else {
                    Extension.setXAttr(stub, senderBankToBalance.getValue0(), BALANCE_KEY, Integer.toString(newSenderBankToBalance));

                    Map<String, Object> xattr = new HashMap<>();
                    xattr.put(BANK_KEY, senderBankToBalance.getValue1());
                    xattr.put(BALANCE_KEY, balance);
                    xattr.put(SENDER_KEY, new ArrayList<>(Arrays.asList(sender, balance, nowDataAndTime)));

                    String newId;
                    do {
                        long unixTime = Instant.now().getEpochSecond();
                        newId = Long.toString(unixTime);
                    } while (stub.getStringState(newId).length() == 0);

                    Extension.mint(stub, newId, CHECK_TYPE, xattr, null);
                    ERC721.transferFrom(stub, sender, receiver, newId);
                }
                break;
            }
            else {
                boolean hasBank = false;
                Triplet<String, String, Integer> currentReceiverBankToBalance = null;
                for (Triplet<String, String, Integer> receiverBankToBalance: receiverBankToBalances) {
                    hasBank = receiverBankToBalance.getValue1().equals(senderBankToBalance.getValue1());
                    if (hasBank) {
                        currentReceiverBankToBalance = receiverBankToBalance;
                        break;
                    }
                }

                if (hasBank) {
                    Extension.setXAttr(stub, senderBankToBalance.getValue0(), BALANCE_KEY, Integer.toString(0));

                    List<String> receiverInfo = new ArrayList<>();
                    receiverInfo.add(receiver);
                    receiverInfo.add(Integer.toString(balance));
                    receiverInfo.add(nowDataAndTime);
                    Extension.setXAttr(stub, senderBankToBalance.getValue0(), RECEIVER_KEY, receiverInfo.toString());

                    Extension.setXAttr(stub, currentReceiverBankToBalance.getValue0(), BALANCE_KEY,
                            Integer.toString(currentReceiverBankToBalance.getValue2() + senderBankToBalance.getValue2()));

                    List<String> senderInfo = new ArrayList<>();
                    senderInfo.add(sender);
                    senderInfo.add(Integer.toString(balance));
                    senderInfo.add(nowDataAndTime);
                    Extension.setXAttr(stub, currentReceiverBankToBalance.getValue0(), SENDER_KEY, senderInfo.toString());
                }
                else {
                    Extension.setXAttr(stub, senderBankToBalance.getValue0(), BALANCE_KEY, Integer.toString(0));

                    Map<String, Object> xattr = new HashMap<>();
                    xattr.put(BANK_KEY, senderBankToBalance.getValue1());
                    xattr.put(BALANCE_KEY, balance);
                    xattr.put(SENDER_KEY, new ArrayList<>(Arrays.asList(sender, balance, nowDataAndTime)));

                    String newId;
                    do {
                        long unixTime = Instant.now().getEpochSecond();
                        newId = Long.toString(unixTime);
                    } while (stub.getStringState(newId).length() == 0);

                    Extension.mint(stub, newId, CHECK_TYPE, xattr, null);
                    ERC721.transferFrom(stub, sender, receiver, newId);
                }

                break;
            }
        }

        return true;
    }

    boolean redeem(ChaincodeStub stub, String redeemer, String bank, String account, int balance) throws IOException {
        List<String> redeemerIds = Extension.tokenIdsOf(stub, redeemer, CHECK_TYPE);

        String nowDataAndTime = getNowDateAndTime();

        int redeemerTotalBalance = 0;
        for (String redeemerId : redeemerIds) {
            int redeemerBalance = (int) Extension.getXAttr(stub, redeemerId, BALANCE_KEY);
            redeemerTotalBalance += redeemerBalance;
        }

        if (redeemerTotalBalance < balance) {
            return false;
        }

        List<Triplet<String, String, Integer>> redeemerBankToBalances = new LinkedList<>();

        for (String redeemerId : redeemerIds) {
            String redeemerBank = (String) Extension.getXAttr(stub, redeemerId, BANK_KEY);
            if (redeemerBank == null) {
                return false;
            }

            int redeemerBalance = (int) Extension.getXAttr(stub, redeemerId, BALANCE_KEY);

            redeemerBankToBalances.add(new Triplet<>(redeemerId, redeemerBank, redeemerBalance));
        }

        int remainingBalance = balance;
        for (Triplet<String, String, Integer> redeemerBankToBalance: redeemerBankToBalances) {
            if (redeemerBankToBalance.getValue2() <= remainingBalance) {
                Extension.setXAttr(stub, redeemerBankToBalance.getValue0(), BALANCE_KEY, Integer.toString(0));

                List<String> receiverInfo = new ArrayList<>();
                receiverInfo.add(account);
                receiverInfo.add(bank);
                receiverInfo.add(Integer.toString(remainingBalance));
                receiverInfo.add(nowDataAndTime);
                Extension.setXAttr(stub, redeemerBankToBalance.getValue0(), RECEIVER_KEY, receiverInfo.toString());
                remainingBalance -= redeemerBankToBalance.getValue2();
                if (remainingBalance == 0) {
                    break;
                }
            }
            else {
                Extension.setXAttr(stub, redeemerBankToBalance.getValue0(), BALANCE_KEY,
                        Integer.toString(redeemerBankToBalance.getValue2() - remainingBalance));

                List<String> receiverInfo = new ArrayList<>();
                receiverInfo.add(account);
                receiverInfo.add(bank);
                receiverInfo.add(Integer.toString(redeemerBankToBalance.getValue2() - remainingBalance));
                receiverInfo.add(nowDataAndTime);
                Extension.setXAttr(stub, redeemerBankToBalance.getValue0(), RECEIVER_KEY, receiverInfo.toString());
                remainingBalance -= redeemerBankToBalance.getValue2();
                break;
            }
        }
        return true;
    }
}
