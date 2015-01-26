package org.datacite.mds.domain;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OrderBy;
import javax.persistence.PersistenceContext;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import javax.persistence.Version;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.datacite.mds.util.FilterPredicates;
import org.datacite.mds.util.Utils;
import org.datacite.mds.validation.constraints.Email;
import org.datacite.mds.validation.constraints.Symbol;
import org.datacite.mds.validation.constraints.Unique;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.transaction.annotation.Transactional;

@Configurable
@Entity
@Unique(field = "symbol")
public class Allocator implements AllocatorOrDatacentre {

    private static Logger log4j = Logger.getLogger(Allocator.class);

    @NotNull
    @Symbol(Symbol.Type.ALLOCATOR)
    @Column(unique = true)
    private String symbol;

    private String password;

    @NotNull
    @Size(min = 3, max = 255)
    private String name;

    @NotNull
    @Size(min = 2, max = 80)
    private String contactName;

    @NotNull
    @Email
    private String contactEmail;

    @NotNull
    private Integer doiQuotaAllowed = -1;

    @NotNull
    @Min(0L)
    @Max(999999999L)
    private Integer doiQuotaUsed = 0;

    @ManyToMany(cascade = CascadeType.ALL)
    @OrderBy("prefix")
    private Set<org.datacite.mds.domain.Prefix> prefixes = new java.util.HashSet<org.datacite.mds.domain.Prefix>();

    private Boolean isActive = true;

    private String roleName = "ROLE_ALLOCATOR";
    
    @Size(max = 4000)
    private String comments;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(iso = ISO.DATE_TIME)
    private Date created;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(iso = ISO.DATE_TIME)
    private Date updated;

    private String experiments;

    @SuppressWarnings("unchecked")
    public static List<Allocator> findAllAllocators() {
        return entityManager().createQuery("select o from Allocator o order by symbol").getResultList();
    }

