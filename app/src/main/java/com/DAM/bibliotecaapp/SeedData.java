package com.DAM.bibliotecaapp;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SeedData {

    public static void seedIfEmpty(Context context) {
        try {
            AppDatabase db = AppDatabase.getInstance(context);

            // Si ya están TODAS las cosas básicas, no hacemos nada
            boolean hayUsuarios = db.usuarioDao().count() > 0;
            boolean hayLibros = db.libroDao().count() > 0;
            if (!hayLibros) {
                // insertar libros del JSON
            }
            boolean hayEjemplares = db.ejemplarDao().count() > 0;
            if (!hayEjemplares) {
                // insertar ejemplares del JSON
            }


            if (hayUsuarios && hayLibros && hayEjemplares) return;

            // Leer seed.json
            InputStream is = context.getAssets().open("seed.json");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);

            JSONObject root = new JSONObject(json);

            // 1) USUARIOS (solo si no hay)
            if (!hayUsuarios) {
                JSONArray usuariosJson = root.getJSONArray("usuarios");
                List<Usuario> usuarios = new ArrayList<>();

                for (int i = 0; i < usuariosJson.length(); i++) {
                    JSONObject u = usuariosJson.getJSONObject(i);
                    Usuario user = new Usuario();
                    user.nombre = u.getString("nombre");
                    user.email = u.getString("email");
                    user.password = u.getString("password");
                    user.rol = u.getString("rol");
                    usuarios.add(user);
                }
                db.usuarioDao().insertAll(usuarios);
            }

            // 2) LIBROS (solo si no hay)
            if (!hayLibros) {
                JSONArray librosJson = root.getJSONArray("libros");
                List<Libro> libros = new ArrayList<>();

                for (int i = 0; i < librosJson.length(); i++) {
                    JSONObject l = librosJson.getJSONObject(i);
                    Libro libro = new Libro();
                    libro.isbn = l.getString("isbn");
                    libro.titulo = l.getString("titulo");
                    libro.autor = l.getString("autor");
                    libros.add(libro);
                }
                db.libroDao().insertAll(libros);
            }

            // 3) EJEMPLARES (solo si no hay) - IMPORTANTE: después de libros
            if (!hayEjemplares) {
                JSONArray ejemplaresJson = root.getJSONArray("ejemplares");
                List<Ejemplar> ejemplares = new ArrayList<>();

                for (int i = 0; i < ejemplaresJson.length(); i++) {
                    JSONObject e = ejemplaresJson.getJSONObject(i);

                    String isbnLibro = e.getString("isbnLibro");
                    Libro libro = db.libroDao().getByIsbn(isbnLibro);

                    if (libro != null) {
                        Ejemplar ej = new Ejemplar();
                        ej.idLibro = libro.id;
                        ej.codigoInventario = e.getString("codigoInventario");
                        ej.estado = e.getString("estado");
                        ejemplares.add(ej);
                    }
                }

                db.ejemplarDao().insertAll(ejemplares);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
