package com.myapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.myapp.model.User;

public interface portfolioRepository extends JpaRepository<User, String> {
	@Query("SELECT u FROM User u WHERE u.email = :email")
	public User findByEmail(String email);
}
