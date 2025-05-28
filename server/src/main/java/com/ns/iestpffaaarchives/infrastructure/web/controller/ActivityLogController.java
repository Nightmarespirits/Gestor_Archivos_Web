package com.ns.iestpffaaarchives.infrastructure.web.controller;

import com.ns.iestpffaaarchives.application.service.UserService;
import com.ns.iestpffaaarchives.domain.entity.ActivityLog;
import com.ns.iestpffaaarchives.domain.entity.User;
import com.ns.iestpffaaarchives.infrastructure.web.dto.ActivityLogDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

// Repositorio para ActivityLog
@Repository
interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    List<ActivityLog> findByUser(User user);
    List<ActivityLog> findByActionType(String actionType);
    List<ActivityLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    Page<ActivityLog> findAll(Pageable pageable);
}

// Servicio para ActivityLog
@Service
class ActivityLogService {
    
    private final ActivityLogRepository activityLogRepository;
    
    @Autowired
    public ActivityLogService(ActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }
    
    public List<ActivityLog> getAllActivityLogs() {
        return activityLogRepository.findAll();
    }
    
    public Page<ActivityLog> getActivityLogsPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        return activityLogRepository.findAll(pageable);
    }
    
    public Optional<ActivityLog> getActivityLogById(Long id) {
        return activityLogRepository.findById(id);
    }
    
    public List<ActivityLog> getActivityLogsByUser(User user) {
        return activityLogRepository.findByUser(user);
    }
    
    public List<ActivityLog> getActivityLogsByActionType(String actionType) {
        return activityLogRepository.findByActionType(actionType);
    }
    
    public List<ActivityLog> getActivityLogsByTimeRange(LocalDateTime start, LocalDateTime end) {
        return activityLogRepository.findByTimestampBetween(start, end);
    }
    
    public ActivityLog createActivityLog(ActivityLog activityLog) {
        activityLog.setTimestamp(LocalDateTime.now());
        return activityLogRepository.save(activityLog);
    }
    
    public void deleteActivityLog(Long id) {
        activityLogRepository.deleteById(id);
    }
}

@RestController
@RequestMapping("/api/activity-logs")
// Eliminado @CrossOrigin(origins = "*") para usar la configuración global
public class ActivityLogController {

    private final ActivityLogService activityLogService;
    private final UserService userService;
    
    @Autowired
    public ActivityLogController(ActivityLogService activityLogService, UserService userService) {
        this.activityLogService = activityLogService;
        this.userService = userService;
    }
    
    @GetMapping
    @PreAuthorize("hasAuthority('AUDIT_LOG_VIEW')")
    public ResponseEntity<List<ActivityLogDTO>> getAllActivityLogs() {
        List<ActivityLog> activityLogs = activityLogService.getAllActivityLogs();
        // Convertir las entidades a DTOs
        List<ActivityLogDTO> dtos = activityLogs.stream()
            .map(ActivityLogDTO::new)
            .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }
    
    @GetMapping("/paged")
    public ResponseEntity<Page<ActivityLog>> getActivityLogsPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<ActivityLog> activityLogs = activityLogService.getActivityLogsPage(page, size);
        return ResponseEntity.ok(activityLogs);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('AUDIT_LOG_VIEW')")
    public ResponseEntity<ActivityLog> getActivityLogById(@PathVariable Long id) {
        Optional<ActivityLog> activityLog = activityLogService.getActivityLogById(id);
        return activityLog.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ActivityLog>> getActivityLogsByUser(@PathVariable Long userId) {
        Optional<User> userOptional = userService.getUserById(userId);
        
        if (userOptional.isPresent()) {
            List<ActivityLog> activityLogs = activityLogService.getActivityLogsByUser(userOptional.get());
            return ResponseEntity.ok(activityLogs);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/action-type/{actionType}")
    public ResponseEntity<List<ActivityLog>> getActivityLogsByActionType(@PathVariable String actionType) {
        List<ActivityLog> activityLogs = activityLogService.getActivityLogsByActionType(actionType);
        return ResponseEntity.ok(activityLogs);
    }
    
    @GetMapping("/time-range")
    public ResponseEntity<List<ActivityLog>> getActivityLogsByTimeRange(
            @RequestParam String startTime,
            @RequestParam String endTime) {
        LocalDateTime start = LocalDateTime.parse(startTime);
        LocalDateTime end = LocalDateTime.parse(endTime);
        
        List<ActivityLog> activityLogs = activityLogService.getActivityLogsByTimeRange(start, end);
        return ResponseEntity.ok(activityLogs);
    }
    
    @PostMapping
    public ResponseEntity<ActivityLog> createActivityLog(@RequestBody ActivityLog activityLog) {
        ActivityLog newActivityLog = activityLogService.createActivityLog(activityLog);
        return ResponseEntity.ok(newActivityLog);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('AUDIT_LOG_VIEW')")
    public ResponseEntity<Void> deleteActivityLog(@PathVariable Long id) {
        Optional<ActivityLog> activityLogOptional = activityLogService.getActivityLogById(id);
        
        if (activityLogOptional.isPresent()) {
            activityLogService.deleteActivityLog(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
