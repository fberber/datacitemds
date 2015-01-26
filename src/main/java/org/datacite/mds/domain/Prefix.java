package org.datacite.mds.domain;

import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.TypedQuery;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.datacite.mds.util.Utils;
import org.datacite.mds.validation.constraints.DoiPrefix;
import org.datacite.mds.validation.constraints.Unique;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.transaction.annotation.Transactional;

@Configurable
@Unique(field = "prefix")
@Entity
@XmlRootElement
public class Prefix implements Comparable<Prefix>{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    
    @Version
    @Column(name = "version")
    private Integer version;
    
    @XmlTransient
    public Long getId() {
        return this.id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    @XmlTransient
    public Integer getVersion() {
        return this.version;
    }
    
    public void setVersion(Integer version) {
        this.version = version;
    }    

    @NotNull
    @DoiPrefix
    @Column(unique = true)
    private String prefix;

    @XmlTransient
    public Date getCreated() {
        return this.created;
    }
    
    public void setCreated(Date created) {
        this.created = created;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(iso = ISO.DATE_TIME)
    private Date created;

    @SuppressWarnings("unchecked")
    public static List<Prefix> findAllPrefixes() {
        return entityManager().createQuery("select o from Prefix o order by length(prefix),prefix").getResultList();
    }

    @SuppressWarnings("unchecked")
    public static List<Prefix> findPrefixEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("select o from Prefix o order by length(prefix),prefix").setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

    @Transactional
    public void persist() {
        setCreated(new Date());
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.persist(this);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((prefix == null) ? 0 : prefix.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Prefix other = (Prefix) obj;
        if (prefix == null) {
            if (other.prefix != null)
                return false;
        } else if (!prefix.equals(other.prefix))
            return false;
        return true;
    }

    @Transient
    public List<Allocator> getAllocators() {
        return Allocator.findAllocatorsByPrefix(this);
    }
    
    @Transient
    public String getLabelWithAllocators() {
        List<Allocator> allocators = getAllocators();
        return getLabel(allocators);
    }

    @Transient
    public String getLabelWithDatacentres() {
        List<Datacentre> datacentres = Datacentre.findDatacentresByPrefix(this);
        return getLabel(datacentres);
    }

    private String getLabel(List<? extends AllocatorOrDatacentre> users) {
        List<String> symbols = Utils.toSymbols(users);
        if (symbols.isEmpty())
            return prefix;
        else
            return prefix + " " + symbols.toString();
    }
    
    @Override
    public String toString() {
        return getPrefix() + " (id=" + getId() + ")";
    }

    @Override
    public int compareTo(Prefix o) {
        String prefix1 = getPrefix();
        String prefix2 = o.getPrefix();
        int cmp = prefix1.length() - prefix2.length();
        if (cmp == 0)
            cmp = prefix1.compareTo(prefix2);
        return cmp;
    }

	public static TypedQuery<Prefix> findPrefixesByPrefixLike(String prefix) {
        if (prefix == null || prefix.length() == 0) throw new IllegalArgumentException("The prefix argument is required");
        EntityManager em = Prefix.entityManager();
        TypedQuery<Prefix> q = em.createQuery("SELECT o FROM Prefix AS o WHERE LOWER(o.prefix) LIKE LOWER(:prefix)", Prefix.class);
        q.setParameter("prefix", prefix);
        return q;
    }

	@PersistenceContext
    transient EntityManager entityManager;

	@Transactional
    public void remove() {
        if (this.entityManager == null) this.entityManager = entityManager();
        if (this.entityManager.contains(this)) {
            this.entityManager.remove(this);
        } else {
            Prefix attached = Prefix.findPrefix(this.id);
            this.entityManager.remove(attached);
        }
    }

	@Transactional
    public void flush() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.flush();
    }

	@Transactional
    public void clear() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.clear();
    }

	@Transactional
    public Prefix merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        Prefix merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }

	public static final EntityManager entityManager() {
        EntityManager em = new Prefix().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countPrefixes() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Prefix o", Long.class).getSingleResult();
    }

	public static Prefix findPrefix(Long id) {
        if (id == null) return null;
        return entityManager().find(Prefix.class, id);
    }

	public String getPrefix() {
        return this.prefix;
    }

	public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
