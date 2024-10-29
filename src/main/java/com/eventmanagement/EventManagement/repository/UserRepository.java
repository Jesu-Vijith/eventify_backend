package com.eventmanagement.EventManagement.repository;

import com.eventmanagement.EventManagement.model.entity.RoleEnum;
import com.eventmanagement.EventManagement.model.entity.User;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,String> {

    Optional<User>findByEmail(@NotEmpty(message = "UserName is required") @Pattern(regexp = "^[_A-Za-z0-9-+]+(.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(.[A-Za-z0-9]+)*(.[A-Za-z]{2,})$",
            message = "Invalid  mailId composition") String email);


    @Query("SELECT u.name FROM User u WHERE u.userId = :userId")
    Optional<String> findNameByUserId(@Param("userId") String userId);

    Optional<User> findByUserId(String userId);

    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true AND u.role = :roleEnum")
    Long countActiveUsersByRole(RoleEnum roleEnum);

    @Query("SELECT u FROM User u WHERE u.role = :role")
    List<User> findUsersByRole(RoleEnum role);
}
