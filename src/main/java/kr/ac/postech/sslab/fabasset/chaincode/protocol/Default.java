package kr.ac.postech.sslab.fabasset.chaincode.protocol;

import com.fasterxml.jackson.core.JsonProcessingException;
import kr.ac.postech.sslab.fabasset.chaincode.manager.TokenManager;
import kr.ac.postech.sslab.fabasset.chaincode.client.Address;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyModification;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Default {
    private static final String QUERY_OWNER = "{\"selector\":{\"owner\":\"%s\"}}";

    private Default() {}

    public static boolean mint(ChaincodeStub stub, String id) throws JsonProcessingException {
        String caller = Address.getMyAddress(stub);
        String type = "base";

        TokenManager nft = new TokenManager();

        if (nft.hasToken(stub, id)) {
            return false;
        }

        nft.setId(id);
        nft.setType(type);
        nft.setOwner(caller);
        nft.setApprovee("");
        nft.setXAttr(null);
        nft.setURI(null);
        nft.store(stub);

        ERC721.eventTransfer(stub, "", caller, id);

        return true;
    }

    public static boolean burn(ChaincodeStub stub, String id) throws IOException {
        String caller = Address.getMyAddress(stub);
        String owner = ERC721.ownerOf(stub, id);
        if (!caller.equals(owner)) {
            return false;
        }

        TokenManager nft = TokenManager.load(stub, id);
        nft.delete(stub, id);

        ERC721.eventTransfer(stub, owner, "", id);

        return true;
    }

    public static String getType(ChaincodeStub stub, String id) throws IOException {
        TokenManager nft = TokenManager.load(stub, id);
        return nft.getType();
    }

    public static List<String> tokenIdsOf(ChaincodeStub stub, String owner) {
        String query = String.format(QUERY_OWNER, owner);
        return queryByValues(stub, query);
    }

    public static String query(ChaincodeStub stub, String id) throws IOException {
        TokenManager nft = TokenManager.load(stub, id);
        return nft.toJSONString();
    }

    public static List<String> queryByValues(ChaincodeStub stub, String query) {
        List<String> ids = new ArrayList<>();
        QueryResultsIterator<KeyValue> resultsIterator = stub.getQueryResult(query);
        while(resultsIterator.iterator().hasNext()) {
            String id = resultsIterator.iterator().next().getKey();
            ids.add(id);
        }

        return ids;
    }

    public static List<String> history(ChaincodeStub stub, String id) {
        List<String> histories = new LinkedList<>();
        QueryResultsIterator<KeyModification> resultsIterator = stub.getHistoryForKey(id);
        while (resultsIterator.iterator().hasNext()) {
            histories.add(resultsIterator.iterator().next().getStringValue());
        }

        return histories;
    }
}