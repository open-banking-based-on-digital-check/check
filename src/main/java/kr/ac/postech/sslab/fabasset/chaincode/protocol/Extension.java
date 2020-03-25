package kr.ac.postech.sslab.fabasset.chaincode.protocol;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;
import kr.ac.postech.sslab.fabasset.chaincode.manager.TokenManager;
import kr.ac.postech.sslab.fabasset.chaincode.manager.TokenTypeManager;
import kr.ac.postech.sslab.fabasset.chaincode.client.Address;
import kr.ac.postech.sslab.fabasset.chaincode.util.DataTypeConversion;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.io.IOException;
import java.util.*;

import static kr.ac.postech.sslab.fabasset.chaincode.constant.Key.*;

public class Extension {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static void eventURI(ChaincodeStub stub, String id, Map<String, String> uri) throws JsonProcessingException {
        String message = String.format("URI of %s: %s", id, objectMapper.writeValueAsString(uri));
        stub.setEvent("URI", ByteString.copyFromUtf8(message).toByteArray());
    }

    private static void eventXAttr(ChaincodeStub stub, String id, Map<String, Object> xattr) throws JsonProcessingException {
        String message = String.format("XAttr of %s: %s", id, objectMapper.writeValueAsString(xattr));
        stub.setEvent("XAttr", ByteString.copyFromUtf8(message).toByteArray());
    }

    public static long balanceOf(ChaincodeStub stub, String owner, String type) {
        return tokenIdsOf(stub, owner, type).size();
    }

    public static List<String> tokenIdsOf(ChaincodeStub stub, String owner, String type) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(OWNER_KEY, owner);
        attributes.put(TYPE_KEY, type);
        return Default.queryByValues(stub, attributes);
    }

    public static boolean mint(ChaincodeStub stub, String id, String type, Map<String, Object> xattr, Map<String, String> uri) throws IOException {
        String caller = Address.getMyAddress(stub);

        TokenManager nft = new TokenManager();

        if (nft.hasToken(stub, id)) {
            return false;
        }

        if (hasInvalidXAttr(stub, type, xattr)) {
            return false;
        }

        configureXAttr(stub, type, xattr);

        if (hasInvalidURI(uri)) {
            return false;
        }

        configureURI(uri);

        nft.setId(id);
        nft.setType(type);
        nft.setOwner(caller);
        nft.setApprovee("");
        nft.setXAttr(xattr);
        nft.setURI(uri);
        nft.store(stub);

        ERC721.eventTransfer(stub, "", caller, id);

        return true;

    }

    public static boolean setURI(ChaincodeStub stub, String id, String index, String value) throws IOException {
        TokenManager nft = TokenManager.load(stub, id);
        Map<String, String> uri = nft.getURI();
        if (!uri.containsKey(index)) {
            return false;
        }

        nft.setURI(index, value);
        nft.store(stub);

        eventURI(stub, id, uri);

        return true;
    }

    public static boolean setURI(ChaincodeStub stub, String id, Map<String, String> uri) throws IOException {
        TokenManager nft = TokenManager.load(stub, id);

        if (hasInvalidURI(uri)) {
            return false;
        }

        configureURI(uri);

        nft.setURI(uri);
        nft.store(stub);

        eventURI(stub, id, uri);

        return true;
    }

    public static String getURI(ChaincodeStub stub, String id, String index) throws IOException {
        TokenManager nft = TokenManager.load(stub, id);
        return nft.getURI(index);
    }

    public static Map<String, String> getURI(ChaincodeStub stub, String id) throws IOException {
        TokenManager nft = TokenManager.load(stub, id);
        return nft.getURI();
    }

    public static boolean setXAttr(ChaincodeStub stub, String id, String index, Object value) throws IOException {
        TokenManager nft = TokenManager.load(stub, id);
        Map<String, Object> xattr = nft.getXAttr();

        if (!xattr.containsKey(index)) {
            return false;
        }

        nft.setXAttr(index, value);
        nft.store(stub);

        eventXAttr(stub, id, xattr);

        return true;
    }

    public static boolean setXAttr(ChaincodeStub stub, String id, Map<String, Object> xattr) throws IOException {
        TokenManager nft = TokenManager.load(stub, id);

        String type = nft.getType();
        if (hasInvalidXAttr(stub, type, xattr)) {
            return false;
        }

        configureXAttr(stub, type, xattr);

        nft.setXAttr(xattr);
        nft.store(stub);

        return true;
    }

    public static Object getXAttr(ChaincodeStub stub, String id, String index) throws IOException {
        TokenManager nft = TokenManager.load(stub, id);
        return nft.getXAttr(index);
    }

    public static Map<String, Object> getXAttr(ChaincodeStub stub, String id) throws IOException {
        TokenManager nft = TokenManager.load(stub, id);
        return nft.getXAttr();
    }

    private static boolean hasInvalidURI(Map<String, String> uri) {
        if (uri == null || uri.keySet().size() == 0) {
            return false;
        }
        else if (uri.keySet().size() == 1) {
            return !uri.containsKey(PATH_KEY) && !uri.containsKey(HASH_KEY);
        }
        else if (uri.keySet().size() == 2) {
            return !uri.containsKey(PATH_KEY) || !uri.containsKey(HASH_KEY);
        }

        return true;
    }

    private static void configureURI(Map<String, String> uri) {
        if (uri == null || uri.keySet().size() == 0) {
            uri = new HashMap<>();
            uri.put(HASH_KEY, "");
            uri.put(PATH_KEY, "");
        }
        else if (uri.keySet().size() == 1) {
            if (uri.containsKey(PATH_KEY)) {
                uri.put(HASH_KEY, "");
            }
            else if (uri.containsKey(HASH_KEY)) {
                uri.put(PATH_KEY, "");
            }
        }
    }

    private static boolean hasInvalidXAttr(ChaincodeStub stub, String type, Map<String, Object> xattr) throws IOException {
        if (xattr == null || xattr.keySet().size() == 0) {
            return false;
        }

        TokenTypeManager manager = TokenTypeManager.load(stub);
        Map<String, List<String>> attributes = manager.getType(type);
        if (attributes == null) {
            return true;
        }

        for (String key : xattr.keySet()) {
            if (!attributes.containsKey(key)) {
                return true;
            }
        }

        return false;
    }

    private static void configureXAttr(ChaincodeStub stub, String type, Map<String, Object> xattr) throws IOException {
        if (xattr == null) {
            xattr = new HashMap<>();
        }

        TokenTypeManager manager = TokenTypeManager.load(stub);
        Map<String, List<String>> attributes = manager.getType(type);

        for (Map.Entry<String, List<String>> attribute : attributes.entrySet()) {
            if (attribute.getKey().equals(ADMIN_KEY)) {
                continue;
            }

            List<String> info = attribute.getValue();

            if (!xattr.containsKey(attribute.getKey())) {
                String dataType = info.get(0);
                Object initialValue = DataTypeConversion.strToDataType(dataType, info.get(1));
                xattr.put(attribute.getKey(), initialValue);
            }
        }
    }
}
