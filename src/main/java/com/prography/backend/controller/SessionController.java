package com.prography.backend.controller;

import com.prography.backend.api.ApiResponse;
import com.prography.backend.dto.SessionDto;
import com.prography.backend.service.SessionService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping("/sessions")
    public ApiResponse<List<SessionDto.SessionResponse>> getSessions() {
        return ApiResponse.success(sessionService.getMemberSessions());
    }
}
