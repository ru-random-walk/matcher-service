package ru.randomwalk.matcherservice.service;

import ru.random.walk.dto.UserJoinEvent;

public interface ClubService {

    void addUserToClub(UserJoinEvent userJoinEvent);
}
