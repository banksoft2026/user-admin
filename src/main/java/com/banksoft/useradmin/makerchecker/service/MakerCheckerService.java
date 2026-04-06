package com.banksoft.useradmin.makerchecker.service;

import com.banksoft.useradmin.common.CbsException;
import com.banksoft.useradmin.makerchecker.dto.CheckerDecisionRequest;
import com.banksoft.useradmin.makerchecker.dto.SubmitActionRequest;
import com.banksoft.useradmin.makerchecker.entity.MakerCheckerQueue;
import com.banksoft.useradmin.makerchecker.repository.MakerCheckerQueueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MakerCheckerService {

    private final MakerCheckerQueueRepository repository;

    @Transactional(readOnly = true)
    public Page<MakerCheckerQueue> getQueue(String status, String actionType, UUID makerId, Pageable pageable) {
        return repository.findFiltered(status, actionType, makerId, pageable);
    }

    @Transactional(readOnly = true)
    public MakerCheckerQueue getById(UUID queueId) {
        return repository.findById(queueId)
                .orElseThrow(() -> CbsException.notFound("Queue item not found: " + queueId));
    }

    @Transactional(readOnly = true)
    public long getPendingCount() {
        return repository.countByStatus("PENDING");
    }

    @Transactional
    public MakerCheckerQueue submit(SubmitActionRequest request, UUID makerUserId) {
        MakerCheckerQueue item = MakerCheckerQueue.builder()
                .makerUserId(makerUserId)
                .actionType(request.getActionType())
                .entityType(request.getEntityType())
                .entityId(request.getEntityId())
                .screenId(request.getScreenId())
                .payloadBefore(request.getPayloadBefore())
                .payloadAfter(request.getPayloadAfter())
                .priority(request.getPriority())
                .status("PENDING")
                .build();
        return repository.save(item);
    }

    @Transactional
    public MakerCheckerQueue approve(UUID queueId, UUID checkerUserId) {
        MakerCheckerQueue item = getById(queueId);
        if (!"PENDING".equals(item.getStatus())) {
            throw CbsException.badRequest("Item is not in PENDING status");
        }
        if (item.getMakerUserId().equals(checkerUserId)) {
            throw CbsException.badRequest("Checker cannot approve their own submission");
        }
        item.setStatus("APPROVED");
        item.setCheckerUserId(checkerUserId);
        item.setResolvedAt(Instant.now());
        item.setExecutedAt(Instant.now());
        return repository.save(item);
    }

    @Transactional
    public MakerCheckerQueue reject(UUID queueId, UUID checkerUserId, CheckerDecisionRequest request) {
        MakerCheckerQueue item = getById(queueId);
        if (!"PENDING".equals(item.getStatus())) {
            throw CbsException.badRequest("Item is not in PENDING status");
        }
        if (item.getMakerUserId().equals(checkerUserId)) {
            throw CbsException.badRequest("Checker cannot reject their own submission");
        }
        item.setStatus("REJECTED");
        item.setCheckerUserId(checkerUserId);
        item.setResolvedAt(Instant.now());
        item.setRejectionReason(request.getRejectionReason());
        return repository.save(item);
    }

    @Transactional
    public MakerCheckerQueue recall(UUID queueId, UUID makerUserId) {
        MakerCheckerQueue item = getById(queueId);
        if (!item.getMakerUserId().equals(makerUserId)) {
            throw CbsException.forbidden("Only the maker can recall this submission");
        }
        if (!"PENDING".equals(item.getStatus())) {
            throw CbsException.badRequest("Only PENDING items can be recalled");
        }
        item.setStatus("RECALLED");
        item.setResolvedAt(Instant.now());
        return repository.save(item);
    }
}
