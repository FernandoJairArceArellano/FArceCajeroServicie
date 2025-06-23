package com.Digis01.FArceCajeroServicie.DAO;

import com.Digis01.FArceCajeroServicie.JPA.Cajero;
import com.Digis01.FArceCajeroServicie.JPA.CajeroInventario;
import com.Digis01.FArceCajeroServicie.JPA.Result;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Override
    public Result GetById(int idcajero) {
        Result result = new Result();

        try {
            StoredProcedureQuery query = entityManager
                    .createStoredProcedureQuery("GetCajeroInventario")
                    .registerStoredProcedureParameter("p_idcajero", Integer.class, ParameterMode.IN)
                    .registerStoredProcedureParameter("p_cursor", void.class, ParameterMode.REF_CURSOR)
                    .setParameter("p_idcajero", idcajero);

            query.execute();

            @SuppressWarnings("unchecked")
            List<Object[]> datos = query.getResultList();

            List<Object> lista = new ArrayList<>();

            for (Object[] fila : datos) {
                CajeroInventario dto = new CajeroInventario();
                dto.idcajero = ((Number) fila[0]).intValue();
                dto.ubicacion = (String) fila[1];
                dto.saldo = ((Number) fila[2]).intValue();

                if (fila[3] != null) {
                    dto.idinventario = ((Number) fila[3]).intValue();
                    dto.denominacion = ((Number) fila[4]).doubleValue();
                    dto.cantidad = ((Number) fila[5]).intValue();
                    dto.tipo = (String) fila[6];
                }

                lista.add(dto);
            }

            result.objects = lista;
            result.correct = true;

        } catch (Exception ex) {
            ex.printStackTrace();
            result.correct = false;
            result.errorMessasge = ex.getLocalizedMessage();
            result.ex = ex;
        }

        return result;
    }

    @Override
    @Transactional
    public Result RetirarDinero(int idCajero, double monto) {
        Result result = new Result();
        try {
            // Obtener el inventario del cajero
            List<Object[]> inventarioDatos = entityManager.createQuery("""
            SELECT i.idinventario, i.denominacion, i.cantidad, i.tipo
            FROM Inventario i
            WHERE i.cajero.idcajero = :idCajero
            ORDER BY i.denominacion DESC
        """)
                    .setParameter("idCajero", idCajero)
                    .getResultList();

            double restante = monto;
            List<Integer> idsActualizar = new ArrayList<>();
            List<Integer> nuevasCantidades = new ArrayList<>();

            // Lista para registrar los billetes/monedas entregadas
            List<Map<String, Object>> entregado = new ArrayList<>();

            for (Object[] fila : inventarioDatos) {
                int idInventario = (Integer) fila[0];
                double denominacion = (Double) fila[1];
                int cantidadDisponible = (Integer) fila[2];
                String tipo = (String) fila[3];

                // Calcular cuantos utilizar sin exceder el monto a retirar
                int cantidadUsar = (int) Math.min(restante / denominacion, cantidadDisponible);

                if (cantidadUsar > 0) {
                    restante -= cantidadUsar * denominacion;
                    restante = Math.round(restante * 100.0) / 100.0;

                    idsActualizar.add(idInventario);
                    nuevasCantidades.add(cantidadDisponible - cantidadUsar);

                    // Registrar lo que se entrega
                    Map<String, Object> item = new HashMap<>();
                    item.put("denominacion", denominacion);
                    item.put("cantidad", cantidadUsar);
                    item.put("tipo", tipo);
                    entregado.add(item);
                }
            }

            if (restante > 0) {
                result.correct = false;
                result.errorMessasge = "No se puede dispensar el monto actual, lamentamos los inconvenientes.";
                return result;
            }

            // Actualizar cantidades en la base de datos
            for (int i = 0; i < idsActualizar.size(); i++) {
                entityManager.createQuery("""
                UPDATE Inventario i SET i.cantidad = :cantidad
                WHERE i.idinventario = :idInventario
            """)
                        .setParameter("cantidad", nuevasCantidades.get(i))
                        .setParameter("idInventario", idsActualizar.get(i))
                        .executeUpdate();
            }

            // Recalcular saldo del cajero
            entityManager.createQuery("""
            UPDATE Cajero c SET c.saldo = (
                SELECT SUM(i.denominacion * i.cantidad)
                FROM Inventario i
                WHERE i.cajero.idcajero = :idCajero
            )
            WHERE c.idcajero = :idCajero
        """)
                    .setParameter("idCajero", idCajero)
                    .executeUpdate();

            result.correct = true;
            result.objects = new ArrayList<>(entregado);
        } catch (Exception ex) {
            ex.printStackTrace();
            result.correct = false;
            result.errorMessasge = ex.getLocalizedMessage();
            result.ex = ex;
        }
        return result;
    }

    @Override
    @Transactional
    public Result RellenarInventarioCajeros() {
        Result result = new Result();
        try {
            // Lista de todos los cajeros
            List<Integer> cajeros = entityManager.createQuery("""
            SELECT c.idcajero FROM Cajero c
        """, Integer.class).getResultList();

            // Valores máximos por denominación (como en tu procedimiento)
            List<Object[]> maximos = List.of(
                    new Object[]{"Billete", 1000.0, 2},
                    new Object[]{"Billete", 500.0, 5},
                    new Object[]{"Billete", 200.0, 10},
                    new Object[]{"Billete", 100.0, 20},
                    new Object[]{"Billete", 50.0, 30},
                    new Object[]{"Billete", 20.0, 40},
                    new Object[]{"Moneda", 10.0, 50},
                    new Object[]{"Moneda", 5.0, 100},
                    new Object[]{"Moneda", 2.0, 200},
                    new Object[]{"Moneda", 1.0, 300},
                    new Object[]{"Moneda", 0.5, 100}
            );

            for (int idCajero : cajeros) {
                for (Object[] max : maximos) {
                    String tipo = (String) max[0];
                    double denominacion = (Double) max[1];
                    int cantidadMaxima = (Integer) max[2];

                    // Verificar si ya existe ese billete/moneda en el cajero
                    List<Object[]> inventario = entityManager.createQuery("""
                    SELECT i.idinventario, i.cantidad
                    FROM Inventario i
                    WHERE i.cajero.idcajero = :idCajero AND i.denominacion = :denominacion AND i.tipo = :tipo
                """)
                            .setParameter("idCajero", idCajero)
                            .setParameter("denominacion", denominacion)
                            .setParameter("tipo", tipo)
                            .getResultList();

                    if (!inventario.isEmpty()) {
                        // Ya existe, actualizar si no tiene el máximo
                        Object[] fila = inventario.get(0);
                        int idInventario = (Integer) fila[0];
                        int cantidadActual = (Integer) fila[1];

                        if (cantidadActual < cantidadMaxima) {
                            entityManager.createQuery("""
                            UPDATE Inventario i SET i.cantidad = :max
                            WHERE i.idinventario = :id
                        """)
                                    .setParameter("max", cantidadMaxima)
                                    .setParameter("id", idInventario)
                                    .executeUpdate();
                        }
                    } else {
                        // No existe, lo insertamos
                        entityManager.createNativeQuery("""
                        INSERT INTO Inventario (denominacion, cantidad, tipo, idcajero)
                        VALUES (:denominacion, :cantidad, :tipo, :idCajero)
                    """)
                                .setParameter("denominacion", denominacion)
                                .setParameter("cantidad", cantidadMaxima)
                                .setParameter("tipo", tipo)
                                .setParameter("idCajero", idCajero)
                                .executeUpdate();
                    }
                }

                // Actualizar saldo total del cajero
                entityManager.createQuery("""
                UPDATE Cajero c SET c.saldo = (
                    SELECT SUM(i.denominacion * i.cantidad)
                    FROM Inventario i
                    WHERE i.cajero.idcajero = :idCajero
                )
                WHERE c.idcajero = :idCajero
            """)
                        .setParameter("idCajero", idCajero)
                        .executeUpdate();
            }

            result.correct = true;
        } catch (Exception e) {
            e.printStackTrace();
            result.correct = false;
            result.errorMessasge = e.getMessage();
            result.ex = e;
        }
        return result;
    }

}
