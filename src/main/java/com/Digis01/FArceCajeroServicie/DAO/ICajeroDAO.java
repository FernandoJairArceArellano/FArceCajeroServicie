package com.Digis01.FArceCajeroServicie.DAO;

import com.Digis01.FArceCajeroServicie.JPA.Cajero;
import com.Digis01.FArceCajeroServicie.JPA.Result;

public interface ICajeroDAO {

    Result AddJPA(Cajero cajero);

    Result GetAllJPA();

    Result GetById(int idcajero);

    Result RetirarDinero(int idCajero, double monto);

    Result RellenarInventarioCajeros();
    
    Result RellenarInventarioPorCajero(int idCajero);
}
