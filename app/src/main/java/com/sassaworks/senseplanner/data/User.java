package com.sassaworks.senseplanner.data;

public class User {

  private String displayname;
  private String email;
  private String phoneNumber;

  public User()
  {}

  public User(String displayname, String email, String phoneNumber)
  {
    this.displayname = displayname;
    this.email = email;
    this.phoneNumber = phoneNumber;
  }

  public String getDisplayname() {
    return displayname;
  }

  public void setDisplayname(String displayname) {
    this.displayname = displayname;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }
}