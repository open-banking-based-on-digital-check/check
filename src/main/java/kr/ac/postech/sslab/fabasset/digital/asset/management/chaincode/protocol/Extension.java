package kr.ac.postech.sslab.fabasset.digital.asset.management.chaincode.protocol;

import kr.ac.postech.sslab.fabasset.digital.asset.management.chaincode.constant.DataType;
import kr.ac.postech.sslab.fabasset.digital.asset.management.chaincode.constant.Message;
import kr.ac.postech.sslab.fabasset.digital.asset.management.chaincode.main.CustomChaincodeBase;
import kr.ac.postech.sslab.fabasset.digital.asset.management.chaincode.structure.TokenManager;
import kr.ac.postech.sslab.fabasset.digital.asset.management.chaincode.structure.TokenTypeManager;
import kr.ac.postech.sslab.fabasset.digital.asset.management.chaincode.util.DataTypeConversion;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyModification;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import java.io.IOException;
import java.util.*;

import static kr.ac.postech.sslab.fabasset.digital.asset.management.chaincode.constant.DataType.*;
import static kr.ac.postech.sslab.fabasset.digital.asset.management.chaincode.constant.Message.NO_ATTRIBUTE_MESSAGE;
import static kr.ac.postech.sslab.fabasset.digital.asset.management.chaincode.constant.Message.NO_TOKEN_TYPE_MESSAGE;

public class Extension extends CustomChaincodeBase {
    private static final Log LOG = LogFactory.getLog(Extension.class);

    private static final String BASE_TYPE = "base";

    private static final String QUERY_OWNER_AND_TYPE = "{\"selector\":{\"owner\":\"%s\",\"type\":\"%s\"}}";

    public static long balanceOf(ChaincodeStub stub, String owner, String type) throws IOException {
        String query = String.format(QUERY_OWNER_AND_TYPE, owner, type);

        long ownedTokensCount = 0;
        QueryResultsIterator<KeyValue> resultsIterator = stub.getQueryResult(query);
        while(resultsIterator.iterator().hasNext()) {
            resultsIterator.iterator().next();
            ownedTokensCount++;
        }

        return ownedTokensCount;
    }

    public static List<String> tokenIdsOf(ChaincodeStub stub, String owner, String type) throws IOException {
        String query = String.format(QUERY_OWNER_AND_TYPE, owner, type);

        List<String> tokenIds = new ArrayList<>();
        QueryResultsIterator<KeyValue> resultsIterator = stub.getQueryResult(query);
        while(resultsIterator.iterator().hasNext()) {
            String tokenId = resultsIterator.iterator().next().getKey();
            tokenIds.add(tokenId);
        }

        return tokenIds;
    }

    public static String query(ChaincodeStub stub, String tokenId) throws IOException {
        TokenManager nft = TokenManager.read(stub, tokenId);
        return nft.toJSONString();
    }

    public static List<String> queryHistory(ChaincodeStub stub, String tokenId) {
        List<String> histories = new LinkedList<>();
        QueryResultsIterator<KeyModification> resultsIterator = stub.getHistoryForKey(tokenId);
        while (resultsIterator.iterator().hasNext()) {
            histories.add(resultsIterator.iterator().next().getStringValue());
        }

        return histories;
    }

    public static boolean mint(ChaincodeStub stub, String tokenId, String type, String owner, Map<String, Object> xattr, Map<String, String> uri) throws IOException {
        TokenTypeManager manager = TokenTypeManager.read(stub);
        Map<String, List<String>> attributes = manager.getTokenType(type);
        if (attributes == null) {
            LOG.error(NO_TOKEN_TYPE_MESSAGE);
            return false;
        }

        if (xattr == null) {
            xattr = new HashMap<>();
        }

        if (!haveValidAttributes(xattr, attributes)) {
            return false;
        }

        if (!addNoInputAttributes(xattr, attributes)) {
            return false;
        }

        if (!haveValidURIAttributes(uri)) {
            return false;
        }

        TokenManager nft = new TokenManager();
        return nft.mint(stub, tokenId, type, owner, xattr, uri);
    }

