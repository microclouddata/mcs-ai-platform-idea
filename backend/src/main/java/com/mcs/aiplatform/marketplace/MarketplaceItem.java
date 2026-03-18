package com.mcs.aiplatform.marketplace;

import com.mcs.aiplatform.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "marketplace_items")
public class MarketplaceItem extends BaseEntity {
    private String authorId;
    private String authorName;
    private MarketplaceItemType type;
    private String title;
    private String description;
    private String category;
    private List<String> tags;
    private Map<String, Object> config; // agent config, template content, workflow steps
    private boolean published = false;
    private int downloads = 0;
    private double rating = 0.0;
    private int ratingCount = 0;
}
