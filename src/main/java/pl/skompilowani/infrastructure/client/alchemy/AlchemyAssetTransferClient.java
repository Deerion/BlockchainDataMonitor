package pl.skompilowani.infrastructure.client.alchemy;

import okhttp3.OkHttpClient;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.http.HttpService;
import pl.skompilowani.core.model.AddressTransferDTO;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class AlchemyAssetTransferClient {

    private final HttpService web3jService;

    public AlchemyAssetTransferClient(String rpcUrl) {
        // Utrzymujemy stabilny timeout 60 sekund
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
        this.web3jService = new HttpService(rpcUrl, okHttpClient, false);
    }

    private static final List<String> CATEGORIES = List.of("external", "internal");

    // Dodaliśmy parametr fromBlockHex do metody
    public List<AddressTransferDTO> getTransfersByFromAddress(String address, int maxCount, String fromBlockHex) throws Exception {
        return executeRequest(buildParams(address, null, maxCount, fromBlockHex));
    }

    // Dodaliśmy parametr fromBlockHex do metody
    public List<AddressTransferDTO> getTransfersByToAddress(String address, int maxCount, String fromBlockHex) throws Exception {
        return executeRequest(buildParams(null, address, maxCount, fromBlockHex));
    }

    private Map<String, Object> buildParams(String fromAddress, String toAddress, int maxCount, String fromBlockHex) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("fromBlock", fromBlockHex); // <--- ZABEZPIECZENIE: dynamiczny blok zamiast "0x0"
        params.put("toBlock", "latest");
        if (fromAddress != null) params.put("fromAddress", fromAddress);
        if (toAddress != null)   params.put("toAddress", toAddress);
        params.put("category", CATEGORIES);
        params.put("order", "desc");
        params.put("maxCount", "0x" + Integer.toHexString(maxCount));
        params.put("withMetadata", true);
        params.put("excludeZeroValue", false);
        return params;
    }

    private List<AddressTransferDTO> executeRequest(Map<String, Object> params) throws Exception {
        Request<?, AlchemyAssetTransfersResponse> request = new Request<>(
                "alchemy_getAssetTransfers",
                List.of(params),
                web3jService,
                AlchemyAssetTransfersResponse.class
        );

        AlchemyAssetTransfersResponse response = request.send();

        if (response.hasError()) {
            throw new RuntimeException("Alchemy API error: " + response.getError().getMessage());
        }

        return response.getTransfers().stream()
                .map(this::toDTO)
                .toList();
    }

    private AddressTransferDTO toDTO(AlchemyAssetTransfersResponse.Transfer t) {
        BigInteger blockNumber = (t.getBlockNum() != null && t.getBlockNum().length() > 2)
                ? new BigInteger(t.getBlockNum().substring(2), 16)
                : BigInteger.ZERO;

        String blockTimestamp = (t.getMetadata() != null) ? t.getMetadata().getBlockTimestamp() : "";
        BigDecimal value = t.getValue() != null ? t.getValue() : BigDecimal.ZERO;

        return new AddressTransferDTO(
                t.getHash(), t.getFrom(), t.getTo(),
                blockNumber, value, t.getAsset(), t.getCategory(), blockTimestamp,
                BigInteger.ZERO, BigDecimal.ZERO
        );
    }
}