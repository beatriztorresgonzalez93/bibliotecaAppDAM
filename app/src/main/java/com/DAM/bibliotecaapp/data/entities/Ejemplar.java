package com.DAM.bibliotecaapp.data.entities;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "ejemplar",
        foreignKeys = @ForeignKey(
                entity = Libro.class,
                parentColumns = "id",
                childColumns = "idLibro",
                onDelete = ForeignKey.RESTRICT
        ),
        indices = {@Index("idLibro"), @Index(value = {"codigoInventario"}, unique = true)}
)
public class Ejemplar {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int idLibro; // FK a Libro.id

    public String codigoInventario; // ej: HP-001, QUI-001 (único)

    public String estado; // "DISPONIBLE", "PRESTADO", "BAJA"
}
