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

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

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
    private Integer walkCount = 1;

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
}
