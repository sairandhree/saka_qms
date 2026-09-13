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
public class Detail implements Identifiable<Integer> {
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
    @Column(name = "detailes_modified_by")
    private String detailsModifiedBy;
    @Column(name = "details_auth_by")
    private String detailsAuthorisedBy;
    @Column(name = "details_modified_on")
    private LocalDateTime detailsModifiedOn;

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
    public String getDetailsModifiedBy() { return detailsModifiedBy; }
    public void setDetailsModifiedBy(String detailsModifiedBy) { this.detailsModifiedBy = detailsModifiedBy; }
    public String getDetailsAuthorisedBy() { return detailsAuthorisedBy; }
    public void setDetailsAuthorisedBy(String detailsAuthorisedBy) { this.detailsAuthorisedBy = detailsAuthorisedBy; }
    public LocalDateTime getDetailsModifiedOn() { return detailsModifiedOn; }
    public void setDetailsModifiedOn(LocalDateTime detailsModifiedOn) { this.detailsModifiedOn = detailsModifiedOn; }
    public NameOfItem getItem() { return item; }
    public void setItem(NameOfItem item) { this.item = item; }
}
