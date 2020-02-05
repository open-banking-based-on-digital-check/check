package kr.ac.postech.sslab.fabasset.digital.asset.management.chaincode.structure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.io.IOException;
import java.util.*;

import static kr.ac.postech.sslab.fabasset.digital.asset.management.chaincode.constant.Key.TOKEN_TYPES;
import static kr.ac.postech.sslab.fabasset.digital.asset.management.chaincode.constant.Key.ADMIN_KEY;

public class TokenTypeManager {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    //Map<token type, Map<attribute, [data type, initial value]>>
    private Map<String, Map<String, List<String>>> tokenTypes;

    private TokenTypeManager(Map<String, Map<String, List<String>>> tokenTypes) {
        this.tokenTypes = tokenTypes;
    }

    public static TokenTypeManager read(ChaincodeStub stub) throws IOException {
        String json = stub.getStringState(TOKEN_TYPES);
        if (json.trim().length() == 0) {
            return new TokenTypeManager(new HashMap<>());
        }
        else {
            Map<String, Map<String, List<String>>> map
                    = objectMapper.readValue(json, new TypeReference<HashMap<String, HashMap<String, List<String>>>>() {});
            return new TokenTypeManager(map);
        }
    }

    public Map<String, Map<String, List<String>>> getTokenTypes() {
        return tokenTypes;
    }

    public void setTokenTypes(ChaincodeStub stub, Map<String, Map<String, List<String>>> tokenTypes) throws JsonProcessingException {
        this.tokenTypes = tokenTypes;
        stub.putStringState(TOKEN_TYPES, toJSONString());
    }

    public boolean hashTokenType(String tokenType) {
        return tokenTypes.containsKey(tokenType);
    }

    public boolean addTokenType(ChaincodeStub stub, String tokenType, Map<String, List<String>> attributes) throws JsonProcessingException {
        if (hashTokenType(tokenType)) {
            return false;
        }

        tokenTypes.put(tokenType, attributes);
        stub.putStringState(TOKEN_TYPES, toJSONString());
        return true;
    }

    public boolean removeTokenType(ChaincodeStub stub, String tokenType) throws JsonProcessingException {
        if (!hashTokenType(tokenType)) {
            return false;
        }

        tokenTypes.remove(tokenType);
        stub.putStringState(TOKEN_TYPES, toJSONString());
        return true;
    }

    public Map<String, List<String>> getTokenType(String tokenType) {
        if (!hashTokenType(tokenType)) {
            return null;
        }

        return tokenTypes.get(tokenType);
    }

    public boolean setTokenType(ChaincodeStub stub, String tokenType, Map<String, List<String>> attributes) throws JsonProcessingException {
        if (!hashTokenType(tokenType)) {
            return false;
        }

        tokenTypes.put(tokenType, attributes);
        stub.putStringState(TOKEN_TYPES, toJSONString());
        return true;
    }

    public boolean hasAttribute(String tokenType, String attribute) {
        return tokenTypes.get(tokenType).containsKey(attribute);
    }

    public boolean addAttributeOfTokenType(ChaincodeStub stub, String tokenType, String attribute, String dataType, String initialValue) throws JsonProcessingException {
        if (!hashTokenType(tokenType)) {
            return false;
        }

        if (hasAttribute(tokenType, attribute)) {
            return false;
        }

        List<String> pair = new ArrayList<>(Arrays.asList(dataType, initialValue));
        tokenTypes.get(tokenType).put(attribute, pair);
        stub.putStringState(TOKEN_TYPES, toJSONString());
        return true;
    }

    public boolean removeAttributeOfTokenType(ChaincodeStub stub, String tokenType, String attribute) throws JsonProcessingException {
        if (!hashTokenType(tokenType)) {
            return false;
        }

        if (!hasAttribute(tokenType, attribute)) {
            return false;
        }

        tokenTypes.get(tokenType).remove(attribute);
        stub.putStringState(TOKEN_TYPES, toJSONString());
        return true;
    }

    public boolean setAttributeOfTokenType(ChaincodeStub stub, String tokenType, String attribute, List<String> pair) throws JsonProcessingException {
        if (!hashTokenType(tokenType)) {
            return false;
        }

        if (!hasAttribute(tokenType, attribute)) {
            return false;
        }

        tokenTypes.get(tokenType).put(attribute, pair);
        stub.putStringState(TOKEN_TYPES, toJSONString());
        return true;
    }

    public List<String> getAttributeOfTokenType(String tokenType, String attribute) {
        if (!hashTokenType(tokenType)) {
            return new ArrayList<>();
        }

        if (!hasAttribute(tokenType, attribute)) {
            return new ArrayList<>();
        }

        return tokenTypes.get(tokenType).get(attribute);
    }

    public String getAdmin(String tokenType) {
        List<String> pair = getAttributeOfTokenType(tokenType, ADMIN_KEY);

        if (pair.isEmpty()) {
            return "";
        }

        return pair.get(1);
    }

    private String toJSONString() throws JsonProcessingException {
        return objectMapper.writeValueAsString(tokenTypes);
    }
}
