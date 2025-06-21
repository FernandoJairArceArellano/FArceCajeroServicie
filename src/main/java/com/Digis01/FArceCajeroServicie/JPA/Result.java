package com.Digis01.FArceCajeroServicie.JPA;

import java.util.List;

public class Result<T> {

    public boolean correct;
    public String errorMessasge;
    public Exception ex;
    public T object;
    public List<T> objects;
}
