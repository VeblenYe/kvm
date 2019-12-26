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
    private Long vmId;

    @Column
    private String vmName;

    @Column
    private String vmMemLoad;

    @Column
    private String vmUuid;

    @Column
    private String vmState;

    @Column
    private String vmDescription;

    @Column
    private Long hostId;

    @Column
    private Long clusterId;

    @Column
    private Integer vmCpus;

    @Column
    private Long vmMemory;

    @Column
    private String os;
}
