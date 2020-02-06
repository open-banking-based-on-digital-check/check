package kr.ac.postech.sslab.fabasset.digital.asset.management.chaincode.protocol;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.ac.postech.sslab.fabasset.digital.asset.management.chaincode.structure.TokenTypeManager;
import kr.ac.postech.sslab.fabasset.digital.asset.management.chaincode.main.CustomChaincodeBase;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.io.IOException;
import java.util.*;

public class TokenTypeManagement extends CustomChaincodeBase {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static List<String> tokenTypesOf(ChaincodeStub stub) throws IOException {
        TokenTypeManager manager = TokenTypeManager.read(stub);
        return new ArrayList<>(manager.getTokenTypes().keySet());
    }

    public static boolean enrollTokenType(ChaincodeStub stub, String admin, String type, String json) throws IOException {
        Map<String, List<String>> attributes = objectMapper.readValue(json, new TypeReference<HashMap<String, List<String>>>() {});
        TokenTypeManager manager = TokenTypeManager.read(stub);
        return manager.addTokenType(stub, type, attributes);
    }

    public static boolean dropTokenType(ChaincodeStub stub, String admin, String tokenType) throws IOException {
        TokenTypeManager manager = TokenTypeManager.read(stub);

        if (!admin.equals(manager.getAdmin(tokenType))) {
            return false;
        }

        return manager.removeTokenType(stub, tokenType);
    }

    public static Map<String, List<String>> retrieveTokenType(ChaincodeStub stub, String tokenType) throws IOException {
        TokenTypeManager manager = TokenTypeManager.read(stub);
        return manager.getTokenType(tokenType);
    }

    public static List<String> retrieveAttributeOfTokenType(ChaincodeStub stub, String tokenType, String attribute) throws IOException {
        TokenTypeManager manager = TokenTypeManager.read(stub);
        return manager.getAttributeOfTokenType(tokenType, attribute);
    }
}
