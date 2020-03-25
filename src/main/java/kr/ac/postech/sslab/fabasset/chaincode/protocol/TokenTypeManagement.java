package kr.ac.postech.sslab.fabasset.chaincode.protocol;

import kr.ac.postech.sslab.fabasset.chaincode.manager.TokenTypeManager;
import kr.ac.postech.sslab.fabasset.chaincode.client.Address;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.io.IOException;
import java.util.*;

import static kr.ac.postech.sslab.fabasset.chaincode.constant.DataType.STRING;
import static kr.ac.postech.sslab.fabasset.chaincode.constant.Key.ADMIN_KEY;

public class TokenTypeManagement {
    public static List<String> tokenTypesOf(ChaincodeStub stub) throws IOException {
        TokenTypeManager manager = TokenTypeManager.load(stub);
        return new ArrayList<>(manager.getTable().keySet());
    }

    public static boolean enrollTokenType(ChaincodeStub stub, String type, Map<String, List<String>> attributes) throws IOException {
        String caller = Address.getMyAddress(stub);

        TokenTypeManager manager = TokenTypeManager.load(stub);

        if (manager.hasType(type)) {
            return false;
        }

        List<String> info = new ArrayList<>(Arrays.asList(STRING, caller));
        attributes.put(ADMIN_KEY, info);
        manager.addType(type, attributes);
        manager.store(stub);

        return true;
    }

    public static boolean dropTokenType(ChaincodeStub stub, String type) throws IOException {
        String caller = Address.getMyAddress(stub);
        TokenTypeManager manager = TokenTypeManager.load(stub);

        if (!caller.equals(manager.getAttribute(type, ADMIN_KEY).get(1))) {
            return false;
        }

        if (!manager.hasType(type)) {
            return false;
        }

        manager.deleteType(type);
        manager.store(stub);

        return true;
    }

    public static Map<String, List<String>> retrieveTokenType(ChaincodeStub stub, String type) throws IOException {
        TokenTypeManager manager = TokenTypeManager.load(stub);
        if (!manager.hasType(type)) {
            return null;
        }

        return manager.getType(type);
    }

    public static List<String> retrieveAttributeOfTokenType(ChaincodeStub stub, String type, String attribute) throws IOException {
        TokenTypeManager manager = TokenTypeManager.load(stub);
        if (!manager.hasType(type)) {
            return null;
        }

        if (!manager.hasAttribute(type, attribute)) {
            return null;
        }

        return manager.getAttribute(type, attribute);
    }
}
