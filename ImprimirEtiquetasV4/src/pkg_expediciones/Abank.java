/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg_expediciones;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

/**
 *
 * @author mmarco
 */
@Entity
@Table(name = "ABANK", catalog = "x3famesa", schema = "FAMESAOF")
@NamedQueries({
    @NamedQuery(name = "Abank.findAll", query = "SELECT a FROM Abank a"),
    @NamedQuery(name = "Abank.findByUpdtick0", query = "SELECT a FROM Abank a WHERE a.updtick0 = :updtick0"),
    @NamedQuery(name = "Abank.findByCry0", query = "SELECT a FROM Abank a WHERE a.cry0 = :cry0"),
    @NamedQuery(name = "Abank.findByBan0", query = "SELECT a FROM Abank a WHERE a.ban0 = :ban0"),
    @NamedQuery(name = "Abank.findByPab0", query = "SELECT a FROM Abank a WHERE a.pab0 = :pab0"),
    @NamedQuery(name = "Abank.findByBic0", query = "SELECT a FROM Abank a WHERE a.bic0 = :bic0"),
    @NamedQuery(name = "Abank.findByVlystr0", query = "SELECT a FROM Abank a WHERE a.vlystr0 = :vlystr0"),
    @NamedQuery(name = "Abank.findByVlyend0", query = "SELECT a FROM Abank a WHERE a.vlyend0 = :vlyend0"),
    @NamedQuery(name = "Abank.findByCredat0", query = "SELECT a FROM Abank a WHERE a.credat0 = :credat0"),
    @NamedQuery(name = "Abank.findByCreusr0", query = "SELECT a FROM Abank a WHERE a.creusr0 = :creusr0"),
    @NamedQuery(name = "Abank.findByUpddat0", query = "SELECT a FROM Abank a WHERE a.upddat0 = :upddat0"),
    @NamedQuery(name = "Abank.findByUpdusr0", query = "SELECT a FROM Abank a WHERE a.updusr0 = :updusr0"),
    @NamedQuery(name = "Abank.findByCredattim0", query = "SELECT a FROM Abank a WHERE a.credattim0 = :credattim0"),
    @NamedQuery(name = "Abank.findByUpddattim0", query = "SELECT a FROM Abank a WHERE a.upddattim0 = :upddattim0"),
    @NamedQuery(name = "Abank.findByRowid", query = "SELECT a FROM Abank a WHERE a.rowid = :rowid")})
public class Abank implements Serializable {
    @Transient
    private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
    private static final long serialVersionUID = 1L;
    @Basic(optional = false)
    @Column(name = "UPDTICK_0")
    private int updtick0;
    @Basic(optional = false)
    @Column(name = "CRY_0")
    private String cry0;
    @Basic(optional = false)
    @Column(name = "BAN_0")
    private String ban0;
    @Basic(optional = false)
    @Column(name = "PAB_0")
    private String pab0;
    @Basic(optional = false)
    @Column(name = "BIC_0")
    private String bic0;
    @Basic(optional = false)
    @Column(name = "VLYSTR_0")
    @Temporal(TemporalType.TIMESTAMP)
    private Date vlystr0;
    @Basic(optional = false)
    @Column(name = "VLYEND_0")
    @Temporal(TemporalType.TIMESTAMP)
    private Date vlyend0;
    @Basic(optional = false)
    @Column(name = "CREDAT_0")
    @Temporal(TemporalType.TIMESTAMP)
    private Date credat0;
    @Basic(optional = false)
    @Column(name = "CREUSR_0")
    private String creusr0;
    @Basic(optional = false)
    @Column(name = "UPDDAT_0")
    @Temporal(TemporalType.TIMESTAMP)
    private Date upddat0;
    @Basic(optional = false)
    @Column(name = "UPDUSR_0")
    private String updusr0;
    @Basic(optional = false)
    @Column(name = "CREDATTIM_0")
    @Temporal(TemporalType.TIMESTAMP)
    private Date credattim0;
    @Basic(optional = false)
    @Column(name = "UPDDATTIM_0")
    @Temporal(TemporalType.TIMESTAMP)
    private Date upddattim0;
    @Basic(optional = false)
    @Lob
    @Column(name = "AUUID_0")
    private byte[] auuid0;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Id
    @Basic(optional = false)
    @Column(name = "ROWID")
    private BigDecimal rowid;

    public Abank() {
    }

    public Abank(BigDecimal rowid) {
        this.rowid = rowid;
    }

    public Abank(BigDecimal rowid, int updtick0, String cry0, String ban0, String pab0, String bic0, Date vlystr0, Date vlyend0, Date credat0, String creusr0, Date upddat0, String updusr0, Date credattim0, Date upddattim0, byte[] auuid0) {
        this.rowid = rowid;
        this.updtick0 = updtick0;
        this.cry0 = cry0;
        this.ban0 = ban0;
        this.pab0 = pab0;
        this.bic0 = bic0;
        this.vlystr0 = vlystr0;
        this.vlyend0 = vlyend0;
        this.credat0 = credat0;
        this.creusr0 = creusr0;
        this.upddat0 = upddat0;
        this.updusr0 = updusr0;
        this.credattim0 = credattim0;
        this.upddattim0 = upddattim0;
        this.auuid0 = auuid0;
    }

