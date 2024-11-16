package ru.randomwalk.matcherservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.randomwalk.matcherservice.model.dto.ClubDto;
import ru.randomwalk.matcherservice.model.dto.LocationDto;
import ru.randomwalk.matcherservice.model.dto.request.ClubFilterRequest;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/person")
public class PersonController {

    @PutMapping("/filter/clubs")
    @Operation(summary = "Change your own clubs filter")
    public List<ClubDto> changeClubFilter(@RequestBody ClubFilterRequest request, Principal principal) {
        log.info("PUT /person/filter/club from {} with body: {}", principal.getName(), request);
        return Collections.emptyList();
    }

    @PutMapping("/location")
    @Operation(summary = "Change your own location")
    public LocationDto changeLocation(@Validated @RequestBody LocationDto request, Principal principal) {
        log.info("PUT /person/location from {} with body {}", principal.getName(), request);
        return LocationDto.builder().build();
    }

    @GetMapping("/location")
    @Operation(summary = "Get your own location")
    public LocationDto getLocation(Principal principal) {
        log.info("GET /person/location from {}", principal.getName());
        return LocationDto.builder().build();
    }

    @GetMapping("/clubs")
    @Operation(summary = "Get information about clubs in filter")
    public List<ClubDto> getClubs(
            @Parameter(description = """
             If inFilter = null returns all clubs for current user.
             Otherwise returns clubs with the same inFilter value.""")
            @RequestParam(required = false)
            Boolean inFilter,
            Principal principal
    ) {
        log.info("GET /person/clubs, inFilter = {} from user {}", inFilter, principal.getName());
        return Collections.emptyList();
    }
}
