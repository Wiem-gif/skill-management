package com.example.skill_management.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;

@Table("users")
public class User {
    @Id
    private Long id;
    private String firstname;
    private String lastname;
    private String email;
    private String password;
    private String createdBy;
    private LocalDateTime creationDate;
    private String role;
    private boolean status;
    private boolean protectedFlag;

    public User() {
    }

    public User(Long id, String firstname, String lastname, String email, String password,
                String createdBy, LocalDateTime creationDate, String role,
                boolean status, boolean protectedFlag) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.password = password;
        this.createdBy = createdBy;
        this.creationDate = creationDate;
        this.role = role;
        this.status = status;
        this.protectedFlag = protectedFlag;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFirstname() { return firstname; }
    public void setFirstname(String firstname) { this.firstname = firstname; }

    public String getLastname() { return lastname; }
    public void setLastname(String lastname) { this.lastname = lastname; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDateTime creationDate) { this.creationDate = creationDate; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isStatus() { return status; }
    public void setStatus(boolean status) { this.status = status; }

    public boolean isProtectedFlag() { return protectedFlag; }
    public void setProtectedFlag(boolean protectedFlag) { this.protectedFlag = protectedFlag; }

    public static UserBuilder builder() { return new UserBuilder(); }

    public static class UserBuilder {
        private Long id;
        private String firstname;
        private String lastname;
        private String email;
        private String password;
        private String createdBy;
        private LocalDateTime creationDate;
        private String role;
        private boolean status;
        private boolean protectedFlag;

        public UserBuilder id(Long id) { this.id = id; return this; }
        public UserBuilder firstname(String firstname) { this.firstname = firstname; return this; }
        public UserBuilder lastname(String lastname) { this.lastname = lastname; return this; }
        public UserBuilder email(String email) { this.email = email; return this; }
        public UserBuilder password(String password) { this.password = password; return this; }
        public UserBuilder createdBy(String createdBy) { this.createdBy = createdBy; return this; }
        public UserBuilder creationDate(LocalDateTime creationDate) { this.creationDate = creationDate; return this; }
        public UserBuilder role(String role) { this.role = role; return this; }
        public UserBuilder status(boolean status) { this.status = status; return this; }
        public UserBuilder protectedFlag(boolean protectedFlag) { this.protectedFlag = protectedFlag; return this; }

        public User build() {
            return new User(id, firstname, lastname, email, password, createdBy, creationDate, role, status, protectedFlag);
        }
    }
}
