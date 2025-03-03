package ru.randomwalk.matcherservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.random.walk.dto.UserJoinEvent;
import ru.randomwalk.matcherservice.model.entity.Club;
import ru.randomwalk.matcherservice.repository.ClubRepository;
import ru.randomwalk.matcherservice.service.ClubService;
import ru.randomwalk.matcherservice.service.mapper.ClubMapper;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClubServiceImpl implements ClubService {

    private final ClubRepository clubRepository;
    private final ClubMapper clubMapper;

    @Override
    public void addUserToClub(UserJoinEvent userJoinEvent) {
        Club club = clubMapper.fromUserJoinEvent(userJoinEvent);
        log.info("Adding person {} to club {}", club.getPersonId(), club.getClubId());
        clubRepository.save(club);
    }
}
