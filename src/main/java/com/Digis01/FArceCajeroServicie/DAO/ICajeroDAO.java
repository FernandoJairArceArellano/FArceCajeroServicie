package com.Digis01.FArceCajeroServicie.DAO;

import com.Digis01.FArceCajeroServicie.JPA.Cajero;
import com.Digis01.FArceCajeroServicie.JPA.Result;

public interface ICajeroDAO {

    Result AddJPA(Cajero cajero);
    
    Result GetAllJPA();
}
