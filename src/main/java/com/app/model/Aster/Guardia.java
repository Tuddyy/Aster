package com.app.model.Aster;

public class Guardia {
    private int idHorari;
    private String grup;
    private String materia;
    private String aula;
    private String hora;         
    private String horaDesde;   
    private String horaFins;    
    private String docentAbsent;
    private String idDocentAbsent;
    private int idAbsent;

    /**
     * Constructor
     * @param idHorari         Identificador del horario
     * @param grup             Grupo
     * @param materia          Materia
     * @param aula             Aula
     * @param hora             Rango "HH:mm - HH:mm"
     * @param docentAbsent     Nombre del docente ausente
     * @param idDocentAbsent   ID del docente ausente
     * @param idAbsent         Identificador ausencia
     **/
    
    public Guardia(int idHorari, String grup, String materia, String aula, String hora,
                   String docentAbsent, String idDocentAbsent, int idAbsent) {
        this.idHorari = idHorari;
        this.grup = grup;
        this.materia = materia;
        this.aula = aula;
        this.hora = hora;
        // Separar hora en inicio y fin
        String[] partes = hora.split(" - ");
        this.horaDesde = partes.length > 0 ? partes[0] : "";
        this.horaFins = partes.length > 1 ? partes[1] : "";
        this.docentAbsent = docentAbsent;
        this.idDocentAbsent = idDocentAbsent;
        this.idAbsent = idAbsent;
    }

	/* Getters y Setters */
	
	public int getIdHorari() {
		return idHorari;
	}

	public void setIdHorari(int idHorari) {
		this.idHorari = idHorari;
	}
	
	public String getGrup() {
		return grup;
	}
	
	public String getHora() {
        return hora;
    }
     
    public String getHoraDesde() {
        return horaDesde;
    }

    public String getHoraFins() {
        return horaFins;
    }

    public String getDocentAbsent() {
        return docentAbsent;
    }

    public String getIdDocentAbsent() {
        return idDocentAbsent;
    }

    public int getIdAbsent() {
        return idAbsent;
    }

	public void setGrup(String grup) {
		this.grup = grup;
	}

	public String getMateria() {
		return materia;
	}

	public void setMateria(String materia) {
		this.materia = materia;
	}

	public String getAula() {
		return aula;
	}

	public void setAula(String aula) {
		this.aula = aula;
	}

	public void setHora(String hora) {
		this.hora = hora;
	}

	public void setHoraDesde(String horaDesde) {
		this.horaDesde = horaDesde;
	}

	public void setHoraFins(String horaFins) {
		this.horaFins = horaFins;
	}

	public void setDocentAbsent(String docentAbsent) {
		this.docentAbsent = docentAbsent;
	}

	public void setIdDocentAbsent(String idDocentAbsent) {
		this.idDocentAbsent = idDocentAbsent;
	}

	public void setIdAbsent(int idAbsent) {
		this.idAbsent = idAbsent;
	}

}
