package com.example.wombaturleater.repository;

import com.example.wombaturleater.entities.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PeopleRepository  extends JpaRepository<Person, Integer> {
    Optional<Person> findByPersonName(String name);

}