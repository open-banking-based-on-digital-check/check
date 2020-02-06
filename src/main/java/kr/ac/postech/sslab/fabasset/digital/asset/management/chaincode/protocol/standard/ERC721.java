package kr.ac.postech.sslab.fabasset.digital.asset.management.chaincode.protocol.standard;

import kr.ac.postech.sslab.fabasset.digital.asset.management.chaincode.main.CustomChaincodeBase;
import kr.ac.postech.sslab.fabasset.digital.asset.management.chaincode.structure.TokenManager;
import kr.ac.postech.sslab.fabasset.digital.asset.management.chaincode.structure.OperatorManager;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ERC721 extends CustomChaincodeBase {
	private static final String QUERY_OWNER = "{\"selector\":{\"owner\":\"%s\"}}";

	public static long balanceOf(ChaincodeStub stub, String owner) {
		String query = String.format(QUERY_OWNER, owner);

		long ownedTokensCount = 0;
		QueryResultsIterator<KeyValue> resultsIterator = stub.getQueryResult(query);
		while(resultsIterator.iterator().hasNext()) {
			resultsIterator.iterator().next();
			ownedTokensCount++;
		}

		return ownedTokensCount;
	}

	public static String ownerOf(ChaincodeStub stub, String tokenId) throws IOException {
		TokenManager nft = TokenManager.read(stub, tokenId);
		return nft.getOwner();
	}

	public static boolean transferFrom(ChaincodeStub stub, String from, String to, String tokenId) throws IOException {
			TokenManager nft = TokenManager.read(stub, tokenId);

			String owner = nft.getOwner();
			if (!from.equals(owner)) {
				return false;
			}

			nft.setApprovee(stub, "");
			nft.setOwner(stub, to);
			return true;
	}

	public static boolean approve(ChaincodeStub stub, String approved, String tokenId) throws IOException {
		TokenManager nft = TokenManager.read(stub, tokenId);
		return nft.setApprovee(stub, approved);
	}

	public static boolean setApprovalForAll(ChaincodeStub stub, String caller, String operator, boolean approved) throws IOException {
		OperatorManager approval = OperatorManager.read(stub);
		Map<String, Map<String, Boolean>> operators = approval.getOperatorsApproval();

		Map<String, Boolean> map;
		if (operators.containsKey(caller)) {
			map = operators.get(caller);
		}
		else {
			map = new HashMap<>();
		}

		map.put(operator, approved);
		operators.put(caller, map);

		approval.setOperatorsApproval(stub, operators);
		return true;
	}

    public static String getApproved(ChaincodeStub stub, String tokenId) throws IOException {
		TokenManager nft = TokenManager.read(stub, tokenId);
		return nft.getApprovee();
	}

	public static boolean isApprovedForAll(ChaincodeStub stub, String owner, String operator) throws IOException {
		OperatorManager approval = OperatorManager.read(stub);
		Map<String, Map<String, Boolean>> operators = approval.getOperatorsApproval();

		if (operators.containsKey(owner)) {
			return operators.get(owner).getOrDefault(operator, false);
		}
		else {
			return false;
		}
	}
}
