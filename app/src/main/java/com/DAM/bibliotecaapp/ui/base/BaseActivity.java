package com.DAM.bibliotecaapp.ui.base;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // ✅ Evita que el contenido se dibuje detrás de la status bar (global)
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);

        super.onCreate(savedInstanceState);

        // ✅ ICONOS NEGROS en la status bar (hora/batería visibles)
        setStatusBarDarkIcons(true);
    }

    private void setStatusBarDarkIcons(boolean darkIcons) {
        WindowInsetsControllerCompat controller =
                new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        controller.setAppearanceLightStatusBars(darkIcons);
        // darkIcons=true -> iconos negros
    }

    /**
     * Llama a esto DESPUÉS de setContentView() en cada Activity
     * y pásale el id del root (normalmente R.id.main).
     */
    protected void applySystemBarsPadding(int rootId) {
        View root = findViewById(rootId);
        if (root == null) return;

        final int start = root.getPaddingLeft();
        final int top = root.getPaddingTop();
        final int end = root.getPaddingRight();
        final int bottom = root.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            v.setPadding(
                    start + bars.left,
                    top + bars.top,
                    end + bars.right,
                    bottom + bars.bottom
            );
            return insets;
        });
    }
}