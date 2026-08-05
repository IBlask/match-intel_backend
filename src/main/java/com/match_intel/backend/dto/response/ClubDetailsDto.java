package com.match_intel.backend.dto.response;

import java.util.List;

public record ClubDetailsDto(
        ClubDto club,
        List<ClubCourtDto> courts,
        List<ClubAdminDto> admins
) {}
