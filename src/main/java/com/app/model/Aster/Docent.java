package com.app.model.Aster;

public class Docent {
    private String id;
    private String nombre;

    public Docent(String id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

	/* Getters y Setters */
    
    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setId(String id) {
		this.id = id;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	@Override
    public String toString() {
        return nombre + " - " + id;
    }
}

