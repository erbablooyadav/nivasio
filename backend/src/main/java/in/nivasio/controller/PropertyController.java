package in.nivasio.controller;

import in.nivasio.dto.ApiResponse;
import in.nivasio.model.Property;
import in.nivasio.security.UserPrincipal;
import in.nivasio.service.PropertyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/properties")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('PROPERTY_ADMIN', 'SUPER_ADMIN')")
public class PropertyController {

    private final PropertyService propertyService;

    @GetMapping
    public ApiResponse<List<Property>> list(@AuthenticationPrincipal UserPrincipal user) {
        return ApiResponse.ok(propertyService.listByTenant(user.getTenantId()));
    }

    @PostMapping
    public ApiResponse<Property> create(@AuthenticationPrincipal UserPrincipal user,
            @RequestBody Property property) {
        return ApiResponse.ok(propertyService.create(user.getTenantId(), property, user.getUserId()));
    }

    @GetMapping("/{id}")
    public ApiResponse<Property> get(@AuthenticationPrincipal UserPrincipal user,
            @PathVariable String id) {
        return ApiResponse.ok(propertyService.getById(user.getTenantId(), id));
    }

    @PutMapping("/{id}")
    public ApiResponse<Property> update(@AuthenticationPrincipal UserPrincipal user,
            @PathVariable String id,
            @RequestBody Property property) {
        return ApiResponse.ok(propertyService.update(user.getTenantId(), id, property, user.getUserId()));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deactivate(@AuthenticationPrincipal UserPrincipal user,
            @PathVariable String id) {
        propertyService.deactivate(user.getTenantId(), id, user.getUserId());
        return ApiResponse.ok("Property deactivated");
    }
}
