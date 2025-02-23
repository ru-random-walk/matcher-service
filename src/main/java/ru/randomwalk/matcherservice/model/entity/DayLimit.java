package ru.randomwalk.matcherservice.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.randomwalk.matcherservice.service.util.MatcherConstants;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

import static ru.randomwalk.matcherservice.service.util.MatcherConstants.DEFAULT_WALK_COUNT;
import static ru.randomwalk.matcherservice.service.util.MatcherConstants.MAX_WALK_COUNT_FOR_DAY;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "DAY_LIMIT")
public class DayLimit {

    @EmbeddedId
    private DayLimitId dayLimitId;

    @Column(name = "WALK_COUNT")
    private Integer walkCount = DEFAULT_WALK_COUNT;

    @Embeddable
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DayLimitId implements Serializable {
        @Column(name = "PERSON_ID")
        private UUID personId;
        @Column(name = "DATE")
        private LocalDate date;
    }

    public void decrementWalkCount() {
        if (this.walkCount > 0) {
            this.walkCount--;
        }
    }

    public void incrementWalkCount() {
        if (this.walkCount < MAX_WALK_COUNT_FOR_DAY) {
            this.walkCount++;
        }
    }
}
