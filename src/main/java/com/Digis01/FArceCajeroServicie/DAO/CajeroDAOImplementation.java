package com.Digis01.FArceCajeroServicie.DAO;

import com.Digis01.FArceCajeroServicie.JPA.Cajero;
import com.Digis01.FArceCajeroServicie.JPA.Result;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class CajeroDAOImplementation implements ICajeroDAO {

    @Autowired
    private EntityManager entityManager;

    @Transactional
    @Override
    public Result AddJPA(Cajero cajero) {
        Result result = new Result();
        try {
            entityManager.createStoredProcedureQuery("CrearCajeroInventario")
                    .registerStoredProcedureParameter("p_ubicacion", String.class, jakarta.persistence.ParameterMode.IN)
                    .setParameter("p_ubicacion", cajero.getUbicacion())
                    .execute();

            result.correct = true;
        } catch (Exception ex) {
            result.correct = false;
            result.errorMessasge = ex.getLocalizedMessage();
            result.ex = ex;
        }
        return result;
    }

    @Override
    public Result GetAllJPA() {
        Result result = new Result();
        try {
            List<Cajero> cajeros = entityManager.createQuery("FROM Cajero ORDER BY idcajero", Cajero.class)
                    .getResultList();

            result.objects = new ArrayList<Object>(cajeros);
            result.correct = true;
        } catch (Exception ex) {
            ex.printStackTrace();
            result.correct = false;
            result.errorMessasge = ex.getLocalizedMessage();
            result.ex = ex;
        }
        return result;
    }

}
