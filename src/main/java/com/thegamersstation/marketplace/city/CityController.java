package com.thegamersstation.marketplace.city;

import com.thegamersstation.marketplace.city.dto.CityDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cities")
@RequiredArgsConstructor
@Tag(name = "Cities", description = "City listing endpoints")
public class CityController {
    
    private final CityService cityService;
    
    @GetMapping
    @Operation(
        summary = "Get cities",
        description = "Returns list of cities. Can be filtered by region using regionId parameter."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cities retrieved successfully")
    })
    public ResponseEntity<List<CityDto>> getCities(
            @Parameter(description = "Filter by region ID")
            @RequestParam(required = false) Long regionId
    ) {
        List<CityDto> cities = regionId != null 
            ? cityService.getCitiesByRegion(regionId)
            : cityService.getAllCities();
        return ResponseEntity.ok(cities);
    }
}
