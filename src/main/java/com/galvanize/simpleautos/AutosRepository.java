package com.galvanize.simpleautos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AutosRepository extends JpaRepository<Automobile, Long> {
    List<Automobile> findByColorAndMake(String color, String make);
    List<Automobile> findByColor(String color);
    List<Automobile> findByMake(String make);
    Optional<Automobile> findByVin(String vin);
}
