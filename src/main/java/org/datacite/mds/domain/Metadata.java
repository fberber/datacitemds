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
import javax.persistence.Query;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.TypedQuery;
import javax.persistence.Version;
import javax.validation.GroupSequence;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.apache.log4j.Logger;
import org.datacite.mds.service.SchemaService;
import org.datacite.mds.validation.constraints.MatchDoi;
import org.datacite.mds.validation.constraints.ValidXML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.transaction.annotation.Transactional;

@Configurable
@Entity
@MatchDoi(groups = Metadata.SecondLevelConstraint.class)
@GroupSequence({ Metadata.class, Metadata.SecondLevelConstraint.class })
public class Metadata {

    public static final int XML_MAX_SIZE = 10 * 1024 * 1024; // 10 MByte

    private static Logger log4j = Logger.getLogger(Metadata.class);
    
    @Autowired
    @Transient
    SchemaService schemaService;

    @ValidXML
    @Column(columnDefinition = "MEDIUMBLOB")
    @Size(max = XML_MAX_SIZE)
    private byte[] xml;
    
    private String namespace;

    @Min(0L)
    private Integer metadataVersion;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(iso = ISO.DATE_TIME)
    private Date created;

    @NotNull
    @ManyToOne(targetEntity = Dataset.class)
    @JoinColumn
    private Dataset dataset;
    
    private Boolean isConvertedByMds = false;
    
    @Transient
    private Query maxMetaVerQuery;

    public static Integer findMaxMetadataVersionByDataset(Dataset dataset) {
        if (dataset == null)
            throw new IllegalArgumentException("The dataset argument is required");
        
        EntityManager em = entityManager();
        Query q = em.createQuery("SELECT MAX(metadataVersion) FROM Metadata WHERE dataset = :dataset");
        q.setParameter("dataset", dataset);
        Integer max = (Integer) q.getSingleResult();
        return max == null ? -1 : max;
    }

    @Transactional
    public void persist() {
        if (this.entityManager == null)
            this.entityManager = entityManager();
        if (maxMetaVerQuery == null)
            maxMetaVerQuery = entityManager.
              createQuery("SELECT MAX(metadataVersion) FROM Metadata WHERE dataset = :dataset");
        
        maxMetaVerQuery.setParameter("dataset", getDataset());
        Integer max = (Integer) maxMetaVerQuery.getSingleResult();
        Integer maxVersion = max == null ? -1 : max;
        setMetadataVersion(maxVersion + 1);
        setCreated(new Date());
        entityManager.persist(this);
        
        log4j.info(getDataset().getDatacentre().getSymbol() + " successfuly stored metadata for " + getDataset().getDoi());
    }

    public static TypedQuery<Metadata> findMetadatasByDataset(Dataset dataset) {
        if (dataset == null)
            throw new IllegalArgumentException("The dataset argument is required");
        EntityManager em = Metadata.entityManager();
        TypedQuery<Metadata> q = em.createQuery(
                "SELECT Metadata FROM Metadata AS metadata WHERE metadata.dataset = :dataset ORDER BY metadataVersion DESC", Metadata.class);
        q.setParameter("dataset", dataset);
        return q;
    }

    public static Metadata findLatestMetadatasByDataset(Dataset dataset) {
        if (dataset == null)
            throw new IllegalArgumentException("The dataset argument is required");

        Integer maxVersion = findMaxMetadataVersionByDataset(dataset);
        
        EntityManager em = Metadata.entityManager();
        TypedQuery<Metadata> q = em.createQuery(
                "SELECT Metadata FROM Metadata AS metadata WHERE metadata.dataset = :dataset "
                + "AND metadata.metadataVersion = :metadataVersion", 
                Metadata.class);
        q.setParameter("dataset", dataset);
        q.setParameter("metadataVersion", maxVersion);
        
        Metadata result;
        try {
            result = q.getSingleResult();
        } catch (Exception e) {
            result = null;
        }

        return result;
    }
    
    private static String hqlFindLatestMetadatas = "select m from Metadata m WHERE m.metadataVersion = (select max(metadataVersion) from Metadata AS mm WHERE mm.dataset = m.dataset)"; 
    
    public static List<Metadata> findLatestMetadatas() {
        return entityManager().createQuery(hqlFindLatestMetadatas, Metadata.class).getResultList();
    }
    
    public static List<Metadata> findLatestMetadatasByNamespace(String namespace) {
        String hql;
        if (namespace == null) 
            hql = hqlFindLatestMetadatas + " AND m.namespace is null";
        else
            hql = hqlFindLatestMetadatas + " AND m.namespace = :namespace";
        
        TypedQuery<Metadata> q = entityManager().createQuery(hql, Metadata.class);
        
        if (namespace != null)
            q.setParameter("namespace", namespace);
        return q.getResultList();
    }
    
    public void setXml(byte[] xml) {
        if (schemaService == null) 
            throw new IllegalStateException("schemaService has not been injected");
        String namespace = schemaService.getNamespace(xml);
        setNamespace(namespace);
        this.xml = xml;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public Boolean getIsConvertedByMds() {
        return isConvertedByMds;
    }

    public void setIsConvertedByMds(Boolean isConvertedByMds) {
        this.isConvertedByMds = isConvertedByMds;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getDataset().getDatacentre().getSymbol()).append(":");
        sb.append(getDataset().getDoi());
        sb.append(" #").append(getMetadataVersion());
        sb.append(" (id=" + getId() + ")");
        return sb.toString();
    }

    public String debugString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Id: ").append(getId()).append(", ");
        sb.append("Version: ").append(getVersion()).append(", ");
        sb.append("Xml: ").append(java.util.Arrays.toString(getXml())).append(", ");
        sb.append("MetadataVersion: ").append(getMetadataVersion()).append(", ");
        sb.append("Created: ").append(getCreated()).append(", ");
        sb.append("Dataset: ").append(getDataset());
        return sb.toString();
    }

    public interface SecondLevelConstraint {};
    

	public SchemaService getSchemaService() {
        return this.schemaService;
    }

	public void setSchemaService(SchemaService schemaService) {
        this.schemaService = schemaService;
    }

	public byte[] getXml() {
        return this.xml;
    }

	public Integer getMetadataVersion() {
        return this.metadataVersion;
    }

	public void setMetadataVersion(Integer metadataVersion) {
        this.metadataVersion = metadataVersion;
    }

	public Date getCreated() {
        return this.created;
    }

	public void setCreated(Date created) {
        this.created = created;
    }

	public Dataset getDataset() {
        return this.dataset;
    }

	public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

	public Query getMaxMetaVerQuery() {
        return this.maxMetaVerQuery;
    }

	public void setMaxMetaVerQuery(Query maxMetaVerQuery) {
        this.maxMetaVerQuery = maxMetaVerQuery;
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
            Metadata attached = Metadata.findMetadata(this.id);
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
    public Metadata merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        Metadata merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }

	public static final EntityManager entityManager() {
        EntityManager em = new Metadata().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countMetadatas() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Metadata o", Long.class).getSingleResult();
    }

	public static List<Metadata> findAllMetadatas() {
        return entityManager().createQuery("SELECT o FROM Metadata o", Metadata.class).getResultList();
    }

	public static Metadata findMetadata(Long id) {
        if (id == null) return null;
        return entityManager().find(Metadata.class, id);
    }

	public static List<Metadata> findMetadataEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM Metadata o", Metadata.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
}
