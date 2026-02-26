package com.prography.backend.dto;

import java.util.List;

public class CohortDto {

    public record CohortListItem(Long id, String name, boolean current) {
    }

    public record PartItem(Long id, String name) {
    }

    public record TeamItem(Long id, String name) {
    }

    public record CohortDetailResponse(Long id, String name, boolean current, List<PartItem> parts, List<TeamItem> teams) {
    }
}
