package com.thegamersstation.marketplace.region;

import com.thegamersstation.marketplace.region.dto.RegionDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/regions")
@RequiredArgsConstructor
@Tag(name = "Regions", description = "Region and city listing endpoints")
public class RegionController {
    
    private final RegionService regionService;
    
    @GetMapping
    @Operation(
        summary = "Get all regions",
        description = "Returns list of all regions with bilingual names (EN/AR)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Regions retrieved successfully")
    })
    public ResponseEntity<List<RegionDto>> getAllRegions() {
        List<RegionDto> regions = regionService.getAllRegions();
        return ResponseEntity.ok(regions);
    }
}
