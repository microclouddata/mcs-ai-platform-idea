package com.mcs.aiplatform.marketplace;
import java.util.List;
import java.util.Map;
public record PublishItemRequest(
        MarketplaceItemType type,
        String title,
        String description,
        String category,
        List<String> tags,
        Map<String, Object> config
) {}
