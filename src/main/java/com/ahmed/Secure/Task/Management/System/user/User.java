package com.ahmed.Secure.Task.Management.System.user;

import com.ahmed.Secure.Task.Management.System.task.Task;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
@Component
@Table(name= "users")
public class User {

    @Id
    @GeneratedValue
    private Integer id;

    private String name;

    @Column(unique= true)
    private String email;

    private String password;

    private String role;

    private boolean enabled;

}
