package org.datacite.mds.domain;

import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PersistenceContext;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.TypedQuery;
import javax.persistence.Version;
import javax.validation.GroupSequence;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.apache.log4j.Logger;
import org.datacite.mds.util.Constants;
import org.datacite.mds.util.Utils;
import org.datacite.mds.validation.constraints.Doi;
import org.datacite.mds.validation.constraints.MatchDoiPrefix;
import org.datacite.mds.validation.constraints.MatchDomain;
import org.datacite.mds.validation.constraints.URL;
import org.datacite.mds.validation.constraints.Unique;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.transaction.annotation.Transactional;

@Entity
@Configurable
@Doi
@MatchDoiPrefix(groups = Dataset.SecondLevelConstraint.class)
@MatchDomain(groups = Dataset.SecondLevelConstraint.class)
@Unique(field = "doi")
@GroupSequence({ Dataset.class, Dataset.SecondLevelConstraint.class })
public class Dataset {
    
    private static Logger log4j = Logger.getLogger(Dataset.class);

    @NotNull
    @Column(unique = true)
    @Size(max = 255)
    private String doi;

    @NotNull
    private Boolean isActive = true;

    private Boolean isRefQuality = false;

    @Min(100L)
    @Max(510L)
    private Integer lastLandingPageStatus;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(iso = ISO.DATE_TIME)
    private Date lastLandingPageStatusCheck;

    private String lastMetadataStatus;

    @NotNull
    @ManyToOne(targetEntity = Datacentre.class)
    @JoinColumn
    private Datacentre datacentre;

    @Transient
    @URL
    private String url;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(iso = ISO.DATE_TIME)
    private Date created;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(iso = ISO.DATE_TIME)
    private Date updated;
    
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(iso = ISO.DATE_TIME)
    private Date minted;

    @Transient
    public Metadata getLatestMetadata() {
        return Metadata.findLatestMetadatasByDataset(this);
    }

    private static TypedQuery<Dataset> queryDatasetsByAllocatorOrDatacentre(AllocatorOrDatacentre user) {
        EntityManager em = entityManager();
        String hql;
        if (user instanceof Datacentre) {
            hql = "SELECT Dataset FROM Dataset AS dataset WHERE dataset.datacentre = :user ORDER BY dataset.updated DESC";
        } else {
            hql = "SELECT Dataset FROM Dataset AS dataset WHERE dataset.datacentre.allocator = :user ORDER BY dataset.updated DESC";
        }
        TypedQuery<Dataset> q = em.createQuery(hql, Dataset.class);
        q.setParameter("user", user);
        return q;
    }

