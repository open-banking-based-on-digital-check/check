package kr.ac.postech.sslab.fabasset.digital.asset.management.chaincode.structure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static kr.ac.postech.sslab.fabasset.digital.asset.management.chaincode.constant.Key.OPERATORS_APPROVAL;

public class OperatorsApproval {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private OperatorsApproval(Map<String, Map<String, Boolean>> operators) {
        this.operators = operators;
    }

    private Map<String, Map<String, Boolean>> operators;

    public static OperatorsApproval read(ChaincodeStub stub) throws IOException {
        String json = stub.getStringState(OPERATORS_APPROVAL);
        if (json.trim().length() == 0) {
            return new OperatorsApproval(new HashMap<>());
        }
        else {
            Map<String, Map<String, Boolean>> map
                    = objectMapper.readValue(json, new TypeReference<HashMap<String, HashMap<String, Boolean>>>() {});
            return new OperatorsApproval(map);
        }
    }

    public Map<String, Map<String, Boolean>> getOperatorsApproval() {
        return operators;
    }

    public void setOperatorsApproval(ChaincodeStub stub, Map<String, Map<String, Boolean>> operators) throws JsonProcessingException {
        this.operators = operators;
        stub.putStringState(OPERATORS_APPROVAL, toJSONString());
    }

    private String toJSONString() throws JsonProcessingException {
        return objectMapper.writeValueAsString(operators);
    }
}
