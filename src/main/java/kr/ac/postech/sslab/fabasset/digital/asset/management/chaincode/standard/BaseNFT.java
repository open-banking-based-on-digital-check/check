package kr.ac.postech.sslab.fabasset.digital.asset.management.chaincode.standard;

import com.fasterxml.jackson.core.JsonProcessingException;
import kr.ac.postech.sslab.fabasset.digital.asset.management.chaincode.structure.NFT;
import org.hyperledger.fabric.shim.ChaincodeStub;
import java.io.IOException;

public class BaseNFT {
    private BaseNFT() {}

    public static boolean mint(ChaincodeStub stub, String tokenId, String owner) throws JsonProcessingException {
        NFT nft = new NFT();
        String type = "base";
        return nft.mint(stub, tokenId, type, owner, null, null);
    }

    public static boolean burn(ChaincodeStub stub, String tokenId) throws IOException {
        NFT nft = NFT.read(stub, tokenId);
        return nft.burn(stub, tokenId);
    }

    public static String getType(ChaincodeStub stub, String tokenId) throws IOException {
        NFT nft = NFT.read(stub, tokenId);
        return nft.getType();
    }
}