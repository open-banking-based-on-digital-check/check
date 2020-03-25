package kr.ac.postech.sslab.check.protocol;

import kr.ac.postech.sslab.check.manager.BankManager;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.io.IOException;
import java.util.List;

public class BankManagement {
    private BankManagement() {}

    public static boolean registerBank(ChaincodeStub stub, String bank) throws IOException {
        BankManager bankManager = BankManager.load(stub);
        List<String> banks = bankManager.getBanks();

        if (bankManager.hasBank(bank)) {
            return false;
        }

        banks.add(bank);
        bankManager.setBanks(banks);
        bankManager.store(stub);

        return true;
    }

    public static List<String> retrieveRegisteredBanks(ChaincodeStub stub) throws IOException {
        BankManager bankManager = BankManager.load(stub);
        return bankManager.getBanks();
    }
}
