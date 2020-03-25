package kr.ac.postech.sslab.fabasset.chaincode.protocol;

import com.google.protobuf.ByteString;
import kr.ac.postech.sslab.fabasset.chaincode.manager.OperatorManager;
import kr.ac.postech.sslab.fabasset.chaincode.manager.TokenManager;
import kr.ac.postech.sslab.fabasset.chaincode.client.Address;
import org.hyperledger.fabric.shim.ChaincodeStub;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ERC721 {
	static void eventTransfer(ChaincodeStub stub, String from, String to, String id) {
		String message = String.format("Transfer %s: from %s to %s", id, from, to);
		stub.setEvent("Transfer", ByteString.copyFromUtf8(message).toByteArray());
	}

	private static void eventApproval(ChaincodeStub stub, String owner, String approved, String id) {
		String message = String.format("Approval %s: from %s to %s", id, owner, approved);
		stub.setEvent("Approval", ByteString.copyFromUtf8(message).toByteArray());
	}

	private static void eventApprovalForAll(ChaincodeStub stub, String owner, String operator, boolean approved) {
		String message = String.format("ApprovalForAll %b: from %s to %s", approved, owner, operator);
		stub.setEvent("ApprovalForAll", ByteString.copyFromUtf8(message).toByteArray());
	}

	public static long balanceOf(ChaincodeStub stub, String owner) {
		return Default.tokenIdsOf(stub, owner).size();
	}

	public static String ownerOf(ChaincodeStub stub, String id) throws IOException {
		TokenManager nft = TokenManager.load(stub, id);
		return nft.getOwner();
	}

	public static boolean transferFrom(ChaincodeStub stub, String from, String to, String id) throws IOException {
		TokenManager nft = TokenManager.load(stub, id);
		String owner = nft.getOwner();
		if (!from.equals(owner)) {
			return false;
		}

		String caller = Address.getMyAddress(stub);
		String approvee = getApproved(stub, id);
		if (!(caller.equals(owner) || caller.equals(approvee) || isApprovedForAll(stub, owner, caller))) {
			return false;
		}

		nft.setApprovee("");
		nft.setOwner(to);
		nft.store(stub);

		eventTransfer(stub, from, to, id);

		return true;
	}

	public static boolean approve(ChaincodeStub stub, String approved, String id) throws IOException {
		String caller = Address.getMyAddress(stub);
		String owner = ownerOf(stub, id);
		if (!(caller.equals(owner) || isApprovedForAll(stub, owner, caller))) {
			return false;
		}

		TokenManager nft = TokenManager.load(stub, id);
		nft.setApprovee(approved);
		nft.store(stub);

		eventApproval(stub, owner, approved, id);

		return true;
	}

	public static boolean setApprovalForAll(ChaincodeStub stub, String operator, boolean approved) throws IOException {
		OperatorManager manager = OperatorManager.load(stub);
		Map<String, Map<String, Boolean>> table = manager.getTable();

		Map<String, Boolean> map;
		String caller = Address.getMyAddress(stub);
		if (table.containsKey(caller)) {
			map = table.get(caller);
		}
		else {
			map = new HashMap<>();
		}

		map.put(operator, approved);
		table.put(caller, map);

		manager.setTable(table);
		manager.store(stub);

		eventApprovalForAll(stub, caller, operator, approved);

		return true;
	}

    public static String getApproved(ChaincodeStub stub, String id) throws IOException {
		TokenManager nft = TokenManager.load(stub, id);
		return nft.getApprovee();
	}

	public static boolean isApprovedForAll(ChaincodeStub stub, String owner, String operator) throws IOException {
		OperatorManager manager = OperatorManager.load(stub);
		Map<String, Map<String, Boolean>> table = manager.getTable();

		if (table.containsKey(owner)) {
			return table.get(owner).getOrDefault(operator, false);
		}

		return false;
	}
}
