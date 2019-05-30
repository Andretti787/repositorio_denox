/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Pck_mantenimiento;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 *
 * @author mmarco
 */
@Entity
@Table(name = "MOLDE", catalog = "mantenimiento", schema = "")
@NamedQueries({
    @NamedQuery(name = "Molde.findAll", query = "SELECT m FROM Molde m"),
    @NamedQuery(name = "Molde.findByIdMolde", query = "SELECT m FROM Molde m WHERE m.idMolde = :idMolde"),
    @NamedQuery(name = "Molde.findByDescripcionMolde", query = "SELECT m FROM Molde m WHERE m.descripcionMolde = :descripcionMolde"),
    @NamedQuery(name = "Molde.findByNserMolde", query = "SELECT m FROM Molde m WHERE m.nserMolde = :nserMolde"),
    @NamedQuery(name = "Molde.findByCarreraExpulsionMolde", query = "SELECT m FROM Molde m WHERE m.carreraExpulsionMolde = :carreraExpulsionMolde"),
    @NamedQuery(name = "Molde.findByAperturaMinimaMolde", query = "SELECT m FROM Molde m WHERE m.aperturaMinimaMolde = :aperturaMinimaMolde"),
    @NamedQuery(name = "Molde.findByAperturaMaximaMolde", query = "SELECT m FROM Molde m WHERE m.aperturaMaximaMolde = :aperturaMaximaMolde"),
    @NamedQuery(name = "Molde.findByTama\u00f1oMolde", query = "SELECT m FROM Molde m WHERE m.tama\u00f1oMolde = :tama\u00f1oMolde"),
    @NamedQuery(name = "Molde.findByPesoMolde", query = "SELECT m FROM Molde m WHERE m.pesoMolde = :pesoMolde")})
public class Molde implements Serializable {
    @Transient
    private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ID_MOLDE")
    private Integer idMolde;
    @Column(name = "DESCRIPCION_MOLDE")
    private String descripcionMolde;
    @Column(name = "NSER_MOLDE")
    private String nserMolde;
    @Column(name = "CARRERA_EXPULSION_MOLDE")
    private Integer carreraExpulsionMolde;
    @Column(name = "APERTURA_MINIMA_MOLDE")
    private Integer aperturaMinimaMolde;
    @Column(name = "APERTURA_MAXIMA_MOLDE")
    private Integer aperturaMaximaMolde;
    @Column(name = "TAMA\u00d1O_MOLDE")
    private Integer tamañoMolde;
    @Column(name = "PESO_MOLDE")
    private Integer pesoMolde;

    public Molde() {
    }

    public Molde(Integer idMolde) {
        this.idMolde = idMolde;
    }

    public Integer getIdMolde() {
        return idMolde;
    }

    public void setIdMolde(Integer idMolde) {
        Integer oldIdMolde = this.idMolde;
        this.idMolde = idMolde;
        changeSupport.firePropertyChange("idMolde", oldIdMolde, idMolde);
    }

    public String getDescripcionMolde() {
        return descripcionMolde;
    }

    public void setDescripcionMolde(String descripcionMolde) {
        String oldDescripcionMolde = this.descripcionMolde;
        this.descripcionMolde = descripcionMolde;
        changeSupport.firePropertyChange("descripcionMolde", oldDescripcionMolde, descripcionMolde);
    }

    public String getNserMolde() {
        return nserMolde;
    }

    public void setNserMolde(String nserMolde) {
        String oldNserMolde = this.nserMolde;
        this.nserMolde = nserMolde;
        changeSupport.firePropertyChange("nserMolde", oldNserMolde, nserMolde);
    }

    public Integer getCarreraExpulsionMolde() {
        return carreraExpulsionMolde;
    }

    public void setCarreraExpulsionMolde(Integer carreraExpulsionMolde) {
        Integer oldCarreraExpulsionMolde = this.carreraExpulsionMolde;
        this.carreraExpulsionMolde = carreraExpulsionMolde;
        changeSupport.firePropertyChange("carreraExpulsionMolde", oldCarreraExpulsionMolde, carreraExpulsionMolde);
    }

    public Integer getAperturaMinimaMolde() {
        return aperturaMinimaMolde;
    }

    public void setAperturaMinimaMolde(Integer aperturaMinimaMolde) {
        Integer oldAperturaMinimaMolde = this.aperturaMinimaMolde;
        this.aperturaMinimaMolde = aperturaMinimaMolde;
        changeSupport.firePropertyChange("aperturaMinimaMolde", oldAperturaMinimaMolde, aperturaMinimaMolde);
    }

    public Integer getAperturaMaximaMolde() {
        return aperturaMaximaMolde;
    }

    public void setAperturaMaximaMolde(Integer aperturaMaximaMolde) {
        Integer oldAperturaMaximaMolde = this.aperturaMaximaMolde;
        this.aperturaMaximaMolde = aperturaMaximaMolde;
        changeSupport.firePropertyChange("aperturaMaximaMolde", oldAperturaMaximaMolde, aperturaMaximaMolde);
    }

    public Integer getTamañoMolde() {
        return tamañoMolde;
    }

    public void setTamañoMolde(Integer tamañoMolde) {
        Integer oldTamañoMolde = this.tamañoMolde;
        this.tamañoMolde = tamañoMolde;
        changeSupport.firePropertyChange("tama\u00f1oMolde", oldTamañoMolde, tamañoMolde);
    }

    public Integer getPesoMolde() {
        return pesoMolde;
    }

    public void setPesoMolde(Integer pesoMolde) {
        Integer oldPesoMolde = this.pesoMolde;
        this.pesoMolde = pesoMolde;
        changeSupport.firePropertyChange("pesoMolde", oldPesoMolde, pesoMolde);
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idMolde != null ? idMolde.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Molde)) {
            return false;
        }
        Molde other = (Molde) object;
        if ((this.idMolde == null && other.idMolde != null) || (this.idMolde != null && !this.idMolde.equals(other.idMolde))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Pck_mantenimiento.Molde[ idMolde=" + idMolde + " ]";
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }
    
}