    @SuppressWarnings("unchecked")
    public static List<Allocator> findAllocatorEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("select o from Allocator o order by symbol").setFirstResult(firstResult)
                .setMaxResults(maxResults).getResultList();
    }

    @Transactional
    public void persist() {
        Date date = new Date();
        setCreated(date);
        setUpdated(date);
        if (this.entityManager == null)
            this.entityManager = entityManager();
        this.entityManager.persist(this);
    }

    @Transactional
    public Allocator merge() {
        setUpdated(new Date());
        if (this.entityManager == null)
            this.entityManager = entityManager();
        Allocator merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }

    /**
     * retrieve a allocator by symbol
     * 
     * @param symbol
     *            of an allocator
     * @return allocator with the given symbol or null if no such allocator
     *         exists
     */
    public static Allocator findAllocatorBySymbol(String symbol) {
        if (symbol == null) {
            return null;
        }
        try {
            log4j.trace("search for '" + symbol + "'");
            Allocator al = findAllocatorsBySymbolEquals(symbol).getSingleResult();
            log4j.trace("found '" + symbol + "'");
            return al;
        } catch (Exception e) {
            log4j.trace("no allocator found");
            return null;
        }
    }
    
    public static List<Allocator> findAllocatorsByPrefix (Prefix prefix) {
        List<Allocator> list = findAllAllocators();
        Predicate containsPrefix = FilterPredicates.getAllocatorOrDatacentreContainsPrefixPredicate(prefix);
        CollectionUtils.filter(list, containsPrefix);
        return list;
    }
    
    private transient long countDatasets; 
    
    public long getCountDatasets() {
        return Dataset.countDatasetsByAllocatorOrDatacentre(this)
                - Dataset.countTestDatasetsByAllocatorOrDatacentre(this);
    }
    
    public Collection<String> getExperiments() {
        return Utils.csvToList(this.experiments);
    }
    
    public void setExperiments(Collection<String> experiments) {
        this.experiments = Utils.collectionToCsv(experiments);
    }
    
    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail.trim();
    }
    
    public void setName(String name) {
        this.name = name.replaceAll("\r?\n", " ").trim();
    }

    /**
     * calculate String to be used for magic auth key
     * 
     * @return (unhashed) base part of the magic auth string
     */
    public String getBaseAuthString() {
        StringBuilder str = new StringBuilder();
        str.append(getId());
        str.append(getSymbol());
        str.append(StringUtils.defaultString(getPassword()));
        return str.toString();
    }
    
    @Override
    public String toString() {
        return getSymbol() + " (id=" + getId() + ")";
    }

	public String getSymbol() {
        return this.symbol;
    }

	public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

	public String getPassword() {
        return this.password;
    }

	public void setPassword(String password) {
        this.password = password;
    }

	public String getName() {
        return this.name;
    }

	public String getContactName() {
        return this.contactName;
    }

	public void setContactName(String contactName) {
        this.contactName = contactName;
    }

	public String getContactEmail() {
        return this.contactEmail;
    }

	public Integer getDoiQuotaAllowed() {
        return this.doiQuotaAllowed;
    }

	public void setDoiQuotaAllowed(Integer doiQuotaAllowed) {
        this.doiQuotaAllowed = doiQuotaAllowed;
    }

	public Integer getDoiQuotaUsed() {
        return this.doiQuotaUsed;
    }

	public void setDoiQuotaUsed(Integer doiQuotaUsed) {
        this.doiQuotaUsed = doiQuotaUsed;
    }

	public Set<Prefix> getPrefixes() {
        return this.prefixes;
    }

	public void setPrefixes(Set<Prefix> prefixes) {
        this.prefixes = prefixes;
    }

	public Boolean getIsActive() {
        return this.isActive;
    }

	public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

	public String getRoleName() {
        return this.roleName;
    }

	public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

	public String getComments() {
        return this.comments;
    }

	public void setComments(String comments) {
        this.comments = comments;
    }

	public Date getCreated() {
        return this.created;
    }

	public void setCreated(Date created) {
        this.created = created;
    }

	public Date getUpdated() {
        return this.updated;
    }

	public void setUpdated(Date updated) {
        this.updated = updated;
    }

	public void setExperiments(String experiments) {
        this.experiments = experiments;
    }

	@PersistenceContext
    transient EntityManager entityManager;

	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

	@Version
    @Column(name = "version")
    private Integer version;

	public Long getId() {
        return this.id;
    }

	public void setId(Long id) {
        this.id = id;
    }

	public Integer getVersion() {
        return this.version;
    }

	public void setVersion(Integer version) {
        this.version = version;
    }

	@Transactional
    public void remove() {
        if (this.entityManager == null) this.entityManager = entityManager();
        if (this.entityManager.contains(this)) {
            this.entityManager.remove(this);
        } else {
            Allocator attached = Allocator.findAllocator(this.id);
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

	public static final EntityManager entityManager() {
        EntityManager em = new Allocator().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countAllocators() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Allocator o", Long.class).getSingleResult();
    }

	public static Allocator findAllocator(Long id) {
        if (id == null) return null;
        return entityManager().find(Allocator.class, id);
    }

	public static TypedQuery<Allocator> findAllocatorsByNameLike(String name) {
        if (name == null || name.length() == 0) throw new IllegalArgumentException("The name argument is required");
        name = name.replace('*', '%');
        if (name.charAt(0) != '%') {
            name = "%" + name;
        }
        if (name.charAt(name.length() - 1) != '%') {
            name = name + "%";
        }
        EntityManager em = Allocator.entityManager();
        TypedQuery<Allocator> q = em.createQuery("SELECT o FROM Allocator AS o WHERE LOWER(o.name) LIKE LOWER(:name)", Allocator.class);
        q.setParameter("name", name);
        return q;
    }

	public static TypedQuery<Allocator> findAllocatorsBySymbolEquals(String symbol) {
        if (symbol == null || symbol.length() == 0) throw new IllegalArgumentException("The symbol argument is required");
        EntityManager em = Allocator.entityManager();
        TypedQuery<Allocator> q = em.createQuery("SELECT o FROM Allocator AS o WHERE o.symbol = :symbol", Allocator.class);
        q.setParameter("symbol", symbol);
        return q;
    }
}
