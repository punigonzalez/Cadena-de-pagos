package com.paymentchain.product.controllers;


import com.paymentchain.product.entities.Product;
import com.paymentchain.product.repositories.ProductRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/product")
@Tag(name="Productos", description = "Crud de los productos") //swagger
public class ProductController {

    @Autowired
    ProductRepository productRepository;

    //crear un producto
    @Operation(summary = "Crear un producto") //swagger
    @PostMapping
    public ResponseEntity<?> post(@RequestBody Product input){
        Product save= productRepository.save(input);
        return ResponseEntity.ok(save);
    }

    // seleccionar todos productos
    @Operation(summary = "Seleccionar todos los productos") //swagger
    @GetMapping("/all")
    public List<Product> findAll(){
        return productRepository.findAll();
    }

    // seleccionar producto por id
    @Operation(summary = "Seleccionar producto por ID") //swagger
    @GetMapping("{id}")
    public ResponseEntity<?> findById(@PathVariable ("id")Long id){
        Optional<Product> product = productRepository.findById(id);
        if(!product.isPresent()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Producto no encontrado");
        }
        return ResponseEntity.ok(product);
    }

    // modificar producto por id
    @Operation(summary = "Modificar producto por ID ") //swagger
    @PutMapping("/{id}")
    public ResponseEntity<?> put(@PathVariable(name = "id") long id, @RequestBody Product input) {
        Product find = productRepository.findById(id).get();
        if(find != null){
            find.setCode(input.getCode());
            find.setName(input.getName());
        }
        Product save = productRepository.save(find);
        return ResponseEntity.ok(save);
    }
    
    // eliminar producto por id
    @Operation(summary = "Eliminar producto por ID") //swagger
    @DeleteMapping("{id}")
    public ResponseEntity<?> eliminarPorId(@PathVariable ("id") Long id){
        Optional<Product> product = productRepository.findById(id);
        if (!product.isPresent()){
            return  ResponseEntity.status(HttpStatus.NOT_FOUND).body("Producto no encontrado");
        }
        productRepository.deleteById(id);
        return ResponseEntity.ok().body("Producto eliminado con exito");
    }




}
