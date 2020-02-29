package kr.ac.postech.sslab.fabasset.chaincode.manager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.ac.postech.sslab.fabasset.chaincode.constant.Key;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class OperatorManager {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private OperatorManager(Map<String, Map<String, Boolean>> operators) {
        this.operators = operators;
    }

    private Map<String, Map<String, Boolean>> operators;

    public static OperatorManager read(ChaincodeStub stub) throws IOException {
        String json = stub.getStringState(Key.OPERATORS_APPROVAL);
        if (json.trim().length() == 0) {
            return new OperatorManager(new HashMap<>());
        }
        else {
            Map<String, Map<String, Boolean>> map
                    = objectMapper.readValue(json, new TypeReference<HashMap<String, HashMap<String, Boolean>>>() {});
            return new OperatorManager(map);
        }
    }

    public Map<String, Map<String, Boolean>> getOperatorsApproval() {
        return operators;
    }

    public void setOperatorsApproval(ChaincodeStub stub, Map<String, Map<String, Boolean>> operators) throws JsonProcessingException {
        this.operators = operators;
        stub.putStringState(Key.OPERATORS_APPROVAL, toJSONString());
    }

    private String toJSONString() throws JsonProcessingException {
        return objectMapper.writeValueAsString(operators);
    }
}
