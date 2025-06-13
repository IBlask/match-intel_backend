
package com.match_intel.backend.controller;

import com.match_intel.backend.service.FollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/follow")
public class FollowController {

    @Autowired
    private FollowService followService;

    @PostMapping("/request")
    public ResponseEntity<?> sendFollowRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String toUsername
    ) {
        followService.sendFollowRequest(userDetails.getUsername(), toUsername);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/accept")
    public ResponseEntity<?> acceptFollowRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String fromUsername
    ) {
        followService.acceptFollowRequest(fromUsername, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
}
