package ru.randomwalk.matcherservice.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
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
@IdClass(Club.PersonClubId.class)
@Table(name = "PERSON_CLUB")
public class Club {

    @Column(name = "PERSON_ID")
    @Id
    private UUID personId;

    @Column(name = "CLUB_ID")
    @Id
    private UUID clubId;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PersonClubId implements Serializable {
        private UUID personId;
        private UUID clubId;
    }
}
