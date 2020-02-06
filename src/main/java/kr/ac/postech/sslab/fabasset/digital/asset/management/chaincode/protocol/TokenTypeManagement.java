package kr.ac.postech.sslab.fabasset.digital.asset.management.chaincode.protocol;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.ac.postech.sslab.fabasset.digital.asset.management.chaincode.structure.TokenTypeManager;
import kr.ac.postech.sslab.fabasset.digital.asset.management.chaincode.main.CustomChaincodeBase;
import kr.ac.postech.sslab.fabasset.digital.asset.management.chaincode.user.Address;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.io.IOException;
import java.util.*;

import static kr.ac.postech.sslab.fabasset.digital.asset.management.chaincode.constant.DataType.STRING;
import static kr.ac.postech.sslab.fabasset.digital.asset.management.chaincode.constant.Key.ADMIN_KEY;

public class TokenTypeManagement extends CustomChaincodeBase {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static List<String> tokenTypesOf(ChaincodeStub stub) throws IOException {
        TokenTypeManager manager = TokenTypeManager.read(stub);
        return new ArrayList<>(manager.getTokenTypes().keySet());
    }

    public static boolean enrollTokenType(ChaincodeStub stub, String type, String json) throws IOException {
        String caller = Address.getMyAddress(stub);

        Map<String, List<String>> attributes = objectMapper.readValue(json, new TypeReference<HashMap<String, List<String>>>() {});
        List<String> list = new ArrayList<>(Arrays.asList(STRING, caller));
        attributes.put(ADMIN_KEY, list);
        TokenTypeManager manager = TokenTypeManager.read(stub);
        return manager.addTokenType(stub, type, attributes);
    }

    public static boolean dropTokenType(ChaincodeStub stub, String tokenType) throws IOException {
        String caller = Address.getMyAddress(stub);
        TokenTypeManager manager = TokenTypeManager.read(stub);

        if (!caller.equals(manager.getAdmin(tokenType))) {
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
