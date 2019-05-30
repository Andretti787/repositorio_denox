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
@Table(name = "MAQUINA", catalog = "mantenimiento", schema = "")
@NamedQueries({
    @NamedQuery(name = "Maquina.findAll", query = "SELECT m FROM Maquina m"),
    @NamedQuery(name = "Maquina.findByIdMaquina", query = "SELECT m FROM Maquina m WHERE m.idMaquina = :idMaquina"),
    @NamedQuery(name = "Maquina.findByDescMaquina", query = "SELECT m FROM Maquina m WHERE m.descMaquina = :descMaquina"),
    @NamedQuery(name = "Maquina.findByNserMaquina", query = "SELECT m FROM Maquina m WHERE m.nserMaquina = :nserMaquina"),
    @NamedQuery(name = "Maquina.findByCarreraExpulsionMaquina", query = "SELECT m FROM Maquina m WHERE m.carreraExpulsionMaquina = :carreraExpulsionMaquina"),
    @NamedQuery(name = "Maquina.findByAperturaMaxMaquina", query = "SELECT m FROM Maquina m WHERE m.aperturaMaxMaquina = :aperturaMaxMaquina"),
    @NamedQuery(name = "Maquina.findByMinMoldeMaquina", query = "SELECT m FROM Maquina m WHERE m.minMoldeMaquina = :minMoldeMaquina"),
    @NamedQuery(name = "Maquina.findByMaxMoldeMaquina", query = "SELECT m FROM Maquina m WHERE m.maxMoldeMaquina = :maxMoldeMaquina"),
    @NamedQuery(name = "Maquina.findByDosificacionMaxMaquina", query = "SELECT m FROM Maquina m WHERE m.dosificacionMaxMaquina = :dosificacionMaxMaquina"),
    @NamedQuery(name = "Maquina.findByIdMoldeMaquina", query = "SELECT m FROM Maquina m WHERE m.idMoldeMaquina = :idMoldeMaquina"),
    @NamedQuery(name = "Maquina.findByIdRobotMaquina", query = "SELECT m FROM Maquina m WHERE m.idRobotMaquina = :idRobotMaquina")})
public class Maquina implements Serializable {
    @Transient
    private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ID_MAQUINA")
    private Integer idMaquina;
    @Column(name = "DESC_MAQUINA")
    private String descMaquina;
    @Column(name = "NSER_MAQUINA")
    private String nserMaquina;
    @Column(name = "CARRERA_EXPULSION_MAQUINA")
    private Integer carreraExpulsionMaquina;
    @Column(name = "APERTURA_MAX_MAQUINA")
    private Integer aperturaMaxMaquina;
    @Column(name = "MIN_MOLDE_MAQUINA")
    private Integer minMoldeMaquina;
    @Column(name = "MAX_MOLDE_MAQUINA")
    private Integer maxMoldeMaquina;
    @Column(name = "DOSIFICACION_MAX_MAQUINA")
    private Integer dosificacionMaxMaquina;
    @Column(name = "ID_MOLDE_MAQUINA")
    private Integer idMoldeMaquina;
    @Column(name = "ID_ROBOT_MAQUINA")
    private Integer idRobotMaquina;

    public Maquina() {
    }

    public Maquina(Integer idMaquina) {
        this.idMaquina = idMaquina;
    }

    public Integer getIdMaquina() {
        return idMaquina;
    }

    public void setIdMaquina(Integer idMaquina) {
        Integer oldIdMaquina = this.idMaquina;
        this.idMaquina = idMaquina;
        changeSupport.firePropertyChange("idMaquina", oldIdMaquina, idMaquina);
    }

    public String getDescMaquina() {
        return descMaquina;
    }

    public void setDescMaquina(String descMaquina) {
        String oldDescMaquina = this.descMaquina;
        this.descMaquina = descMaquina;
        changeSupport.firePropertyChange("descMaquina", oldDescMaquina, descMaquina);
    }

