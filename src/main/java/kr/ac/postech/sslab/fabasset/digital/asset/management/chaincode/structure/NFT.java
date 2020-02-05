package kr.ac.postech.sslab.fabasset.digital.asset.management.chaincode.structure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.ac.postech.sslab.fabasset.digital.asset.management.chaincode.constant.Key;
import org.hyperledger.fabric.shim.ChaincodeStub;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class NFT {
    private static ObjectMapper objectMapper = new ObjectMapper();

    private String tokenId;
    private String type;
    private String owner;
    private String approvee;
    private Map<String, Object> xattr;
    private Map<String, String> uri;

    public NFT() {}

    private NFT(String tokenId, String type, String owner, String approvee, Map<String, Object> xattr, Map<String, String> uri) {
        this.tokenId = tokenId;
        this.type = type;
        this.owner = owner;
        this.approvee = approvee;
        this.xattr = xattr;
        this.uri = uri;
    }

    public boolean mint(ChaincodeStub stub, String tokenId, String type, String owner, Map<String, Object> xattr, Map<String, String> uri) throws JsonProcessingException {
        if (stub.getStringState(tokenId).length() != 0) {
            return false;
        }

        this.tokenId = tokenId;
        this.type = type;
        this.owner = owner;
        this.approvee = "";
        this.xattr = xattr;
        this.uri = uri;

        stub.putStringState(this.tokenId, this.toJSONString());
        return true;
    }

    @SuppressWarnings("unchecked")
    public static NFT read(ChaincodeStub stub, String tokenId) throws IOException {
        String json = stub.getStringState(tokenId);

        Map<String, Object> map =
                objectMapper.readValue(json, new TypeReference<HashMap<String, Object>>(){});

        String type = (String) map.get(Key.TYPE_KEY);
        String owner = (String) map.get(Key.OWNER_KEY);
        String approvee = (String) map.get(Key.APPROVEE_KEY);

        Map<String, Object> xattr
                = map.containsKey(Key.XATTR_KEY) ? (HashMap<String, Object>) map.get(Key.XATTR_KEY) : null;

        Map<String, String> uri
                = map.containsKey(Key.URI_KEY) ? (HashMap<String, String>) map.get(Key.URI_KEY) : null;

        return new NFT(tokenId, type, owner, approvee, xattr, uri);
    }

    public boolean burn(ChaincodeStub stub, String tokenId) {
        stub.delState(tokenId);
        return true;
    }

    public String getId() {
        return this.tokenId;
    }

    public String getType() {
        return this.type;
    }

    public boolean setOwner(ChaincodeStub stub, String owner) throws JsonProcessingException {
        this.owner = owner;
        stub.putStringState(this.tokenId, this.toJSONString());
        return true;
    }

    public String getOwner() {
        return this.owner;
    }

    public boolean setApprovee(ChaincodeStub stub, String approvee) throws JsonProcessingException {
        this.approvee = approvee;
        stub.putStringState(this.tokenId, this.toJSONString());
        return true;
    }

    public String getApprovee() {
        return this.approvee;
    }

    public boolean setXAttr(ChaincodeStub stub, String index, Object value) throws JsonProcessingException {
        xattr.put(index, value);
        stub.putStringState(this.tokenId, this.toJSONString());
        return true;
    }

    public Object getXAttr(String index) {
        return xattr.get(index);
    }

    public Map<String, Object> getXAttr() {
        return xattr;
    }

    public boolean setURI(ChaincodeStub stub, String index, String value) throws JsonProcessingException {
        uri.put(index, value);
        stub.putStringState(this.tokenId, this.toJSONString());
        return true;
    }

    public String getURI(String index) {
        return uri.get(index);
    }

    public Map<String, String> getURI() {
        return uri;
    }

    public String toJSONString() throws JsonProcessingException {
        return objectMapper.writeValueAsString(this.toMap());
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(Key.ID_KEY, this.tokenId);
        map.put(Key.TYPE_KEY, this.type);
        map.put(Key.OWNER_KEY, this.owner);
        map.put(Key.APPROVEE_KEY, this.approvee);

        if (this.xattr != null) {
            map.put(Key.XATTR_KEY, xattr);
        }

        if (this.uri != null) {
            map.put(Key.URI_KEY, uri);
        }

        return map;
    }
}
