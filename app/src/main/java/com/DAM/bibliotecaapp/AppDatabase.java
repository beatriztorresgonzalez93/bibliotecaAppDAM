package com.DAM.bibliotecaapp;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

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
}