    public String getNserMaquina() {
        return nserMaquina;
    }

    public void setNserMaquina(String nserMaquina) {
        String oldNserMaquina = this.nserMaquina;
        this.nserMaquina = nserMaquina;
        changeSupport.firePropertyChange("nserMaquina", oldNserMaquina, nserMaquina);
    }

    public Integer getCarreraExpulsionMaquina() {
        return carreraExpulsionMaquina;
    }

    public void setCarreraExpulsionMaquina(Integer carreraExpulsionMaquina) {
        Integer oldCarreraExpulsionMaquina = this.carreraExpulsionMaquina;
        this.carreraExpulsionMaquina = carreraExpulsionMaquina;
        changeSupport.firePropertyChange("carreraExpulsionMaquina", oldCarreraExpulsionMaquina, carreraExpulsionMaquina);
    }

    public Integer getAperturaMaxMaquina() {
        return aperturaMaxMaquina;
    }

    public void setAperturaMaxMaquina(Integer aperturaMaxMaquina) {
        Integer oldAperturaMaxMaquina = this.aperturaMaxMaquina;
        this.aperturaMaxMaquina = aperturaMaxMaquina;
        changeSupport.firePropertyChange("aperturaMaxMaquina", oldAperturaMaxMaquina, aperturaMaxMaquina);
    }

    public Integer getMinMoldeMaquina() {
        return minMoldeMaquina;
    }

    public void setMinMoldeMaquina(Integer minMoldeMaquina) {
        Integer oldMinMoldeMaquina = this.minMoldeMaquina;
        this.minMoldeMaquina = minMoldeMaquina;
        changeSupport.firePropertyChange("minMoldeMaquina", oldMinMoldeMaquina, minMoldeMaquina);
    }

    public Integer getMaxMoldeMaquina() {
        return maxMoldeMaquina;
    }

    public void setMaxMoldeMaquina(Integer maxMoldeMaquina) {
        Integer oldMaxMoldeMaquina = this.maxMoldeMaquina;
        this.maxMoldeMaquina = maxMoldeMaquina;
        changeSupport.firePropertyChange("maxMoldeMaquina", oldMaxMoldeMaquina, maxMoldeMaquina);
    }

    public Integer getDosificacionMaxMaquina() {
        return dosificacionMaxMaquina;
    }

    public void setDosificacionMaxMaquina(Integer dosificacionMaxMaquina) {
        Integer oldDosificacionMaxMaquina = this.dosificacionMaxMaquina;
        this.dosificacionMaxMaquina = dosificacionMaxMaquina;
        changeSupport.firePropertyChange("dosificacionMaxMaquina", oldDosificacionMaxMaquina, dosificacionMaxMaquina);
    }

    public Integer getIdMoldeMaquina() {
        return idMoldeMaquina;
    }

    public void setIdMoldeMaquina(Integer idMoldeMaquina) {
        Integer oldIdMoldeMaquina = this.idMoldeMaquina;
        this.idMoldeMaquina = idMoldeMaquina;
        changeSupport.firePropertyChange("idMoldeMaquina", oldIdMoldeMaquina, idMoldeMaquina);
    }

    public Integer getIdRobotMaquina() {
        return idRobotMaquina;
    }

    public void setIdRobotMaquina(Integer idRobotMaquina) {
        Integer oldIdRobotMaquina = this.idRobotMaquina;
        this.idRobotMaquina = idRobotMaquina;
        changeSupport.firePropertyChange("idRobotMaquina", oldIdRobotMaquina, idRobotMaquina);
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idMaquina != null ? idMaquina.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Maquina)) {
            return false;
        }
        Maquina other = (Maquina) object;
        if ((this.idMaquina == null && other.idMaquina != null) || (this.idMaquina != null && !this.idMaquina.equals(other.idMaquina))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Pck_mantenimiento.Maquina[ idMaquina=" + idMaquina + " ]";
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }
    
}
