package com.staticconstants.flowpad.backend.db.users;

import com.staticconstants.flowpad.backend.db.DbRecord;
import com.staticconstants.flowpad.backend.security.HashedPassword;
import com.staticconstants.flowpad.backend.security.PasswordHasher;

import java.util.Objects;
import java.util.UUID;

public class User implements DbRecord {

    private final UUID id;
    private String firstName;
    private String lastName;
    private String username;
    private final HashedPassword hashedPassword;

//TODO: Check password length is appropriate length for hashing & insertion to database
    public User(String firstName, String lastName, String username, char[] password) throws Exception
    {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.hashedPassword = PasswordHasher.hashPassword(password);
        this.id = UUID.randomUUID();
    }

    private User(UUID id, String firstName, String lastName, String username, HashedPassword hp)
    {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.hashedPassword = hp;
    }

    public static User fromExisting(UUID id, String firstName, String lastName, String username, HashedPassword hp)
    {
        return new User(id, firstName, lastName, username, hp);
    }

    @Override
    public UUID getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public HashedPassword getHashedPassword() {
        return hashedPassword;
    }


    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(getId(), user.getId()) && Objects.equals(getFirstName(), user.getFirstName()) && Objects.equals(getLastName(), user.getLastName()) && Objects.equals(getHashedPassword(), user.getHashedPassword());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getFirstName(), getLastName(), getHashedPassword());
    }

}



