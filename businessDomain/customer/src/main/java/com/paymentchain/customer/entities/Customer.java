package com.paymentchain.customer.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;


@Data
@Entity
public class Customer {

    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private long id;
    private String code;
    private String name;
    private String surname;
    private String phone;
    private String adress;
    private String iban;


    @OneToMany(fetch = FetchType.LAZY,mappedBy = "customer",cascade=CascadeType.ALL,orphanRemoval = true)
    private List<CustomerProduct> products;

    @Transient
    private List<?> transactions;



}
