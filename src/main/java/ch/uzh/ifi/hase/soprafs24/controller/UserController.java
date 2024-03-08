package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserUpdateDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * User Controller
 * This class is responsible for handling all REST request that are related to
 * the user.
 * The controller will receive the request and delegate the execution to the
 * UserService and finally return the result.
 */
@RestController
public class UserController {

  private final UserService userService;

  UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/users")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public List<UserGetDTO> getAllUsers() {
    // fetch all users in the internal representation
    List<User> users = userService.getUsers();
    List<UserGetDTO> userGetDTOs = new ArrayList<>();

    // convert each user to the API representation
    for (User user : users) {
      userGetDTOs.add(DTOMapper.INSTANCE.convertEntityToUserGetDTO(user));
    }
    return userGetDTOs;
  }

  @GetMapping("/users/{userId}")
  public ResponseEntity<?> getSingleUserById(@PathVariable Long userId) {
      User user = userService.findUserById(userId);
      if (user == null) {
          Map<String, String> errorDetails = new HashMap<>();
          errorDetails.put("Error", "User id " + userId + " was not found");
          // Return the error details with a NOT_FOUND status
          return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDetails);
      }
      return ResponseEntity.ok(user);
  }


  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody UserPostDTO userPostDTO) {
      try {
          boolean isAuthenticated = userService.authenticate(userPostDTO.getUsername(), userPostDTO.getPassword());
          
          if (isAuthenticated) {
              return ResponseEntity.ok("Login successful");
          } else {
              return ResponseEntity.badRequest().body("Invalid username or password");
          }
      } catch (Exception e) {
          return ResponseEntity.internalServerError().body("An error occurred during the login process");
      }
  }

  @PostMapping("/users")
  public ResponseEntity<?> createUser(@RequestBody UserPostDTO userPostDTO) {
      try {
          // convert API user to internal representation
          User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

          // create user
          User createdUser = userService.createUser(userInput);
          
          // convert internal representation of user back to API
          UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(createdUser);
          
          return ResponseEntity.status(HttpStatus.CREATED).body(userGetDTO);

      } catch (Exception e) {
          Map<String, String> errorDetails = new HashMap<>();
          errorDetails.put("Error", "Add User failed because username already exists");

          return ResponseEntity
                      .status(HttpStatus.CONFLICT)
                      .body(errorDetails);
      }
  }

  @PutMapping("/users/{userId}")
  // @PreAuthorize("#userId == principal.id")
  public ResponseEntity<?> updateUser(@PathVariable Long userId, @RequestBody UserUpdateDTO userUpdateDTO) {
      try {
          User user = userService.findUserById(userId);
          if (user == null) {
            Map<String, String> errorDetails = new HashMap<>();
            errorDetails.put("Error", "User id " + userId + " was not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDetails);
          }

          DTOMapper.INSTANCE.updateUserFromDto(userUpdateDTO, user);

          User updatedUser = userService.updateUser(user);

          DTOMapper.INSTANCE.convertEntityToUserGetDTO(updatedUser);

          return ResponseEntity.noContent().build();

      } catch (Exception e) {
          return ResponseEntity.internalServerError().build();
      }
  }

}
