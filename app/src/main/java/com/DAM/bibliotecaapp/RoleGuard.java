package com.DAM.bibliotecaapp;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import com.DAM.bibliotecaapp.ui.login.LoginActivity;

public class RoleGuard {

    public static void requireLogin(Activity a) {
        SessionManager s = new SessionManager(a.getApplicationContext());
        if (!s.isLoggedIn()) {
            a.startActivity(new Intent(a, LoginActivity.class));
            a.finish();
        }
    }

    public static void requireBibliotecario(Activity a) {
        SessionManager s = new SessionManager(a.getApplicationContext());
        if (!s.isLoggedIn()) {
            a.startActivity(new Intent(a, LoginActivity.class));
            a.finish();
            return;
        }
        if (!s.isBibliotecario()) {
            Toast.makeText(a, "Acceso solo para bibliotecario", Toast.LENGTH_SHORT).show();
            a.finish();
        }
    }
}