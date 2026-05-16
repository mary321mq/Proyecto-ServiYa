package com.upeu.review.repository;

import com.upeu.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByTecnicoId(Long tecnicoId);

    @Query("select avg(r.puntuacion) from Review r where r.tecnicoId = ?1")
    Double promedioByTecnicoId(Long tecnicoId);
}
