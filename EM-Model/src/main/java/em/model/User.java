package em.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name="USERS")
public class User implements Serializable {
    @Id
    @GeneratedValue(generator="increment")
    @GenericGenerator(name="increment", strategy = "increment")
    @Column(name = "id", updatable=false, nullable=false, unique=true)
    private Long id;
    @Column(name = "name", nullable=false)
    private String name;
    @Column(name = "username", nullable=false, unique=true)
    private String username;
    @Column(name = "password", nullable=false, unique=true)
    private String password;
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable=false, unique=true)
    private Role role;

    public User() {}

    public User(String name, String username, String password, Role role) {
        this.name = name;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "User {" +
                "name: '" + name + '\'' +
                ", username: '" + username + '\'' +
                ", role: " + role +
                '}';
    }
}
