package com.Digis01.FArceCajeroServicie.JPA;

public class CajeroInventario {

    public int idcajero;
    public String ubicacion;
    public int saldo;

    public int idinventario;
    public double denominacion;
    public int cantidad;
    public String tipo;

    public CajeroInventario() {

    }

    public CajeroInventario(int idcajero, String ubicacion, int saldo,
            int idinventario, double denominacion, int cantidad, String tipo) {
        this.idcajero = idcajero;
        this.ubicacion = ubicacion;
        this.saldo = saldo;
        this.idinventario = idinventario;
        this.denominacion = denominacion;
        this.cantidad = cantidad;
        this.tipo = tipo;
    }

    public CajeroInventario(int idinventario, double denominacion, int cantidad, String tipo) {
        this.idinventario = idinventario;
        this.denominacion = denominacion;
        this.cantidad = cantidad;
        this.tipo = tipo;
    }

}
