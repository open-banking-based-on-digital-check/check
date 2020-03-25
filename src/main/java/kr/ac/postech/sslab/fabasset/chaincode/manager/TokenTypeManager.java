package kr.ac.postech.sslab.fabasset.chaincode.manager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.io.IOException;
import java.util.*;

import static kr.ac.postech.sslab.fabasset.chaincode.constant.Key.TOKEN_TYPES;

public class TokenTypeManager {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Map<token type, Map<attribute, [data type, initial value]>>
    private Map<String, Map<String, List<String>>> table;

    public static TokenTypeManager load(ChaincodeStub stub) throws IOException {
        String json = stub.getStringState(TOKEN_TYPES);
        TokenTypeManager manager = new TokenTypeManager();
        Map<String, Map<String, List<String>>> table;
        if (json.trim().length() == 0) {
            table = new HashMap<>();
        }
        else {
            table = objectMapper.readValue(json,
                    new TypeReference<HashMap<String, HashMap<String, List<String>>>>(){});
        }

        manager.setTable(table);

        return manager;
    }


    public void store(ChaincodeStub stub) throws JsonProcessingException {
        stub.putStringState(TOKEN_TYPES, toJSONString());
    }

    public Map<String, Map<String, List<String>>> getTable() {
        return table;
    }

    private void setTable(Map<String, Map<String, List<String>>> table) {
        this.table = table;
    }

    public boolean hasType(String type) {
        return table.containsKey(type);
    }

    public void addType(String type, Map<String, List<String>> attributes) {
        table.put(type, attributes);
    }

    public void deleteType(String type) {
        table.remove(type);
    }

    public Map<String, List<String>> getType(String type) {
        return table.get(type);
    }

    public boolean hasAttribute(String tokenType, String attribute) {
        return table.get(tokenType).containsKey(attribute);
    }

    public List<String> getAttribute(String type, String attribute) {
        return table.get(type).get(attribute);
    }

    private String toJSONString() throws JsonProcessingException {
        return objectMapper.writeValueAsString(table);
    }
}
