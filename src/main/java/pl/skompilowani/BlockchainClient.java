package pl.skompilowani;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.DefaultBlockParameter;
import java.io.IOException;
import java.math.BigInteger;

public class BlockchainClient {
    private final Web3j web3j;

    public BlockchainClient(String rpcUrl) {
        // Inicjalizacja biblioteki Web3j przy użyciu adresu z .env
        this.web3j = Web3j.build(new HttpService(rpcUrl));
    }

    // Pobiera numer najnowszego bloku (wymóg MVP)
    public BigInteger getLatestBlockNumber() throws IOException {
        return web3j.ethBlockNumber().send().getBlockNumber();
    }

    // Pobiera szczegóły konkretnego bloku (Numer, Hash, Liczba transakcji - wymóg MVP)
    public EthBlock.Block getBlockDetails(BigInteger blockNumber) throws IOException {
        return web3j.ethGetBlockByNumber(
                DefaultBlockParameter.valueOf(blockNumber),
                true
        ).send().getBlock();
    }
}