package ch.uzh.ifi.hase.soprafs24.rest.dto;

import java.util.Date;


public class UserUpdateDTO {

  private Long id;
  private String username;
  private Date birthday;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Date getBirthday() {
    return birthday;
  }

  public void setBirthday(Date birthday) {
    this.birthday = birthday;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }
}
