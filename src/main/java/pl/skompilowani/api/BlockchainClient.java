package pl.skompilowani.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.DefaultBlockParameter;
import java.io.IOException;
import java.math.BigInteger;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import java.util.Optional;

public class BlockchainClient {
    // Logger dla warstwy dostępu
    private static final Logger logger = LoggerFactory.getLogger(BlockchainClient.class);

    private final Web3j web3j;

    public BlockchainClient(String rpcUrl) {
        this.web3j = Web3j.build(new HttpService(rpcUrl));
    }


    // Weryfikacja czy sieć odpowiada
    public boolean checkNetworkStatus() {
        try {
            String clientVersion = web3j.web3ClientVersion().send().getWeb3ClientVersion();
            if (clientVersion != null && !clientVersion.isEmpty()) {
                logger.debug("Pomyślnie połączono z węzłem. Wersja klienta: {}", clientVersion);
                return true;
            }
            return false;
        } catch (IOException e) {
            logger.error("Błąd sieci: Nie można nawiązać połączenia z siecią Sepolia. Sprawdź połączenie z Internetem lub poprawność adresu URL. Szczegóły: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            // Inne nieoczekiwane wyjątki
            logger.error("Nieoczekiwany błąd podczas sprawdzania statusu sieci: {}", e.getMessage());
            return false;
        }
    }

    // Pobiera numer najnowszego bloku
    public BigInteger getLatestBlockNumber() throws IOException {
        return web3j.ethBlockNumber().send().getBlockNumber();
    }

    // Pobiera szczegóły konkretnego bloku (Numer, Hash, Liczba transakcji)
    public EthBlock.Block getBlockDetails(BigInteger blockNumber) throws IOException {
        return web3j.ethGetBlockByNumber(
                DefaultBlockParameter.valueOf(blockNumber),
                true
        ).send().getBlock();
    }

    // Pobiera paragon transakcji, który zawiera informacje o faktycznym zużyciu Gasu
    public Optional<TransactionReceipt> getTransactionReceipt(String transactionHash) throws IOException {
        return web3j.ethGetTransactionReceipt(transactionHash).send().getTransactionReceipt();
    }
}