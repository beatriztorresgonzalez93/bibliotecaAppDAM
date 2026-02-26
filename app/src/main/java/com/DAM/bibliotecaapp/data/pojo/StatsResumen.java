package com.DAM.bibliotecaapp.data.pojo;

public class StatsResumen {
    public int totalUsuarios;
    public int totalLibros;
    public int totalEjemplares;

    public int prestamosActivos;
    public int prestamosVencidos;
    public int prestamosDevueltos;

    public int multasPendientes;
    public int multasPagadas;
    public int multasCondonadas;

    public double dineroRecaudado; // suma de PAGADA
}