package in.nivasio.controller;

import in.nivasio.dto.ApiResponse;
import in.nivasio.model.Resident;
import in.nivasio.security.UserPrincipal;
import in.nivasio.service.ResidentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/residents")
@RequiredArgsConstructor
public class ResidentController {

    private final ResidentService residentService;

    @GetMapping
    public ApiResponse<Page<Resident>> list(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(residentService.list(user.getTenantId(), search, page, size));
    }

    @PostMapping
    public ApiResponse<Resident> create(@AuthenticationPrincipal UserPrincipal user,
            @RequestBody Resident resident) {
        return ApiResponse.ok(residentService.create(user.getTenantId(), resident, user.getUserId()));
    }

    @GetMapping("/{id}")
    public ApiResponse<Resident> get(@AuthenticationPrincipal UserPrincipal user,
            @PathVariable String id) {
        return ApiResponse.ok(residentService.getById(user.getTenantId(), id));
    }

    @GetMapping("/room/{roomNo}")
    public ApiResponse<List<Resident>> getByRoom(@AuthenticationPrincipal UserPrincipal user,
            @PathVariable String roomNo) {
        return ApiResponse.ok(residentService.getByRoom(user.getTenantId(), roomNo));
    }

    @PutMapping("/{id}")
    public ApiResponse<Resident> update(@AuthenticationPrincipal UserPrincipal user,
            @PathVariable String id,
            @RequestBody Resident resident) {
        return ApiResponse.ok(residentService.update(user.getTenantId(), id, resident, user.getUserId()));
    }

    @PostMapping("/{id}/move-out")
    public ApiResponse<Resident> moveOut(@AuthenticationPrincipal UserPrincipal user,
            @PathVariable String id) {
        return ApiResponse.ok(residentService.moveOut(user.getTenantId(), id, user.getUserId()));
    }
}
