package com.paymentchain.customer.repositories;

import com.paymentchain.customer.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long>{

    // consulta personalizada utilizando JPQL (Java Persistence Query Language). Recorre el cobjeto Customer
    // cuya columna code coincida con el parametro ?1, que es reemplazado por el argumento pasado al metodo

    @Query("SELECT c FROM Customer c WHERE c.code = ?1")
        public Customer findByCode(String code);

    @Query("SELECT c FROM Customer c WHERE c.iban = ?1")
    public Customer findByIban(String iban);

}

