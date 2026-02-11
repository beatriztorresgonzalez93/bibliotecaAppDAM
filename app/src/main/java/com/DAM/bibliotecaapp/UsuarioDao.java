package com.DAM.bibliotecaapp;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface UsuarioDao {

    @Insert
    void insert(Usuario usuario);

    @Insert
    void insertAll(List<Usuario> usuarios);

    @Query("SELECT * FROM Usuario")
    List<Usuario> getAll();

    @Query("SELECT COUNT(*) FROM Usuario")
    int count();

    @Query("SELECT * FROM Usuario WHERE email = :email AND password = :password LIMIT 1")
    Usuario login(String email, String password);
}

