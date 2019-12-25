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
    private Long hostId;

    @Column
    private String hostName;

    @Column
    private String hostPassword;

    @Column
    private String hostDescription;

    @Column
    private String ipAddr;

    @Column
    private Long clusterId;

    @Column
    private String hostStatus;

    @Column
    private String hostModel;

    @Column
    private Integer hostCpus;

    @Column
    private Long hostMemory;

    @Column
    private String hostType;
}
