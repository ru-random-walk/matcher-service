package ru.randomwalk.matcherservice.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.randomwalk.matcherservice.model.dto.ClubDto;
import ru.randomwalk.matcherservice.model.entity.Club;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ClubMapper {
    @Mapping(target = "id", source = "clubId")
    ClubDto toDto(Club club);

    List<ClubDto> toDtos(List<Club> clubs);
}
