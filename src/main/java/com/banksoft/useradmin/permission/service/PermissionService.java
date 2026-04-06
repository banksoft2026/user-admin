package com.banksoft.useradmin.permission.service;

import com.banksoft.useradmin.common.CbsException;
import com.banksoft.useradmin.permission.dto.FieldPermRequest;
import com.banksoft.useradmin.permission.dto.ScreenPermRequest;
import com.banksoft.useradmin.permission.dto.ScreenPermissionContext;
import com.banksoft.useradmin.permission.entity.*;
import com.banksoft.useradmin.permission.repository.*;
import com.banksoft.useradmin.role.entity.UserRole;
import com.banksoft.useradmin.role.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final ModuleRepository moduleRepository;
    private final ScreenRepository screenRepository;
    private final ScreenFieldRepository screenFieldRepository;
    private final RoleScreenPermRepository roleScreenPermRepository;
    private final FieldPermissionRepository fieldPermissionRepository;
    private final UserRoleRepository userRoleRepository;

    private static final Map<String, Integer> ACCESS_LEVEL_RANK = Map.of(
            "FULL", 4, "MAKER", 3, "READ", 2, "NONE", 1
    );
    private static final Map<String, Integer> VISIBILITY_RANK = Map.of(
            "VISIBLE", 3, "MASKED", 2, "HIDDEN", 1
    );
    private static final Map<String, Integer> EDITABILITY_RANK = Map.of(
            "EDITABLE", 4, "MAKER_ONLY", 3, "READ_ONLY", 2, "HIDDEN", 1
    );

    @Transactional(readOnly = true)
    public List<CbsModule> getAllModules() {
        return moduleRepository.findAllByIsActiveTrueOrderBySortOrder();
    }

    @Transactional(readOnly = true)
    public List<Screen> getAllScreens() {
        return screenRepository.findAllByIsActiveTrueOrderBySortOrder();
    }

    @Transactional(readOnly = true)
    public List<RoleScreenPerm> getScreenPermsForRole(UUID roleId) {
        return roleScreenPermRepository.findByRoleIdAndIsActiveTrue(roleId);
    }

    @Transactional
    public RoleScreenPerm upsertScreenPerm(ScreenPermRequest request, UUID grantedBy) {
        Optional<RoleScreenPerm> existing = roleScreenPermRepository
                .findByRoleIdAndScreenId(request.getRoleId(), request.getScreenId());
        if (existing.isPresent()) {
            RoleScreenPerm perm = existing.get();
            perm.setCanAccess(request.isCanAccess());
            perm.setAccessLevel(request.getAccessLevel());
            perm.setIsActive(true);
            return roleScreenPermRepository.save(perm);
        }
        return roleScreenPermRepository.save(RoleScreenPerm.builder()
                .roleId(request.getRoleId())
                .screenId(request.getScreenId())
                .canAccess(request.isCanAccess())
                .accessLevel(request.getAccessLevel())
                .grantedBy(grantedBy)
                .build());
    }

    @Transactional
    public FieldPermission upsertFieldPerm(FieldPermRequest request, UUID grantedBy) {
        Optional<FieldPermission> existing = fieldPermissionRepository
                .findByRoleIdAndScreenIdAndFieldId(request.getRoleId(), request.getScreenId(), request.getFieldId());
        if (existing.isPresent()) {
            FieldPermission fp = existing.get();
            fp.setVisibility(request.getVisibility());
            fp.setEditability(request.getEditability());
            fp.setMaskPattern(request.getMaskPattern());
            return fieldPermissionRepository.save(fp);
        }
        return fieldPermissionRepository.save(FieldPermission.builder()
                .roleId(request.getRoleId())
                .screenId(request.getScreenId())
                .fieldId(request.getFieldId())
                .visibility(request.getVisibility())
                .editability(request.getEditability())
                .maskPattern(request.getMaskPattern())
                .grantedBy(grantedBy)
                .build());
    }

    @Transactional(readOnly = true)
    public ScreenPermissionContext resolvePermissions(UUID userId, UUID screenId) {
        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> CbsException.notFound("Screen not found: " + screenId));

        List<UserRole> userRoles = userRoleRepository.findActiveRolesForUser(userId);
        List<UUID> roleIds = userRoles.stream().map(UserRole::getRoleId).collect(Collectors.toList());

        // Step 3-4: Resolve screen access
        boolean canAccess = false;
        String accessLevel = "NONE";
        for (UUID roleId : roleIds) {
            Optional<RoleScreenPerm> perm = roleScreenPermRepository.findByRoleIdAndScreenId(roleId, screenId);
            if (perm.isPresent() && Boolean.TRUE.equals(perm.get().getCanAccess()) && Boolean.TRUE.equals(perm.get().getIsActive())) {
                canAccess = true;
                String level = perm.get().getAccessLevel();
                if (ACCESS_LEVEL_RANK.getOrDefault(level, 0) > ACCESS_LEVEL_RANK.getOrDefault(accessLevel, 0)) {
                    accessLevel = level;
                }
            }
        }

        // Step 5-7: Resolve field permissions
        List<ScreenField> fields = screenFieldRepository.findByScreenIdOrderBySortOrder(screenId);
        List<ScreenPermissionContext.FieldContext> fieldContexts = new ArrayList<>();

        for (ScreenField field : fields) {
            String bestVisibility = field.getDefaultVisible() ? "VISIBLE" : "HIDDEN";
            String bestEditability = field.getDefaultEditable() ? "EDITABLE" : "READ_ONLY";
            String maskPattern = null;

            for (UUID roleId : roleIds) {
                Optional<FieldPermission> fp = fieldPermissionRepository
                        .findByRoleIdAndScreenIdAndFieldId(roleId, screenId, field.getFieldId());
                if (fp.isPresent() && Boolean.TRUE.equals(fp.get().getIsActive())) {
                    String vis = fp.get().getVisibility();
                    String edit = fp.get().getEditability();
                    if (VISIBILITY_RANK.getOrDefault(vis, 0) > VISIBILITY_RANK.getOrDefault(bestVisibility, 0)) {
                        bestVisibility = vis;
                        maskPattern = fp.get().getMaskPattern();
                    }
                    if (EDITABILITY_RANK.getOrDefault(edit, 0) > EDITABILITY_RANK.getOrDefault(bestEditability, 0)) {
                        bestEditability = edit;
                    }
                }
            }

            fieldContexts.add(ScreenPermissionContext.FieldContext.builder()
                    .fieldId(field.getFieldId())
                    .fieldCode(field.getFieldCode())
                    .fieldName(field.getFieldName())
                    .visibility(bestVisibility)
                    .editability(bestEditability)
                    .maskPattern(maskPattern)
                    .build());
        }

        return ScreenPermissionContext.builder()
                .userId(userId)
                .screenId(screenId)
                .screenCode(screen.getScreenCode())
                .canAccess(canAccess)
                .accessLevel(accessLevel)
                .fields(fieldContexts)
                .build();
    }

    @Transactional(readOnly = true)
    public List<ScreenPermissionContext> resolveAllUserPermissions(UUID userId) {
        List<Screen> screens = screenRepository.findAllByIsActiveTrueOrderBySortOrder();
        return screens.stream()
                .map(s -> resolvePermissions(userId, s.getScreenId()))
                .filter(ScreenPermissionContext::isCanAccess)
                .collect(Collectors.toList());
    }
}
