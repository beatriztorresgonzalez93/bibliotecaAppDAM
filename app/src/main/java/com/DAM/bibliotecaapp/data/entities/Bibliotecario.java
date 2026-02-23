package com.DAM.bibliotecaapp.data.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "bibliotecarios",
        indices = {@Index(value = {"email"}, unique = true)}
)
public class Bibliotecario {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String nombre;

    @NonNull
    public String email;

    @NonNull
    public String password;

    public long createdAt;

    public Bibliotecario(@NonNull String nombre,
                         @NonNull String email,
                         @NonNull String password,
                         long createdAt) {
        this.nombre = nombre;
        this.email = email;
        this.password = password;
        this.createdAt = createdAt;
    }
}