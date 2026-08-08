package com.networkattack.demo.repository;



import org.springframework.data.jpa.repository.JpaRepository;

import com.networkattack.demo.Model.FileEntity;

public interface FileRepository extends JpaRepository<FileEntity, Long> {
}
