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
public class VMachine {

    @Id
    @GeneratedValue
    private Long vm_id;

    @Column
    private String vm_name;

    @Column
    private String vm_uuid;

    @Column
    private String vm_state;

    @Column
    private String vm_description;

    @Column
    private Long host_id;

    @Column
    private Integer vm_cpus;

    @Column
    private Long vm_memory;

    @Column
    private String os;
}
