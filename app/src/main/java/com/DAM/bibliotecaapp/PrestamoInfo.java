package com.DAM.bibliotecaapp;

public class PrestamoInfo {
    public int idPrestamo;

    public int idEjemplar;
    public String codigoInventario;

    public int idLibro;
    public String titulo;   // ajusta si en Libro se llama distinto
    public String autor;    // ajusta si en Libro se llama distinto

    public long fechaPrestamo;
    public long fechaVencimiento;

    public String estado;
}
