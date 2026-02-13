package data.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "prestamo",
        foreignKeys = {
                @ForeignKey(entity = Usuario.class,
                        parentColumns = "id",
                        childColumns = "idUsuario",
                        onDelete = ForeignKey.RESTRICT),
                @ForeignKey(entity = Ejemplar.class,
                        parentColumns = "id",
                        childColumns = "idEjemplar",
                        onDelete = ForeignKey.RESTRICT)
        },
        indices = {@Index("idUsuario"), @Index("idEjemplar")}
)
public class Prestamo {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int idUsuario;
    public int idEjemplar;

    public long fechaPrestamo;
    public long fechaVencimiento;

    public Long fechaDevolucion; // null si aún no se devolvió

    public String estado; // "ACTIVO", "DEVUELTO", "VENCIDO"
}