    public int getUpdtick0() {
        return updtick0;
    }

    public void setUpdtick0(int updtick0) {
        int oldUpdtick0 = this.updtick0;
        this.updtick0 = updtick0;
        changeSupport.firePropertyChange("updtick0", oldUpdtick0, updtick0);
    }

    public String getCry0() {
        return cry0;
    }

    public void setCry0(String cry0) {
        String oldCry0 = this.cry0;
        this.cry0 = cry0;
        changeSupport.firePropertyChange("cry0", oldCry0, cry0);
    }

    public String getBan0() {
        return ban0;
    }

    public void setBan0(String ban0) {
        String oldBan0 = this.ban0;
        this.ban0 = ban0;
        changeSupport.firePropertyChange("ban0", oldBan0, ban0);
    }

    public String getPab0() {
        return pab0;
    }

    public void setPab0(String pab0) {
        String oldPab0 = this.pab0;
        this.pab0 = pab0;
        changeSupport.firePropertyChange("pab0", oldPab0, pab0);
    }

    public String getBic0() {
        return bic0;
    }

    public void setBic0(String bic0) {
        String oldBic0 = this.bic0;
        this.bic0 = bic0;
        changeSupport.firePropertyChange("bic0", oldBic0, bic0);
    }

    public Date getVlystr0() {
        return vlystr0;
    }

    public void setVlystr0(Date vlystr0) {
        Date oldVlystr0 = this.vlystr0;
        this.vlystr0 = vlystr0;
        changeSupport.firePropertyChange("vlystr0", oldVlystr0, vlystr0);
    }

    public Date getVlyend0() {
        return vlyend0;
    }

    public void setVlyend0(Date vlyend0) {
        Date oldVlyend0 = this.vlyend0;
        this.vlyend0 = vlyend0;
        changeSupport.firePropertyChange("vlyend0", oldVlyend0, vlyend0);
    }

    public Date getCredat0() {
        return credat0;
    }

    public void setCredat0(Date credat0) {
        Date oldCredat0 = this.credat0;
        this.credat0 = credat0;
        changeSupport.firePropertyChange("credat0", oldCredat0, credat0);
    }

    public String getCreusr0() {
        return creusr0;
    }

    public void setCreusr0(String creusr0) {
        String oldCreusr0 = this.creusr0;
        this.creusr0 = creusr0;
        changeSupport.firePropertyChange("creusr0", oldCreusr0, creusr0);
    }

    public Date getUpddat0() {
        return upddat0;
    }

    public void setUpddat0(Date upddat0) {
        Date oldUpddat0 = this.upddat0;
        this.upddat0 = upddat0;
        changeSupport.firePropertyChange("upddat0", oldUpddat0, upddat0);
    }

    public String getUpdusr0() {
        return updusr0;
    }

    public void setUpdusr0(String updusr0) {
        String oldUpdusr0 = this.updusr0;
        this.updusr0 = updusr0;
        changeSupport.firePropertyChange("updusr0", oldUpdusr0, updusr0);
    }

    public Date getCredattim0() {
        return credattim0;
    }

    public void setCredattim0(Date credattim0) {
        Date oldCredattim0 = this.credattim0;
        this.credattim0 = credattim0;
        changeSupport.firePropertyChange("credattim0", oldCredattim0, credattim0);
    }

    public Date getUpddattim0() {
        return upddattim0;
    }

    public void setUpddattim0(Date upddattim0) {
        Date oldUpddattim0 = this.upddattim0;
        this.upddattim0 = upddattim0;
        changeSupport.firePropertyChange("upddattim0", oldUpddattim0, upddattim0);
    }

    public byte[] getAuuid0() {
        return auuid0;
    }

    public void setAuuid0(byte[] auuid0) {
        byte[] oldAuuid0 = this.auuid0;
        this.auuid0 = auuid0;
        changeSupport.firePropertyChange("auuid0", oldAuuid0, auuid0);
    }

    public BigDecimal getRowid() {
        return rowid;
    }

    public void setRowid(BigDecimal rowid) {
        BigDecimal oldRowid = this.rowid;
        this.rowid = rowid;
        changeSupport.firePropertyChange("rowid", oldRowid, rowid);
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (rowid != null ? rowid.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Abank)) {
            return false;
        }
        Abank other = (Abank) object;
        if ((this.rowid == null && other.rowid != null) || (this.rowid != null && !this.rowid.equals(other.rowid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "pkg_expediciones.Abank[ rowid=" + rowid + " ]";
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }
    
}
