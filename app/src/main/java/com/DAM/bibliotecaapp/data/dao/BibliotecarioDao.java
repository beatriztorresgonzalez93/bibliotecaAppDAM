package com.DAM.bibliotecaapp.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.DAM.bibliotecaapp.data.entities.Bibliotecario;

@Dao
public interface BibliotecarioDao {

    @Insert
    long insert(Bibliotecario b);

    @Query("SELECT COUNT(*) FROM bibliotecarios WHERE email = :email")
    int countByEmail(String email);

    @Query("SELECT * FROM bibliotecarios WHERE email = :email LIMIT 1")
    Bibliotecario findByEmail(String email);

    @Query("SELECT * FROM bibliotecarios WHERE email = :email AND password = :password LIMIT 1")
    Bibliotecario login(String email, String password);
}