package ru.randomwalk.matcherservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.random.walk.dto.RegisteredUserInfoEvent;
import ru.randomwalk.matcherservice.model.dto.request.AddPersonDto;
import ru.randomwalk.matcherservice.service.PersonService;

@RestController
@Slf4j
@RequiredArgsConstructor
@Profile({"test", "local"})
public class TestController {

    private final PersonService personService;

    @PostMapping("/test/add-person")
    public void addPerson(@RequestBody RegisteredUserInfoEvent userInfoEvent) {
        personService.addNewPerson(userInfoEvent);
    }

}
