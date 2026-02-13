package data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import data.entities.Usuario;

@Dao
public interface UsuarioDao {

    @Insert
    void insert(Usuario usuario);

    @Insert
    void insertAll(List<Usuario> usuarios);

    @Query("SELECT * FROM Usuario")
    List<Usuario> getAll();

    @Query("SELECT COUNT(*) FROM Usuario")
    int count();

    @Query("SELECT * FROM Usuario WHERE email = :email AND password = :password LIMIT 1")
    Usuario login(String email, String password);

    @Query("SELECT * FROM Usuario ORDER BY nombre ASC")
    List<Usuario> getAllOrderByNombre();

    @Query("SELECT * FROM Usuario WHERE (nombre LIKE :q OR email LIKE :q) ORDER BY nombre ASC")
    List<Usuario> search(String q);

    @Query("SELECT * FROM Usuario WHERE id = :id LIMIT 1")
    Usuario getById(int id);

    @Query("SELECT * FROM Usuario WHERE email = :email LIMIT 1")
    Usuario getByEmail(String email);
    @Query("SELECT email FROM Usuario ORDER BY email")
    List<String> getAllEmails();




}

