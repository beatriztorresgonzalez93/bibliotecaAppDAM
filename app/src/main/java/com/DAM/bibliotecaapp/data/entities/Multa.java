package com.DAM.bibliotecaapp.data.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "multa",
        foreignKeys = {
                @ForeignKey(entity = Prestamo.class,
                        parentColumns = "id",
                        childColumns = "idPrestamo",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Usuario.class,
                        parentColumns = "id",
                        childColumns = "idUsuario",
                        onDelete = ForeignKey.CASCADE)
        },
        indices = {
                @Index(value = {"idPrestamo"}, unique = true),
                @Index("idUsuario")
        }
)
public class Multa {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int idPrestamo;
    public int idUsuario;

    public long fechaCreacion;
    public Long fechaCierre;

    public int diasRetraso;
    public double importe;

    public String estado; // "PENDIENTE", "PAGADA", "CONDONADA"
}
