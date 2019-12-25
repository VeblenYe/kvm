package com.example.kvm.Repository;

import com.example.kvm.Models.VMachine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VMachineRepository extends JpaRepository<VMachine, Integer> {
    List<VMachine> findByHostId(Long host_id);
}
