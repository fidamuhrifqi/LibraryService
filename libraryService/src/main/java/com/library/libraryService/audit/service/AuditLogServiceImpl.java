package com.library.libraryService.audit.service;

import com.library.libraryService.audit.dao.AuditLogDao;
import com.library.libraryService.audit.dto.AuditLogResponseDto;
import com.library.libraryService.audit.entity.AuditLog;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    @Autowired
    private AuditLogDao auditLogDao;

    @Override
    public void log(String action, String resourceType, String resourceId) {
        log(action, resourceType, resourceId, null);
    }

    @Override
    public void log(String action, String resourceType, String resourceId, String usernameOverride) {
        String username = resolveUsername(usernameOverride);

        HttpServletRequest request = resolveCurrentRequest();

        String method = null;
        String path = null;
        String userAgent = null;
        String ip = null;

        if (request != null) {
            method = request.getMethod();
            path = request.getRequestURI();
            userAgent = request.getHeader("User-Agent");
            ip = request.getRemoteAddr();
        }

        AuditLog log = new AuditLog();
        log.setUsername(username);
        log.setAction(action);
        log.setResourceType(resourceType);
        log.setResourceId(resourceId);
        log.setMethod(method);
        log.setPath(path);
        log.setUserAgent(userAgent);
        log.setIpAddress(ip);
        log.setTimestamp(new Date());

        auditLogDao.save(log);
    }

    @Override
    public List<AuditLogResponseDto> getAllLogs(String sortType) {

        log("GET_ALL_AUDIT_LOG","AUDIT","ALL");

        ArrayList<AuditLogResponseDto> result = auditLogDao.findAll().stream()
                .map(l -> new AuditLogResponseDto(
                        l.getId(),
                        l.getUsername(),
                        l.getAction(),
                        l.getResourceType(),
                        l.getResourceId(),
                        l.getMethod(),
                        l.getPath(),
                        l.getUserAgent(),
                        l.getIpAddress(),
                        l.getTimestamp()
                ))
                .collect(Collectors.toCollection(ArrayList::new));

        bubbleSort(result, sortType);

        return result;
    }

    private String resolveUsername(String usernameOverride) {
        if (usernameOverride != null) {
            return usernameOverride;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            return auth.getName();
        }
        return "anonymousUser";
    }

    private HttpServletRequest resolveCurrentRequest() {
        var attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes servletAttrs) {
            return servletAttrs.getRequest();
        }
        return null;
    }

    private void bubbleSort(ArrayList<AuditLogResponseDto> list, String sortType) {
        int size = list.size();
        boolean swapped;

        if (sortType.equals("asc")) {
            for (int i = 0; i < size - 1; i++) {
                swapped = false;
                for (int j = 0; j < size - 1 - i; j++) {
                    if (list.get(j).getTimestamp().after(list.get(j + 1).getTimestamp())) {
                        AuditLogResponseDto temp = list.get(j);
                        list.set(j, list.get(j + 1));
                        list.set(j + 1, temp);
                        swapped = true;
                    }
                }
                if (!swapped) break;
            }
        }else if (sortType.equals("desc")) {
            for (int i = 0; i < size - 1; i++) {
                swapped = false;
                for (int j = 0; j < size - 1 - i; j++) {
                    if (list.get(j).getTimestamp().before(list.get(j + 1).getTimestamp())) {
                        AuditLogResponseDto temp = list.get(j);
                        list.set(j, list.get(j + 1));
                        list.set(j + 1, temp);
                        swapped = true;
                    }
                }
                if (!swapped) break;
            }
        }else{
            throw new RuntimeException("Format Sorting Salah");
        }
    }
}