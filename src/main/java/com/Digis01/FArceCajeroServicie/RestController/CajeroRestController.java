package com.Digis01.FArceCajeroServicie.RestController;

import com.Digis01.FArceCajeroServicie.DAO.CajeroDAOImplementation;
import com.Digis01.FArceCajeroServicie.JPA.Cajero;
import com.Digis01.FArceCajeroServicie.JPA.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cajeroapi/v1")
public class CajeroRestController {

    @Autowired
    private CajeroDAOImplementation cajeroDAOImplementation;

    @PostMapping("add/{ubicacion}")
    public ResponseEntity addCajero(@PathVariable String ubicacion) {
        Cajero cajero = new Cajero();
        cajero.setUbicacion(ubicacion); // Asegúrate de tener setUbicacion

        Result result = cajeroDAOImplementation.AddJPA(cajero);

        if (result.correct) {
            return ResponseEntity.ok("Cajero abierto correctamente");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result.errorMessasge);
        }
    }

    @GetMapping("/todos")
    public ResponseEntity GetAll() {
        Result result = cajeroDAOImplementation.GetAllJPA();
        
        if (result.correct) {
            if (result.objects.isEmpty()) {
                return ResponseEntity.status(204).body(null);
            } else {
                return ResponseEntity.ok(result);
            }
        } else {
            return ResponseEntity.status(404).body(null);
        }
    }

    @GetMapping("/getbyid/{id}")
    public ResponseEntity getById(@PathVariable int id) {
        Result result = cajeroDAOImplementation.GetById(id);
        if (result.correct) {
            return ResponseEntity.ok(result.objects); // Regresa lista de CajeroInventarioDTO
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result.errorMessasge);
        }
    }

    @PostMapping("/retirar/{id}/{monto}")
    public ResponseEntity retirar(@PathVariable int id, @PathVariable double monto) {
        Result result = cajeroDAOImplementation.RetirarDinero(id, monto);
        if (result.correct) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(result.errorMessasge);
        }
    }

    @GetMapping("/simularCambioDia")
    public ResponseEntity<String> simularCambioDia() {
        Result resultado = cajeroDAOImplementation.RellenarInventarioCajeros();

        if (resultado.correct) {
            return ResponseEntity.ok("Cajeros rellenados correctamente.");
        } else {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al rellenar cajeros: " + resultado.errorMessasge);
        }
    }

    @GetMapping("/simularCambioDia/{id}")
    public ResponseEntity<String> simularCambioDiaPorCajero(@PathVariable int id) {
        Result resultado = cajeroDAOImplementation.RellenarInventarioPorCajero(id);

        if (resultado.correct) {
            return ResponseEntity.ok("Cajero rellenado correctamente.");
        } else {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al rellenar cajero: " + resultado.errorMessasge);
        }
    }

}
