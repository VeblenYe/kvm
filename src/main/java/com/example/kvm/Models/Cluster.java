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
public class Cluster {

    @Id
    @GeneratedValue
    private Long clusterId;

    @Column
    private String clusterName;

    @Column
    private String clusterDescription;
}
