package com.polstat.perpustakaan.repository;

import com.polstat.perpustakaan.entity.Pinjam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "loans", path = "loans")
public interface PinjamRepository extends JpaRepository<Pinjam, Long> {
}