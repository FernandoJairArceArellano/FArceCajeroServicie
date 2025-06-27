package com.Digis01.FArceCajeroServicie.RestController;

import com.Digis01.FArceCajeroServicie.DAO.UsuarioDAOImplementation;
import com.Digis01.FArceCajeroServicie.JPA.Result;
import com.Digis01.FArceCajeroServicie.JPA.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/usuarioapi/v1")
public class UsuarioRestController {

    @Autowired
    private UsuarioDAOImplementation usuarioDAOImplementation;

    @GetMapping
    public ResponseEntity GetAll() {
        Result result = usuarioDAOImplementation.GetAllJPA();
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

    @PutMapping("updateStatus")
    public ResponseEntity<Result> updateStatus(@RequestBody Usuario usuario) {
        Result result = usuarioDAOImplementation.UpdateStatusJPA(usuario);

        if (result.correct) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }
    }
}