    public static List<Dataset> findDatasetEntriesByAllocatorOrDatacentre(AllocatorOrDatacentre user, int firstResult, int maxResults) {
        TypedQuery<Dataset> q = queryDatasetsByAllocatorOrDatacentre(user);
        return q.setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

    public static List<Dataset> findDatasetsByAllocatorOrDatacentre(AllocatorOrDatacentre user) {
        TypedQuery<Dataset> q = queryDatasetsByAllocatorOrDatacentre(user);
        return q.getResultList();
    }

    public static long countDatasetsByAllocatorOrDatacentre(AllocatorOrDatacentre user, String prefix) {
        EntityManager em = entityManager();
        String hql;
        if (user instanceof Allocator)
            hql = "SELECT COUNT(*) FROM Dataset AS dataset WHERE dataset.datacentre.allocator = :user";
        else
            hql = "SELECT COUNT(*) FROM Dataset AS dataset WHERE dataset.datacentre = :user";
        
        if (prefix != null)
            hql += " AND dataset.doi like :prefix";
        
        TypedQuery<Long> q = em.createQuery(hql, Long.class);
        q.setParameter("user", user);
        
        if (prefix != null)
            q.setParameter("prefix", prefix + "/%");
        
        return q.getSingleResult();
    }

    public static long countDatasetsByAllocatorOrDatacentre(AllocatorOrDatacentre user) {
        return countDatasetsByAllocatorOrDatacentre(user, null);
    }

    public static long countTestDatasetsByAllocatorOrDatacentre(AllocatorOrDatacentre user) {
        return countDatasetsByAllocatorOrDatacentre(user, Constants.TEST_PREFIX);
    }

    
    @Transactional
    public void persist() {
        Date date = new Date();
        setCreated(date);
        setUpdated(date);
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.persist(this);
    }

    @Transactional
    public Dataset merge() {
        setUpdated(new Date());
        if (this.entityManager == null) this.entityManager = entityManager();
        Dataset merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }
    
    /**
     * retrieve a dataset by 
     * 
     * @param doi
     *            of an dataset
     * @return dataset with the given doi or null if no such dataset
     *         exists
     */
    public static Dataset findDatasetByDoi(String doi) {
        doi = Utils.normalizeDoi(doi);
        if (doi == null) {
            return null;
        }
        try {
            log4j.trace("search for '" + doi + "'");
            Dataset dataset = findDatasetsByDoiEquals(doi).getSingleResult();
            log4j.trace("found '" + doi + "'");
            return dataset;
        } catch (Exception e) {
            log4j.trace("no dataset found");
            return null;
        }
    }
    
    public static List<Dataset> findDatasetsByPrefix(String prefix) {
        EntityManager em = entityManager();
        String hql = "select o from Dataset o where o.doi like :prefix";
        TypedQuery<Dataset> query = em.createQuery(hql, Dataset.class);
        query.setParameter("prefix", prefix + "/%");
        return query.getResultList();
    }

    public void setDoi(String doi) {
        this.doi = Utils.normalizeDoi(doi);
    }
    
    @Override
    public String toString() {
        return getDatacentre().getSymbol() + ":" + getDoi() + " (id=" + getId() + ")";
    }
    
    public interface SecondLevelConstraint {};

	public String getDoi() {
        return this.doi;
    }

	public Boolean getIsActive() {
        return this.isActive;
    }

	public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

	public Boolean getIsRefQuality() {
        return this.isRefQuality;
    }

	public void setIsRefQuality(Boolean isRefQuality) {
        this.isRefQuality = isRefQuality;
    }

	public Integer getLastLandingPageStatus() {
        return this.lastLandingPageStatus;
    }

	public void setLastLandingPageStatus(Integer lastLandingPageStatus) {
        this.lastLandingPageStatus = lastLandingPageStatus;
    }

	public Date getLastLandingPageStatusCheck() {
        return this.lastLandingPageStatusCheck;
    }

	public void setLastLandingPageStatusCheck(Date lastLandingPageStatusCheck) {
        this.lastLandingPageStatusCheck = lastLandingPageStatusCheck;
    }

	public String getLastMetadataStatus() {
        return this.lastMetadataStatus;
    }

	public void setLastMetadataStatus(String lastMetadataStatus) {
        this.lastMetadataStatus = lastMetadataStatus;
    }

	public Datacentre getDatacentre() {
        return this.datacentre;
    }

	public void setDatacentre(Datacentre datacentre) {
        this.datacentre = datacentre;
    }

	public String getUrl() {
        return this.url;
    }

	public void setUrl(String url) {
        this.url = url;
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

	public Date getMinted() {
        return this.minted;
    }

	public void setMinted(Date minted) {
        this.minted = minted;
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
            Dataset attached = Dataset.findDataset(this.id);
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
        EntityManager em = new Dataset().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countDatasets() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Dataset o", Long.class).getSingleResult();
    }

	public static List<Dataset> findAllDatasets() {
        return entityManager().createQuery("SELECT o FROM Dataset o", Dataset.class).getResultList();
    }

	public static Dataset findDataset(Long id) {
        if (id == null) return null;
        return entityManager().find(Dataset.class, id);
    }

	public static List<Dataset> findDatasetEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM Dataset o", Dataset.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	public static TypedQuery<Dataset> findDatasetsByDoiEquals(String doi) {
        if (doi == null || doi.length() == 0) throw new IllegalArgumentException("The doi argument is required");
        EntityManager em = Dataset.entityManager();
        TypedQuery<Dataset> q = em.createQuery("SELECT o FROM Dataset AS o WHERE o.doi = :doi", Dataset.class);
        q.setParameter("doi", doi);
        return q;
    }
}
