package com.smart.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smart.entities.Contact;

public interface ContactRepository extends JpaRepository<Contact, Integer>{
	// Pagination

	@Query("from Contact as c where c.user.id=:userId")
	//pageable obj have 1. Current page -page 2. contact per page-7
	public Page<Contact> findContactByUser(@Param("userId") int userId,Pageable pageable);
}
