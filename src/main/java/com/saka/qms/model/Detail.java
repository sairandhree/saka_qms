package com.saka.qms.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.GenerationType;
import jakarta.persistence.SequenceGenerator;
import java.time.LocalDateTime;

@Entity
@Table(name = "details")
public class Detail implements Identifiable<Integer>, Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "details-id")
    @SequenceGenerator(name = "details-id", sequenceName = "details_id_seq",
            schema = "qms_checklist", allocationSize = 1)
    private Integer id;
    private String details;
    private String note;
    private String parameters;
    @Column(name = "rel_id")
    private Integer relatedItemId;
    private String link;
    @Column(name = "sequence")
    private Integer sequence;
    private String passward;
    @Column(name = "modified_by")
    private String modifiedBy;
    @Column(name = "authorised_by")
    private String authorisedBy;
    @Column(name = "modified_on")
    private LocalDateTime modifiedOn;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rel_id", insertable = false, updatable = false)
    private NameOfItem item;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getParameters() { return parameters; }
    public void setParameters(String parameters) { this.parameters = parameters; }
    public Integer getRelatedItemId() { return relatedItemId; }
    public void setRelatedItemId(Integer relatedItemId) { this.relatedItemId = relatedItemId; }
    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }
    public Integer getSequence() { return sequence; }
    public void setSequence(Integer sequence) { this.sequence = sequence; }
    public String getPassward() { return passward; }
    public void setPassward(String passward) { this.passward = passward; }
    public String getModifiedBy() { return modifiedBy; }
    public void setModifiedBy(String modifiedBy) { this.modifiedBy = modifiedBy; }
    public String getAuthorisedBy() { return authorisedBy; }
    public void setAuthorisedBy(String authorisedBy) { this.authorisedBy = authorisedBy; }
    public LocalDateTime getModifiedOn() { return modifiedOn; }
    public void setModifiedOn(LocalDateTime modifiedOn) { this.modifiedOn = modifiedOn; }
    public NameOfItem getItem() { return item; }
    public void setItem(NameOfItem item) { this.item = item; }
}
