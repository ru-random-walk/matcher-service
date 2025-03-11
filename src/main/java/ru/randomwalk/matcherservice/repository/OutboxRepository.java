package ru.randomwalk.matcherservice.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import ru.randomwalk.matcherservice.model.entity.OutboxMessage;

import java.util.List;
import java.util.UUID;

public interface OutboxRepository extends JpaRepository<OutboxMessage, UUID> {

    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    List<OutboxMessage> findAllBySentFalse();
}
