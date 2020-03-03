package kr.ac.postech.sslab.fabasset.chaincode.manager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hyperledger.fabric.shim.ChaincodeStub;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import static kr.ac.postech.sslab.fabasset.chaincode.constant.Key.*;

public class TokenManager {
    private static ObjectMapper objectMapper = new ObjectMapper();

    // standard attributes
    private String id;
    private String type;
    private String owner;
    private String approvee;

    // extensible attributes
    private Map<String, Object> xattr; // on-chain extensible attribute
    private Map<String, String> uri; // off-chain extensible attribute

    public boolean hasToken(ChaincodeStub stub, String id) {
        return stub.getStringState(id).length() != 0;
    }

    public void store(ChaincodeStub stub) throws JsonProcessingException {
        stub.putStringState(this.id, this.toJSONString());
    }

    public void delete(ChaincodeStub stub, String id) {
        stub.delState(id);
    }

    @SuppressWarnings("unchecked")
    public static TokenManager load(ChaincodeStub stub, String id) throws IOException {
        TokenManager nft = new TokenManager();
        String json = stub.getStringState(id);

        Map<String, Object> map = objectMapper.readValue(json,
                new TypeReference<HashMap<String, Object>>(){});

        nft.setId(id);

        String type = (String) map.get(TYPE_KEY);
        nft.setType(type);

        String owner = (String) map.get(OWNER_KEY);
        nft.setOwner(owner);

        String approvee = (String) map.get(APPROVEE_KEY);
        nft.setApprovee(approvee);

        Map<String, Object> xattr
                = map.containsKey(XATTR_KEY) ? (HashMap<String, Object>) map.get(XATTR_KEY) : null;
        nft.setXAttr(xattr);

        Map<String, String> uri
                = map.containsKey(URI_KEY) ? (HashMap<String, String>) map.get(URI_KEY) : null;
        nft.setURI(uri);

        return nft;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getApprovee() {
        return approvee;
    }

    public void setApprovee(String approvee) {
        this.approvee = approvee;
    }

    public Map<String, Object> getXAttr() {
        return xattr;
    }

    public Object getXAttr(String index) {
        return xattr.get(index);
    }

    public void setXAttr(Map<String, Object> xattr) {
        this.xattr = xattr;
    }

    public void setXAttr(String index, Object value) {
        xattr.put(index, value);
    }

    public Map<String, String> getURI() {
        return uri;
    }

    public String getURI(String index) {
        return uri.get(index);
    }

    public void setURI(Map<String, String> uri) {
        this.uri = uri;
    }

    public void setURI(String index, String value) {
        uri.put(index, value);
    }

    public String toJSONString() throws JsonProcessingException {
        return objectMapper.writeValueAsString(this.toMap());
    }

    private Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(ID_KEY, this.id);
        map.put(TYPE_KEY, this.type);
        map.put(OWNER_KEY, this.owner);
        map.put(APPROVEE_KEY, this.approvee);

        if (this.xattr != null) {
            map.put(XATTR_KEY, xattr);
        }

        if (this.uri != null) {
            map.put(URI_KEY, uri);
        }

        return map;
    }
}
