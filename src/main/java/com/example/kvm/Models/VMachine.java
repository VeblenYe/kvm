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
    private Integer id;

    @Column
    private Integer vm_id;

    @Column
    private String vm_name;

    @Column
    private String vm_description;

    @Column
    private Integer host_id;

    @Column
    private String vm_status;

    @Column
    private String vm_cpu;

    @Column
    private Integer vm_memory;

    @Column
    private String os;
}
