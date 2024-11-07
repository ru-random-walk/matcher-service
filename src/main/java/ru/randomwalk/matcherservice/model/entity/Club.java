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
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "PERSON_CLUB")
public class Club {

    @EmbeddedId
    private PersonClubId personClubId;

    @Column(name = "IN_FILTER")
    private boolean inFilter = false;

    @Embeddable
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PersonClubId implements Serializable {
        @Column(name = "PERSON_ID")
        private UUID personId;
        @Column(name = "CLUB_ID")
        private UUID clubId;
    }
}
