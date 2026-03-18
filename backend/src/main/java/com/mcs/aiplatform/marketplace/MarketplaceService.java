package com.mcs.aiplatform.marketplace;

import com.mcs.aiplatform.user.User;
import com.mcs.aiplatform.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MarketplaceService {

    private final MarketplaceRepository marketplaceRepository;
    private final UserRepository userRepository;

    public MarketplaceItem publish(String authorId, PublishItemRequest req) {
        User author = userRepository.findById(authorId).orElseThrow();
        MarketplaceItem item = new MarketplaceItem();
        item.setAuthorId(authorId);
        item.setAuthorName(author.getName());
        item.setType(req.type());
        item.setTitle(req.title());
        item.setDescription(req.description());
        item.setCategory(req.category());
        item.setTags(req.tags());
        item.setConfig(req.config());
        item.setPublished(true);
        return marketplaceRepository.save(item);
    }

    public List<MarketplaceItem> browse(String type, String search) {
        if (search != null && !search.isBlank()) {
            return marketplaceRepository.findByPublishedTrueAndTitleContainingIgnoreCase(search);
        }
        if (type != null && !type.isBlank()) {
            try {
                MarketplaceItemType t = MarketplaceItemType.valueOf(type.toUpperCase());
                return marketplaceRepository.findByPublishedTrueAndTypeOrderByDownloadsDesc(t);
            } catch (IllegalArgumentException ignored) {}
        }
        return marketplaceRepository.findByPublishedTrueOrderByDownloadsDesc();
    }

    public List<MarketplaceItem> myItems(String authorId) {
        return marketplaceRepository.findByAuthorId(authorId);
    }

    public MarketplaceItem clone(String itemId, String userId) {
        MarketplaceItem source = marketplaceRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        if (!source.isPublished()) throw new RuntimeException("Item not available");
        source.setDownloads(source.getDownloads() + 1);
        marketplaceRepository.save(source);
        // Return the config for the caller to use (no new DB record needed for the clone itself)
        return source;
    }

    public void unpublish(String itemId, String userId) {
        MarketplaceItem item = marketplaceRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        if (!item.getAuthorId().equals(userId)) throw new RuntimeException("Access denied");
        item.setPublished(false);
        marketplaceRepository.save(item);
    }
}