    private static boolean haveValidAttributes(Map<String, Object> xattr, Map<String, List<String>> attributes) {
        for (String key : xattr.keySet()) {
            if (!attributes.containsKey(key)) {
                LOG.error(NO_ATTRIBUTE_MESSAGE);
                return false;
            }
        }

        return true;
    }

    private static boolean addNoInputAttributes(Map<String, Object> xattr, Map<String, List<String>> attributes) {
        for (Map.Entry<String, List<String>> entry : attributes.entrySet()) {
            if(!insertNewEntry(entry.getKey(), entry.getValue(), xattr)) {
                return false;
            }
        }

        return true;
    }

    private static boolean insertNewEntry(String attribute, List<String> pair, Map<String, Object> xattr) {
        if (!xattr.containsKey(attribute)) {
            if (pair.size() != 2) {
                return false;
            }

            String dataType = pair.get(0);
            String initialValueStr = pair.get(1);
            Object initialValue = DataTypeConversion.strToDataType(dataType, initialValueStr);
            if (initialValue == null) {
                return false;
            }

            xattr.put(attribute, initialValue);
        }

        return true;
    }

    private static boolean haveValidURIAttributes(Map<String, String> uri) {
        return uri == null || (uri.keySet().size() == 2
                && uri.containsKey("path") && uri.containsKey("hash"));
    }

    public static boolean setURI(ChaincodeStub stub, String tokenId, String index, String value) throws IOException {
        TokenManager nft = TokenManager.read(stub, tokenId);
        Map<String, String> uri = nft.getURI();
        if (!uri.containsKey(index)) {
            return false;
        }

        return nft.setURI(stub, index, value);
    }

    public static String getURI(ChaincodeStub stub, String tokenId, String index) throws IOException {
        TokenManager nft = TokenManager.read(stub, tokenId);
        Map<String, String> uri = nft.getURI();
        if (!uri.containsKey(index)) {
            return null;
        }

        return nft.getURI(index);
    }

    public static boolean setXAttr(ChaincodeStub stub, String tokenId, String index, String value) throws IOException {
        TokenManager nft = TokenManager.read(stub, tokenId);
        Map<String, Object> xattr = nft.getXAttr();
        if (!xattr.containsKey(index)) {
            return false;
        }

        TokenTypeManager manager = TokenTypeManager.read(stub);
        List<String> attr = manager.getAttributeOfTokenType(nft.getType(), index);
        String dataType = attr.get(0);
        Object object = DataTypeConversion.strToDataType(dataType, value);
        if (object == null) {
            return false;
        }

        nft.setXAttr(stub, index, object);

        return true;
    }

    @SuppressWarnings("unchecked")
    public static String getXAttr(ChaincodeStub stub, String tokenId, String index) throws IOException {
        TokenManager nft = TokenManager.read(stub, tokenId);
        Map<String, Object> xattr = nft.getXAttr();
        if (!xattr.containsKey(index)) {
            return null;
        }

        Object value = nft.getXAttr(index);

        TokenTypeManager manager = TokenTypeManager.read(stub);
        List<String> attr = manager.getAttributeOfTokenType(nft.getType(), index);

        if (attr.isEmpty()) {
            return null;
        }

        switch (attr.get(0)) {
            case DataType.INTEGER:
                return Integer.toString((int) value);

            case DataType.DOUBLE:
                return Double.toString((double) value);

            case DataType.BYTE:
                return Byte.toString((byte) value);

            case DataType.STRING:
                return (String) value;

            case DataType.BOOLEAN:
                return Boolean.toString((boolean) value);

            case LIST_INTEGER:
                List<Integer> integers = (List<Integer>) value;
                return integers != null ? integers.toString() : null;

            case LIST_DOUBLE:
                List<Double> doubles = (List<Double>) value;
                return doubles != null ? doubles.toString() : null;

            case DataType.LIST_BYTE:
                List<Byte> bytes = (List<Byte>) value;
                return bytes != null ? bytes.toString() : null;

            case DataType.LIST_STRING:
                List<String> strings = (List<String>) value;
                return strings != null ? strings.toString() : null;

            case DataType.LIST_BOOLEAN:
                List<Boolean> booleans = (List<Boolean>) value;
                return booleans != null ? booleans.toString() : null;

            default:
                LOG.error(Message.NO_DATA_TYPE_MESSAGE);
                return null;
        }
    }
}
