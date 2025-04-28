package ru.randomwalk.matcherservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.random.walk.dto.UserExcludeEvent;
import ru.random.walk.dto.UserJoinEvent;
import ru.randomwalk.matcherservice.model.entity.Club;
import ru.randomwalk.matcherservice.repository.ClubRepository;
import ru.randomwalk.matcherservice.service.ClubService;
import ru.randomwalk.matcherservice.service.mapper.ClubMapper;

import java.util.UUID;

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

    @Override
    public void removeUserFromClub(UserExcludeEvent userExcludeEvent) {
        UUID personId = userExcludeEvent.userId();
        UUID clubId = userExcludeEvent.clubId();
        log.info("Removing person {} from club {}", personId, clubId);

        var id = new Club.PersonClubId(personId, clubId);
        clubRepository.findById(id).ifPresent(this::deleteClub);
    }

    private void deleteClub(Club club) {
        clubRepository.delete(club);
        log.info("Club {} has been deleted", club.getClubId());
    }
}
