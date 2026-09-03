package com.match_intel.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlayerInfoDto {
    private String id;
    private String username;
    private String firstName;
    private String lastName;
}
