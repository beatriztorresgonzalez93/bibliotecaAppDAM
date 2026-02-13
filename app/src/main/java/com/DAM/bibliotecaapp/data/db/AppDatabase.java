package com.DAM.bibliotecaapp.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.DAM.bibliotecaapp.data.dao.EjemplarDao;
import com.DAM.bibliotecaapp.data.dao.LibroDao;
import com.DAM.bibliotecaapp.data.dao.PrestamoDao;
import com.DAM.bibliotecaapp.data.dao.UsuarioDao;
import com.DAM.bibliotecaapp.data.entities.Ejemplar;
import com.DAM.bibliotecaapp.data.entities.Libro;
import com.DAM.bibliotecaapp.data.entities.Prestamo;
import com.DAM.bibliotecaapp.data.entities.Usuario;

@Database(entities = {Usuario.class, Libro.class, Ejemplar.class, Prestamo.class}, version = 7)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UsuarioDao usuarioDao();
    public abstract LibroDao libroDao();

    public abstract EjemplarDao ejemplarDao();

    public abstract PrestamoDao prestamoDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "biblioteca_db"
                            )
                            .fallbackToDestructiveMigration()
                            .allowMainThreadQueries() // luego lo quitamos
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public void runInTx(Runnable r) {
        runInTransaction(r);
    }

}
