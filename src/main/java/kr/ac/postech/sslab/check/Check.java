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

    boolean issue(ChaincodeStub stub, String bank, String issuer, int balance) throws IOException {
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

            int currentBalance = Integer.parseInt(currentBalanceString);
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
        List<String> senderIds = Extension.tokenIdsOf(stub, sender, BANK_KEY);
        List<String> receiverIds = Extension.tokenIdsOf(stub, receiver, BANK_KEY);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DATE_AND_TIME_FORMAT);
        LocalDateTime now = LocalDateTime.now();
        String nowDataAndTime = dtf.format(now);

        int senderTotalBalance = 0;
        for (String senderId : senderIds) {
            String senderBalanceString = Extension.getXAttr(stub, senderId, BALANCE_KEY);
            if (senderBalanceString == null) {
                return false;
            }
            int senderBalance = Integer.parseInt(senderBalanceString);
            senderTotalBalance += senderBalance;
        }

        if (senderTotalBalance < balance) {
            return false;
        }

        // id bank balance
        List<Triplet<String, String, Integer>> senderBankToBalances = new LinkedList<>();

        for (String senderId : senderIds) {
            String senderBank = Extension.getXAttr(stub, senderId, BANK_KEY);
            if (senderBank == null) {
                return false;
            }

            String senderBalanceString = Extension.getXAttr(stub, senderId, BALANCE_KEY);
            if (senderBalanceString == null) {
                return false;
            }

            int senderBalance = Integer.parseInt(senderBalanceString);

            senderBankToBalances.add(new Triplet<>(senderId, senderBank, senderBalance));
        }

        senderBankToBalances.sort(new BalanceComparator());

        List<Triplet<String, String, Integer>> receiverBankToBalances = new LinkedList<>();

        for (String receiverId : receiverIds) {
            String receiverBank = Extension.getXAttr(stub, receiverId, BANK_KEY);
            if (receiverBank == null) {
                return false;
            }

            String receiverBalanceString = Extension.getXAttr(stub, receiverId, BALANCE_KEY);
            if (receiverBalanceString == null) {
                return false;
            }

            int receiverBalance = Integer.parseInt(receiverBalanceString);

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

    boolean redeem(ChaincodeStub stub, String redeemer, String bank, String account, int balance) {
        return true;
    }
}
