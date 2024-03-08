package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserUpdateDTO;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UserControllerTest
 * This is a WebMvcTest which allows to test the UserController i.e. GET/POST
 * request without actually sending them over the network.
 * This tests if the UserController works.
 */
@WebMvcTest(UserController.class)
public class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private UserService userService;

  @Test
  public void createUser_usernameAlreadyExists_throwsException() throws Exception {
      // given
      UserPostDTO userPostDTO = new UserPostDTO();
      userPostDTO.setUsername("existingUsername");

      // when userService.createUser() is called with an already existing username, then throw ResponseStatusException
      given(userService.createUser(Mockito.any())).willThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists"));

      // when/then -> do the request + validate the result
      MockHttpServletRequestBuilder postRequest = post("/users")
              .contentType(MediaType.APPLICATION_JSON)
              .content(asJsonString(userPostDTO));

      // then
      mockMvc.perform(postRequest)
              .andExpect(status().isConflict());
  }

  @Test
  public void getUserProfile_userExists_returnsUserProfile() throws Exception {
      // given
      Long userId = 1L;
      User user = new User();
      user.setId(userId);
      user.setUsername("testUsername");

      given(userService.findUserById(userId)).willReturn(user);

      // when
      MockHttpServletRequestBuilder getRequest = get("/users/{userId}", userId)
              .contentType(MediaType.APPLICATION_JSON);

      // then
      mockMvc.perform(getRequest)
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.id", is(userId.intValue())))
              .andExpect(jsonPath("$.username", is(user.getUsername())));
  }

  @Test
  public void getUserProfile_userDoesNotExist_throwsException() throws Exception {
      // given
      Long userId = 99L; // non-existing user ID

      // when userService.getUserById() is called with a non-existing userId, then throw ResponseStatusException
      given(userService.findUserById(userId)).willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

      // when
      MockHttpServletRequestBuilder getRequest = get("/users/{userId}", userId)
              .contentType(MediaType.APPLICATION_JSON);

      // then
      mockMvc.perform(getRequest)
              .andExpect(status().isNotFound());
  }

  @Test
  public void updateUserProfile_userExists_userUpdated() throws Exception {
      // given
      Long userId = 1L;
      User user = new User();
      user.setId(userId);
      user.setUsername("originalUsername");

      User updatedUser = new User();
      updatedUser.setId(userId);
      updatedUser.setUsername("updatedUsername");

      given(userService.findUserById(userId)).willReturn(user); // Assuming the method is called findById
      given(userService.updateUser(any(User.class))).willReturn(updatedUser); // Assuming the method is called save

      // when/then -> do the request + validate the result
      MockHttpServletRequestBuilder putRequest = MockMvcRequestBuilders.put("/users/{userId}", userId)
              .contentType(MediaType.APPLICATION_JSON)
              .content(asJsonString(updatedUser));

      // then
      mockMvc.perform(putRequest)
              .andExpect(status().isNoContent());
  }

  @Test
  public void updateUserProfile_userDoesNotExist_returnsNotFound() throws Exception {
      // given
      Long nonExistentUserId = 99L; // A user ID that does not exist
      UserUpdateDTO userUpdateDTO = new UserUpdateDTO();
      userUpdateDTO.setUsername("updatedUsername");
  
      // when userService.findUserById() is called with a non-existing userId, return null
      given(userService.findUserById(nonExistentUserId)).willReturn(null);
  
      // when/then -> perform the request and validate the result
      MockHttpServletRequestBuilder putRequest = MockMvcRequestBuilders.put("/users/{userId}", nonExistentUserId)
              .contentType(MediaType.APPLICATION_JSON)
              .content(asJsonString(userUpdateDTO));
  
      // then
      mockMvc.perform(putRequest)
              .andExpect(status().isNotFound())
              .andExpect(jsonPath("$.Error", is("User id " + nonExistentUserId + " was not found")));
  }

  @Test
  public void givenUsers_whenGetUsers_thenReturnJsonArray() throws Exception {
    // given
    User user = new User();
    user.setUsername("firstname@lastname");
    user.setStatus(UserStatus.OFFLINE);

    List<User> allUsers = Collections.singletonList(user);

    // this mocks the UserService -> we define above what the userService should
    // return when getUsers() is called
    given(userService.getUsers()).willReturn(allUsers);

    // when
    MockHttpServletRequestBuilder getRequest = get("/users").contentType(MediaType.APPLICATION_JSON);

    // then
    mockMvc.perform(getRequest).andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].username", is(user.getUsername())))
        .andExpect(jsonPath("$[0].status", is(user.getStatus().toString())));
  }

  @Test
  public void createUser_validInput_userCreated() throws Exception {
    // given
    User user = new User();
    user.setId(1L);
    user.setUsername("testUsername");
    user.setToken("1");
    user.setStatus(UserStatus.ONLINE);

    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setUsername("testUsername");

    given(userService.createUser(Mockito.any())).willReturn(user);

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder postRequest = post("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(userPostDTO));

    // then
    mockMvc.perform(postRequest)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(user.getId().intValue())))
        .andExpect(jsonPath("$.username", is(user.getUsername())))
        .andExpect(jsonPath("$.status", is(user.getStatus().toString())));
  }

  /**
   * Helper Method to convert userPostDTO into a JSON string such that the input
   * can be processed
   * Input will look like this: {"name": "Test User", "username": "testUsername"}
   * 
   * @param object
   * @return string
   */
  private String asJsonString(final Object object) {
    try {
      return new ObjectMapper().writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          String.format("The request body could not be created.%s", e.toString()));
    }
  }
}