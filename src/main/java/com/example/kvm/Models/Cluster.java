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
    private Long cluster_id;

    @Column
    private String cluster_description;
}
