package com.Digis01.FArceCajeroServicie.DAO;

import com.Digis01.FArceCajeroServicie.JPA.Result;
import com.Digis01.FArceCajeroServicie.JPA.Usuario;

public interface IUsuarioDAO {

    Result GetAllJPA();

    Result GetById(int idUsuario);

    Result UpdateStatusJPA(Usuario usuario);

    Result UpdateUsuario(Usuario usuario);
}
