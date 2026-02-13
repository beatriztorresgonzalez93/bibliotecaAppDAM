package data.entities;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "libro", indices = {@Index(value = {"isbn"}, unique = true)})
public class Libro {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String isbn;
    public String titulo;
    public String autor;
}
