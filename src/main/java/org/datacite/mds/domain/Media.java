package org.datacite.mds.domain;

import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.PersistenceContext;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import javax.persistence.Version;
import javax.validation.GroupSequence;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.datacite.mds.validation.constraints.MatchDomain;
import org.datacite.mds.validation.constraints.MediaType;
import org.datacite.mds.validation.constraints.URL;
import org.datacite.mds.validation.constraints.Unique;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.transaction.annotation.Transactional;

@Entity
@Configurable
@Unique(field = { "dataset", "mediaType" })
@MatchDomain(groups = Media.SecondLevelConstraint.class)
@GroupSequence( { Media.class, Media.SecondLevelConstraint.class })
public class Media implements Comparable {

    @ManyToOne
    @NotNull
    private Dataset dataset;

    @MediaType
    private String mediaType;

    @URL
    @NotEmpty
    private String url;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(iso = ISO.DATE_TIME)
    private Date created;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(iso = ISO.DATE_TIME)
    private Date updated;

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getDataset().getDatacentre().getSymbol()).append(":");
        sb.append(getDataset().getDoi()).append(" ");
        sb.append(getMediaType());
        sb.append(" (id=" + getId() + ")");
        return sb.toString();
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
    public Media merge() {
        setUpdated(new Date());
        if (this.entityManager == null)
            this.entityManager = entityManager();
        Media merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }
    
    public static TypedQuery<Media> findMediasByDataset(Dataset dataset) {
        if (dataset == null) throw new IllegalArgumentException("The dataset argument is required");
        EntityManager em = Media.entityManager();
        TypedQuery<Media> q = em.createQuery("SELECT o FROM Media AS o WHERE o.dataset = :dataset ORDER BY o.mediaType ASC", Media.class);
        q.setParameter("dataset", dataset);
        return q;
    }

    public static Media findMediaByDatasetAndMediaType(Dataset dataset, String mediaType) {
        if (dataset == null) throw new IllegalArgumentException("The dataset argument is required");
        EntityManager em = Media.entityManager();
        TypedQuery<Media> q = em.createQuery("SELECT o FROM Media AS o WHERE o.dataset = :dataset AND o.mediaType = :mediaType", Media.class);
        q.setParameter("dataset", dataset);
        q.setParameter("mediaType", mediaType);
        return q.getSingleResult();
    }

    @Override
    public int compareTo(Object o) {
        Media media = (Media) o;
        return  this.mediaType.compareTo(media.mediaType);
    };

    public interface SecondLevelConstraint {
    }


	public Dataset getDataset() {
        return this.dataset;
    }

	public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

	public String getMediaType() {
        return this.mediaType;
    }

	public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
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
            Media attached = Media.findMedia(this.id);
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
        EntityManager em = new Media().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countMedias() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Media o", Long.class).getSingleResult();
    }

	public static List<Media> findAllMedias() {
        return entityManager().createQuery("SELECT o FROM Media o", Media.class).getResultList();
    }

	public static Media findMedia(Long id) {
        if (id == null) return null;
        return entityManager().find(Media.class, id);
    }

	public static List<Media> findMediaEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM Media o", Media.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
}
