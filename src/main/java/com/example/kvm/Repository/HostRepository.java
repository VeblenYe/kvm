package com.example.kvm.Repository;

import com.example.kvm.Models.Host;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HostRepository extends JpaRepository<Host, Integer> {

    List<Host> findByClusterId(Long cluster_id);

    Host findByHostId(Long host_id);

}
