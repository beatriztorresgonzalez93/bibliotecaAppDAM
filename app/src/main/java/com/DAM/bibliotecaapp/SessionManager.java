package com.DAM.bibliotecaapp;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREFS = "session";

    private static final String KEY_ROLE_NEW = "role";
    private static final String KEY_ROLE_OLD = "rol"; // compat
    private static final String KEY_USUARIO_ID = "usuario_id";
    private static final String KEY_BIBLIO_ID = "bibliotecario_id";

    // ✅ NUEVO
    private static final String KEY_NOMBRE = "nombre";

    private final SharedPreferences prefs;

    public SessionManager(Context ctx) {
        prefs = ctx.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    // ✅ NUEVO: guardar también nombre del lector
    public void loginLector(long usuarioId, String nombre) {
        prefs.edit()
                .putString(KEY_ROLE_NEW, "LECTOR")
                .putString(KEY_ROLE_OLD, "LECTOR")
                .putLong(KEY_USUARIO_ID, usuarioId)
                .putLong(KEY_BIBLIO_ID, -1L)
                .putString(KEY_NOMBRE, nombre == null ? "" : nombre)
                .apply();
    }

    // (opcional) mantener el método antiguo por compatibilidad
    public void loginLector(long usuarioId) {
        loginLector(usuarioId, "");
    }

    public void loginBibliotecario(long biblioId) {
        prefs.edit()
                .putString(KEY_ROLE_NEW, "BIBLIOTECARIO")
                .putString(KEY_ROLE_OLD, "BIBLIOTECARIO")
                .putLong(KEY_BIBLIO_ID, biblioId)
                .putLong(KEY_USUARIO_ID, -1L)
                // ✅ opcional: limpiar nombre en bibliotecario
                .putString(KEY_NOMBRE, "")
                .apply();
    }

    public String getRole() {
        String r = prefs.getString(KEY_ROLE_NEW, null);
        if (r == null) r = prefs.getString(KEY_ROLE_OLD, null);
        return r;
    }

    public boolean isBibliotecario() {
        return "BIBLIOTECARIO".equalsIgnoreCase(getRole());
    }

    public boolean isLector() {
        return "LECTOR".equalsIgnoreCase(getRole());
    }

    public long getUsuarioId() {
        try {
            return prefs.getLong(KEY_USUARIO_ID, -1L);
        } catch (ClassCastException e) {
            return prefs.getInt(KEY_USUARIO_ID, -1);
        }
    }

    public long getBibliotecarioId() {
        try {
            return prefs.getLong(KEY_BIBLIO_ID, -1L);
        } catch (ClassCastException e) {
            return prefs.getInt(KEY_BIBLIO_ID, -1);
        }
    }

    public boolean isLoggedIn() {
        return getUsuarioId() != -1L || getBibliotecarioId() != -1L;
    }

    // ✅ NUEVO
    public String getNombre() {
        return prefs.getString(KEY_NOMBRE, "");
    }

    public void logout() {
        prefs.edit().clear().apply();
    }
}