package com.mortgagebank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.mortgagebank.model.MortgageApplication;

@Repository
public interface MortgageRepository extends JpaRepository<MortgageApplication, Long> {
}
