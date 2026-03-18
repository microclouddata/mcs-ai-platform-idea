package com.mcs.aiplatform.marketplace;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface MarketplaceRepository extends MongoRepository<MarketplaceItem, String> {
    List<MarketplaceItem> findByPublishedTrueOrderByDownloadsDesc();
    List<MarketplaceItem> findByPublishedTrueAndTypeOrderByDownloadsDesc(MarketplaceItemType type);
    List<MarketplaceItem> findByAuthorId(String authorId);
    List<MarketplaceItem> findByPublishedTrueAndTitleContainingIgnoreCase(String title);
}
