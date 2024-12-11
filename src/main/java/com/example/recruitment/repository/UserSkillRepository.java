package com.example.recruitment.repository;

import com.example.recruitment.model.entity.UserSkill;
import com.example.recruitment.model.entity.UserSkillId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSkillRepository extends JpaRepository<UserSkill, UserSkillId> {

}
