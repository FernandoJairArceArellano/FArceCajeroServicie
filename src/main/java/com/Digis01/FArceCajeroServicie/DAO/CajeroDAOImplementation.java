package com.Digis01.FArceCajeroServicie.DAO;

import com.Digis01.FArceCajeroServicie.JPA.Cajero;
import com.Digis01.FArceCajeroServicie.JPA.CajeroInventario;
import com.Digis01.FArceCajeroServicie.JPA.ItemEntregado;
import com.Digis01.FArceCajeroServicie.JPA.Result;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

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
            StoredProcedureQuery queryGetById = entityManager
                    .createStoredProcedureQuery("GetCajeroInventario")
                    .registerStoredProcedureParameter("p_idcajero", Integer.class, ParameterMode.IN)
                    .registerStoredProcedureParameter("p_cursor", void.class, ParameterMode.REF_CURSOR)
                    .setParameter("p_idcajero", idcajero);

            queryGetById.execute();

            List<Object[]> datos = queryGetById.getResultList();
            List<CajeroInventario> lista = new ArrayList<>();

            for (Object[] fila : datos) {
                int idCajero = ((Number) fila[0]).intValue();
                String ubicacion = (String) fila[1];
                int saldo = ((Number) fila[2]).intValue();

                if (fila[3] != null) {
                    int idInventario = ((Number) fila[3]).intValue();
                    double denominacion = ((Number) fila[4]).doubleValue();
                    int cantidad = ((Number) fila[5]).intValue();
                    String tipo = (String) fila[6];

                    CajeroInventario cajeroInventario = new CajeroInventario(
                            idCajero, ubicacion, saldo, idInventario, denominacion, cantidad, tipo);
                    lista.add(cajeroInventario);
                } else {
                    // Si el cajero no tiene inventario (muy raro, pero por si acaso)
                    CajeroInventario cajeroInventario = new CajeroInventario();
                    cajeroInventario.idcajero = idCajero;
                    cajeroInventario.ubicacion = ubicacion;
                    cajeroInventario.saldo = saldo;
                    lista.add(cajeroInventario);
                }
            }

            result.objects = new ArrayList<>(lista);
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

            if (monto <= 0) {
                result.correct = false;
                result.errorMessasge = "La cantidad a retirar debe de ser mayor a 0.";
                return result;
            }

            // Obtener el inventario del cajero, ordenado por denominación descendente
            List<com.Digis01.FArceCajeroServicie.JPA.CajeroInventario> inventario = entityManager.createQuery("""
                SELECT new com.Digis01.FArceCajeroServicie.JPA.CajeroInventario(
                    i.idinventario, i.denominacion, i.cantidad, i.tipo
                )
                    FROM Inventario i
                    WHERE i.cajero.idcajero = :idCajero
                    ORDER BY i.denominacion DESC
                """, com.Digis01.FArceCajeroServicie.JPA.CajeroInventario.class)
                    .setParameter("idCajero", idCajero)
                    .getResultList();

            double restante = monto;
            List<Integer> idsActualizar = new ArrayList<>();
            List<Integer> nuevasCantidades = new ArrayList<>();

            // Lista para registrar los billetes o monedas entregadas
            List<ItemEntregado> entregado = new ArrayList<>();

            // Recorrer el inventario
            for (CajeroInventario elemento : inventario) {
                int idInventario = elemento.idinventario;
                double denominacion = elemento.denominacion;
                int cantidadDisponible = elemento.cantidad;
                String tipo = elemento.tipo;

                // Calcular cuántos usar
                int cantidadUsar = (int) (restante / denominacion);

                // Limitar a lo que hay disponible
                if (cantidadUsar > cantidadDisponible) {
                    cantidadUsar = cantidadDisponible;
                }

                if (cantidadUsar > 0) {
                    restante -= cantidadUsar * denominacion;

                    idsActualizar.add(idInventario);
                    nuevasCantidades.add(cantidadDisponible - cantidadUsar);

                    ItemEntregado entregadoItem = new ItemEntregado();
                    entregadoItem.setDenominacion(denominacion);
                    entregadoItem.setCantidad(cantidadUsar);
                    entregadoItem.setTipo(tipo);
                    entregado.add(entregadoItem);
                }
            }

            // Si no se pudo dispensar el monto completo
            if (restante > 0) { // margen por decimales
                result.correct = false;
                result.errorMessasge = "No se puede dispensar el monto actual, lamentamos los inconvenientes.";
                return result;
            }

            // Actualizar inventario en BD
            for (int i = 0; i < idsActualizar.size(); i++) {
                entityManager.createQuery("""
                UPDATE Inventario i SET i.cantidad = :cantidad
                WHERE i.idinventario = :idInventario
            """)
                        .setParameter("cantidad", nuevasCantidades.get(i))
                        .setParameter("idInventario", idsActualizar.get(i))
                        .executeUpdate();
            }

            // Recalcular el saldo total del cajero
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
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
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

            // Valores máximos por denominación
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
                        // Se actualiza si no tiene el máximo
                        Object[] fila = inventario.get(0);
                        int idInventario = (Integer) fila[0];
                        int cantidadActual = (Integer) fila[1];

                        // Si la cantidad Maxima es mayor a la cantidad actual
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
                        // Insertar dinero en caso contrario
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
        } catch (Exception ex) {
            ex.printStackTrace();
            result.correct = false;
            result.errorMessasge = ex.getMessage();

            result.ex = ex;
        }
        return result;
    }

    @Override
    @Transactional
    public Result RellenarInventarioPorCajero(int idCajero) {
        Result result = new Result();
        try {
            // Valores máximos por denominación
            List<Object[]> montosMaximos = List.of(
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

            for (Object[] max : montosMaximos) {
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
                    // Insertar dinero si no existe
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

            result.correct = true;
        } catch (Exception ex) {
            ex.printStackTrace();
            result.correct = false;
            result.errorMessasge = ex.getMessage();
            result.ex = ex;
        }
        return result;
    }

}
