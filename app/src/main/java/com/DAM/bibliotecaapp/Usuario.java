package com.DAM.bibliotecaapp;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Usuario {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String nombre;
    public String email;
    public String password;
    public String rol;
}

