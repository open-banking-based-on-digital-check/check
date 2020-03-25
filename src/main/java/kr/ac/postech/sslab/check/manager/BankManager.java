package kr.ac.postech.sslab.check.manager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BankManager {
    private static ObjectMapper objectMapper = new ObjectMapper();
    private static final String REGISTERED_BANKS = "REGISTERED_BANKS";

    private List<String> banks;

    public static BankManager load(ChaincodeStub stub) throws IOException {
        String list = stub.getStringState(REGISTERED_BANKS);

        List<String> bank;
        if (list.length() == 0) {
            bank = new ArrayList<>();
        }
        else {
            bank = objectMapper.readValue(list, new TypeReference<ArrayList<String>>() {});
        }

        BankManager bankManager = new BankManager();
        bankManager.setBanks(bank);

        return bankManager;
    }

    public void store(ChaincodeStub stub) throws JsonProcessingException {
        stub.putStringState(REGISTERED_BANKS, objectMapper.writeValueAsString(banks));
    }

    public void setBanks(List<String> banks) {
        this.banks = banks;
    }

    public List<String> getBanks() {
        return banks;
    }

    public boolean hasBank(String bank) {
        for (String b: banks) {
            if (b.equals(bank)) {
                return true;
            }
        }

        return false;
    }
}
