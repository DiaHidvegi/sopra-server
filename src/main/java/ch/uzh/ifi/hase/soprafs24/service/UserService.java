package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.Date;
import javax.persistence.EntityNotFoundException;
import java.util.Optional;

/**
 * User Service
 * This class is the "worker" and responsible for all functionality related to
 * the user
 * (e.g., it creates, modifies, deletes, finds). The result will be passed back
 * to the caller.
 */
@Service
@Transactional
public class UserService {

  private final Logger log = LoggerFactory.getLogger(UserService.class);

  private final UserRepository userRepository;

  @Autowired
  public UserService(@Qualifier("userRepository") UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public List<User> getUsers() {
    return this.userRepository.findAll();
  }

  public User findUserById(Long id) {
    return userRepository.findById(id).orElse(null);
  }

  public UserGetDTO getUserProfileById(Long userId) {
    User user = userRepository.findById(userId)
                              .orElseThrow(() -> new EntityNotFoundException("User not found"));
    UserGetDTO dto = new UserGetDTO();
    dto.setUsername(user.getUsername());
    dto.setStatus(user.getStatus());
    dto.setCreationDate(user.getCreationDate());
    return dto;
  }

  public boolean authenticate(String username, String password) {
    User user = userRepository.findByUsername(username);
    if (user != null && user.getPassword().equals(password)) {
        return true;
    }
    return false;
  }

  public User createUser(User newUser) {
    newUser.setToken(UUID.randomUUID().toString());
    newUser.setCreationDate(new Date());
    newUser.setStatus(UserStatus.OFFLINE);
    checkIfUserExists(newUser);
    // saves the given entity but data is only persisted in the database once
    // flush() is called
    newUser = userRepository.save(newUser);
    userRepository.flush();

    log.debug("Created Information for User: {}", newUser);
    return newUser;
  }

  /**
   * Updates an existing user.
   *
   * @param user the user entity with updated fields
   * @return the updated user
   */
  public User updateUser(User user) {

    // Check if the user exists
    if (user.getId() != null && userRepository.existsById(user.getId())) {
        // Save updates to the user
        userRepository.save(user);
        userRepository.flush();

        log.debug("Updated Information for User: {}", user);
        return user;

    } else {
        // Handle the case where the user does not exist, e.g., throw an exception
        throw new RuntimeException("User not found with id: " + user.getId());
    }
  }

  /**
   * This is a helper method that will check the uniqueness criteria of the
   * username
   * defined in the User entity. The method will do nothing if the input is unique
   * and throw an error otherwise.
   *
   * @param userToBeCreated
   * @throws org.springframework.web.server.ResponseStatusException
   * @see User
   */
  private void checkIfUserExists(User userToBeCreated) {
    User userByUsername = userRepository.findByUsername(userToBeCreated.getUsername());

    if (userByUsername != null) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already taken!");
    }
  }
}