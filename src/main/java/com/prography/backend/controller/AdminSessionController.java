package com.prography.backend.controller;

import com.prography.backend.api.ApiResponse;
import com.prography.backend.dto.SessionDto;
import com.prography.backend.service.QrCodeService;
import com.prography.backend.service.SessionService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminSessionController {

    private final SessionService sessionService;
    private final QrCodeService qrCodeService;

    public AdminSessionController(SessionService sessionService, QrCodeService qrCodeService) {
        this.sessionService = sessionService;
        this.qrCodeService = qrCodeService;
    }

    @GetMapping("/sessions")
    public ApiResponse<List<SessionDto.SessionResponse>> getSessions() {
        return ApiResponse.success(sessionService.getAdminSessions());
    }

    @PostMapping("/sessions")
    public ApiResponse<SessionDto.SessionResponse> create(@Valid @RequestBody SessionDto.CreateSessionRequest request) {
        return ApiResponse.success(sessionService.create(request));
    }

    @PutMapping("/sessions/{id}")
    public ApiResponse<SessionDto.SessionResponse> update(@PathVariable Long id,
                                                          @Valid @RequestBody SessionDto.UpdateSessionRequest request) {
        return ApiResponse.success(sessionService.update(id, request));
    }

    @DeleteMapping("/sessions/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        sessionService.delete(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/sessions/{id}/qrcodes")
    public ApiResponse<SessionDto.QrCodeResponse> createQr(@PathVariable Long id) {
        return ApiResponse.success(qrCodeService.create(id));
    }

    @PutMapping("/qrcodes/{id}")
    public ApiResponse<SessionDto.QrCodeResponse> renewQr(@PathVariable Long id) {
        return ApiResponse.success(qrCodeService.renew(id));
    }
}
