package com.Digis01.FArceCajeroServicie.DAO;

import com.Digis01.FArceCajeroServicie.JPA.Result;
import com.Digis01.FArceCajeroServicie.JPA.Rol;
import com.Digis01.FArceCajeroServicie.JPA.Usuario;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class UsuarioDAOImplementation implements IUsuarioDAO {

    @Autowired
    private EntityManager entityManager;

    @Override
    public Result GetAllJPA() {
        Result result = new Result();
        try {
            List<Usuario> usuarios = entityManager.createQuery("FROM Usuario", Usuario.class)
                    .getResultList();

            result.objects = new ArrayList<Object>(usuarios);

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
    public Result GetById(int idUsuario) {
        Result result = new Result();

        try {
            Usuario usuarioJAP = entityManager.find(Usuario.class, idUsuario);

            if (usuarioJAP != null) {
                Usuario usuarioML = new Usuario();
                usuarioML.setNombre(usuarioJAP.getNombre());
                usuarioML.setApellidoPaterno(usuarioJAP.getApellidoPaterno());
                usuarioML.setApellidoMaterno(usuarioJAP.getApellidoMaterno());
                usuarioML.setUserName(usuarioJAP.getNombre());
                usuarioML.setEmail(usuarioJAP.getEmail());
                usuarioML.setPassword(usuarioJAP.getPassword());
                usuarioML.setfNacimiento(usuarioJAP.getfNacimiento());
                usuarioML.setTelefono(usuarioJAP.getTelefono());
                usuarioML.setStatus(usuarioJAP.getStatus());

                Rol rolML = new Rol();
                rolML.setIdRol(usuarioJAP.Rol.getIdRol());
                usuarioML.Rol = rolML;

                result.object = usuarioML;
                result.correct = true;

            } else {
                result.correct = false;
                result.errorMessasge = "Usuario no encontraco con Id: " + idUsuario;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            result.correct = false;
            result.errorMessasge = ex.getLocalizedMessage();
            result.ex = ex;
        }

        return result;
    }

    @Transactional
    @Override
    public Result UpdateStatusJPA(Usuario usuario) {
        Result result = new Result();

        try {
            Usuario usuarioJPA = entityManager.find(Usuario.class, usuario.getIdUsuario());

            if (usuarioJPA != null) {
                System.out.println("Usuario encontrado con el ID: " + usuario.getIdUsuario());
                usuarioJPA.setStatus(usuario.getStatus());
                entityManager.persist(usuarioJPA);
                result.correct = true;
            } else {
                result.correct = false;
                result.errorMessasge = "Usuario no encontrado con ID: " + usuario.getIdUsuario();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            result.correct = false;
            result.errorMessasge = ex.getLocalizedMessage();
            result.ex = ex;
        }
        return result;
    }

    @Transactional
    @Override
    public Result UpdateUsuario(Usuario usuario) {
        Result result = new Result();

        try {
            Usuario usuarioJPA = entityManager.find(Usuario.class, usuario.getIdUsuario());

            if (usuarioJPA != null) {
                usuarioJPA.setNombre(usuario.getNombre());
                usuarioJPA.setApellidoPaterno(usuario.getApellidoPaterno());
                usuarioJPA.setApellidoPaterno(usuario.getApellidoMaterno());
                usuarioJPA.setUserName(usuario.getUserName());
                usuarioJPA.setEmail(usuario.getEmail());
                usuarioJPA.setPassword(usuario.getPassword());
                usuarioJPA.setfNacimiento(usuario.getfNacimiento());
                usuarioJPA.setTelefono(usuario.getTelefono());
                usuarioJPA.setnCelular(usuario.getnCelular());
                usuarioJPA.setStatus(usuario.getStatus());

                Rol rolJPA = entityManager.find(Rol.class, usuario.getRol().getIdRol());

                usuarioJPA.setRol(rolJPA);

                entityManager.merge(usuarioJPA);

                result.correct = true;
            } else {
                result.correct = false;
                result.errorMessasge = "Usuario no encontrado con el ID: " + usuario.getIdUsuario();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            result.correct = false;
            result.errorMessasge = ex.getLocalizedMessage();
            result.ex = ex;
        }

        return result;
    }

}
