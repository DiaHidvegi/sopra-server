package ch.uzh.ifi.hase.soprafs24.rest.mapper;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserUpdateDTO;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * DTOMapper
 * This class is responsible for generating classes that will automatically
 * transform/map the internal representation
 * of an entity (e.g., the User) to the external/API representation (e.g.,
 * UserGetDTO for getting, UserPostDTO for creating)
 * and vice versa.
 * Additional mappers can be defined for new entities.
 * Always created one mapper for getting information (GET) and one mapper for
 * creating information (POST).
 */

@Mapper(componentModel = "spring")
public interface DTOMapper {

  DTOMapper INSTANCE = Mappers.getMapper(DTOMapper.class);
  @Mapping(source = "password", target = "password")
  @Mapping(source = "username", target = "username")
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "creationDate", ignore = true)
  @Mapping(target = "token", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "birthday", ignore = true)
  User convertUserPostDTOtoEntity(UserPostDTO userPostDTO);

  @Mapping(target = "creationDate", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "token", ignore = true)
  @Mapping(target = "password", ignore = true)
  @Mapping(target = "birthday", source = "birthday")
  @Mapping(source = "username", target = "username")
  User updateUserFromDto(UserUpdateDTO userUpdateDTO, @MappingTarget User user);

  @Mapping(source = "id", target = "id")
  @Mapping(source = "password", target = "password")
  @Mapping(source = "username", target = "username")
  @Mapping(source = "status", target = "status")
  @Mapping(source = "birthday", target = "birthday")
  UserGetDTO convertEntityToUserGetDTO(User user);

}