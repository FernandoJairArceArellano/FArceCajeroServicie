package com.Digis01.FArceCajeroServicie.JPA;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Cajero {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idcajero;

    private int saldo;
    private String ubicacion;

    public int getIdcajero() {
        return idcajero;
    }

    public void setIdcajero(int idcajero) {
        this.idcajero = idcajero;
    }

    public int getSaldo() {
        return saldo;
    }

    public void setSaldo(int saldo) {
        this.saldo = saldo;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

}
