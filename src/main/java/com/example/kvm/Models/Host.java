package com.example.kvm.Models;


import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Data
@NoArgsConstructor
public class Host {

    @Id
    @GeneratedValue
    private Long host_id;

    @Column
    private String host_name;

    @Column
    private String host_password;

    @Column
    private String host_description;

    @Column
    private String ipAddr;

    @Column
    private Long cluster_id;

    @Column
    private String host_status;

    @Column
    private String host_model;

    @Column
    private Integer host_cpus;

    @Column
    private Long host_memory;

    @Column
    private String host_type;
}
