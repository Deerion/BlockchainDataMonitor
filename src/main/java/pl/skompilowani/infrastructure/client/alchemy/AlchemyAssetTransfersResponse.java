package pl.skompilowani.infrastructure.client.alchemy;

import org.web3j.protocol.core.Response;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public class AlchemyAssetTransfersResponse extends Response<AlchemyAssetTransfersResponse.TransferResult> {

    public List<Transfer> getTransfers() {
        TransferResult r = getResult();
        return r != null && r.getTransfers() != null ? r.getTransfers() : Collections.emptyList();
    }

    public static class TransferResult {
        private List<Transfer> transfers;

        public List<Transfer> getTransfers() { return transfers; }
        public void setTransfers(List<Transfer> transfers) { this.transfers = transfers; }
    }

    public static class Transfer {
        private String hash;
        private String from;
        private String to;
        private String blockNum;
        private BigDecimal value;
        private String asset;
        private String category;
        private Metadata metadata;

        public String getHash()         { return hash; }
        public void setHash(String v)   { this.hash = v; }

        public String getFrom()         { return from; }
        public void setFrom(String v)   { this.from = v; }

        public String getTo()           { return to; }
        public void setTo(String v)     { this.to = v; }

        public String getBlockNum()        { return blockNum; }
        public void setBlockNum(String v)  { this.blockNum = v; }

        public BigDecimal getValue()        { return value; }
        public void setValue(BigDecimal v)  { this.value = v; }

        public String getAsset()        { return asset; }
        public void setAsset(String v)  { this.asset = v; }

        public String getCategory()        { return category; }
        public void setCategory(String v)  { this.category = v; }

        public Metadata getMetadata()        { return metadata; }
        public void setMetadata(Metadata v)  { this.metadata = v; }
    }

    public static class Metadata {
        private String blockTimestamp;

        public String getBlockTimestamp()        { return blockTimestamp; }
        public void setBlockTimestamp(String v)  { this.blockTimestamp = v; }
    }
}
