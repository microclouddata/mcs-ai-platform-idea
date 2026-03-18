package com.mcs.aiplatform.marketplace;

import com.mcs.aiplatform.common.ApiResponse;
import com.mcs.aiplatform.config.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/marketplace")
@RequiredArgsConstructor
public class MarketplaceController {

    private final MarketplaceService marketplaceService;

    @GetMapping
    public ApiResponse<List<MarketplaceItem>> browse(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String search) {
        return ApiResponse.ok(marketplaceService.browse(type, search));
    }

    @GetMapping("/my")
    public ApiResponse<List<MarketplaceItem>> myItems() {
        return ApiResponse.ok(marketplaceService.myItems(CurrentUser.userId()));
    }

    @PostMapping
    public ApiResponse<MarketplaceItem> publish(@RequestBody PublishItemRequest req) {
        return ApiResponse.ok(marketplaceService.publish(CurrentUser.userId(), req));
    }

    @PostMapping("/{itemId}/clone")
    public ApiResponse<MarketplaceItem> clone(@PathVariable String itemId) {
        return ApiResponse.ok(marketplaceService.clone(itemId, CurrentUser.userId()));
    }

    @DeleteMapping("/{itemId}")
    public ApiResponse<Void> unpublish(@PathVariable String itemId) {
        marketplaceService.unpublish(itemId, CurrentUser.userId());
        return ApiResponse.ok(null);
    }
}
