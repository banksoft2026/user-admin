package com.banksoft.useradmin.makerchecker.controller;

import com.banksoft.useradmin.common.ApiResponse;
import com.banksoft.useradmin.makerchecker.dto.CheckerDecisionRequest;
import com.banksoft.useradmin.makerchecker.dto.SubmitActionRequest;
import com.banksoft.useradmin.makerchecker.entity.MakerCheckerQueue;
import com.banksoft.useradmin.makerchecker.service.MakerCheckerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/approvals")
@RequiredArgsConstructor
@Tag(name = "Maker-Checker", description = "Approval queue management")
public class MakerCheckerController {

    private final MakerCheckerService makerCheckerService;

    @GetMapping
    @Operation(summary = "List approval queue")
    public ResponseEntity<ApiResponse<Page<MakerCheckerQueue>>> getQueue(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String actionType,
            @RequestParam(required = false) UUID makerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
                makerCheckerService.getQueue(status, actionType, makerId,
                        PageRequest.of(page, size, Sort.by("submittedAt").descending()))));
    }

    @GetMapping("/pending-count")
    @Operation(summary = "Get pending approvals count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getPendingCount() {
        return ResponseEntity.ok(ApiResponse.ok(Map.of("count", makerCheckerService.getPendingCount())));
    }

    @GetMapping("/{queueId}")
    @Operation(summary = "Get approval item detail")
    public ResponseEntity<ApiResponse<MakerCheckerQueue>> getById(@PathVariable UUID queueId) {
        return ResponseEntity.ok(ApiResponse.ok(makerCheckerService.getById(queueId)));
    }

    @PostMapping
    @Operation(summary = "Submit action for approval")
    public ResponseEntity<ApiResponse<MakerCheckerQueue>> submit(
            @Valid @RequestBody SubmitActionRequest request,
            @AuthenticationPrincipal String makerUserId) {
        UUID makerId = makerUserId != null ? UUID.fromString(makerUserId) : null;
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Submitted for approval", makerCheckerService.submit(request, makerId)));
    }

    @PostMapping("/{queueId}/approve")
    @Operation(summary = "Checker approves")
    public ResponseEntity<ApiResponse<MakerCheckerQueue>> approve(
            @PathVariable UUID queueId,
            @AuthenticationPrincipal String checkerUserId) {
        UUID checkerId = checkerUserId != null ? UUID.fromString(checkerUserId) : null;
        return ResponseEntity.ok(ApiResponse.ok("Approved", makerCheckerService.approve(queueId, checkerId)));
    }

    @PostMapping("/{queueId}/reject")
    @Operation(summary = "Checker rejects")
    public ResponseEntity<ApiResponse<MakerCheckerQueue>> reject(
            @PathVariable UUID queueId,
            @RequestBody CheckerDecisionRequest request,
            @AuthenticationPrincipal String checkerUserId) {
        UUID checkerId = checkerUserId != null ? UUID.fromString(checkerUserId) : null;
        return ResponseEntity.ok(ApiResponse.ok("Rejected", makerCheckerService.reject(queueId, checkerId, request)));
    }

    @PostMapping("/{queueId}/recall")
    @Operation(summary = "Maker recalls submission")
    public ResponseEntity<ApiResponse<MakerCheckerQueue>> recall(
            @PathVariable UUID queueId,
            @AuthenticationPrincipal String makerUserId) {
        UUID makerId = makerUserId != null ? UUID.fromString(makerUserId) : null;
        return ResponseEntity.ok(ApiResponse.ok("Recalled", makerCheckerService.recall(queueId, makerId)));
    }
}
