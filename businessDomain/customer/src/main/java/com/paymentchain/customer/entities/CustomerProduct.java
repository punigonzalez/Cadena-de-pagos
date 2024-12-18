package com.paymentchain.customer.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class CustomerProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private long productId;
    @Transient
    private String productName;

    @JsonIgnore //para evitar recursividad infinita (no traer infinitamente los productos)
    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Customer.class)
    @JoinColumn(name="customerId", nullable = true)
    private Customer customer;
}
