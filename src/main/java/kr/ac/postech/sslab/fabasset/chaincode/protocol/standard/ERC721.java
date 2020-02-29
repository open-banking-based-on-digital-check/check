package kr.ac.postech.sslab.fabasset.chaincode.protocol.standard;

import com.google.protobuf.ByteString;
import kr.ac.postech.sslab.fabasset.chaincode.main.CustomChaincodeBase;
import kr.ac.postech.sslab.fabasset.chaincode.manager.OperatorManager;
import kr.ac.postech.sslab.fabasset.chaincode.manager.TokenManager;
import kr.ac.postech.sslab.fabasset.chaincode.client.Address;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ERC721 extends CustomChaincodeBase {
	private static final String QUERY_OWNER = "{\"selector\":{\"owner\":\"%s\"}}";

	public static void eventTransfer(ChaincodeStub stub, String from, String to, String tokenId) {
		String message = String.format("Client %s transfers NFT %s to Client %s", from, tokenId, to);
		stub.setEvent("Transfer", ByteString.copyFromUtf8(message).toByteArray());
	}

	public static void eventApproval(ChaincodeStub stub, String owner, String approved, String tokenId) {
		String message = String.format("Client %s approves NFT %s to Client %s", owner, tokenId, approved);
		stub.setEvent("Approval", ByteString.copyFromUtf8(message).toByteArray());
	}

	public static void eventApprovalForAll(ChaincodeStub stub, String owner, String operator, boolean approved) {
		String message = String.format("Client %s %s Client %s to be an operator",
									owner, approved ? "enables" : "disables", operator);
		stub.setEvent("ApprovalForAll", ByteString.copyFromUtf8(message).toByteArray());
	}

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

		String caller = Address.getMyAddress(stub);
		String approvee = getApproved(stub, tokenId);
		if ( !(caller.equals(owner) || caller.equals(approvee) || isApprovedForAll(stub, owner, caller)) ) {
			return false;
		}

		nft.setApprovee(stub, "");
		nft.setOwner(stub, to);

		eventTransfer(stub, from, to, tokenId);

		return true;
	}

	public static boolean approve(ChaincodeStub stub, String approved, String tokenId) throws IOException {
		String caller = Address.getMyAddress(stub);
		String owner = ownerOf(stub, tokenId);
		if ( !(caller.equals(owner) || isApprovedForAll(stub, owner, caller)) ) {
			return false;
		}

		TokenManager nft = TokenManager.read(stub, tokenId);
		nft.setApprovee(stub, approved);

		eventApproval(stub, owner, approved, tokenId);

		return true;
	}

	public static boolean setApprovalForAll(ChaincodeStub stub, String operator, boolean approved) throws IOException {
		OperatorManager approval = OperatorManager.read(stub);
		Map<String, Map<String, Boolean>> operators = approval.getOperatorsApproval();

		Map<String, Boolean> map;
		String caller = Address.getMyAddress(stub);
		if (operators.containsKey(caller)) {
			map = operators.get(caller);
		}
		else {
			map = new HashMap<>();
		}

		map.put(operator, approved);
		operators.put(caller, map);

		approval.setOperatorsApproval(stub, operators);

		eventApprovalForAll(stub, caller, operator, approved);

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
