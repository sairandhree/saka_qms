package com.saka.qms.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.GenerationType;
import jakarta.persistence.SequenceGenerator;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "name_of_item")
public class NameOfItem implements Identifiable<Integer> {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "items-id")
    @SequenceGenerator(name = "items-id", sequenceName = "items_id_seq", allocationSize = 1)
    private Integer id;
    @Column(name = "name_of_item")
    private String nameOfItem;
    private String note;
    @Column(name = "sequence")
    private String sequence;
    private String code1;
    private String code2;
    private String code3;
    private String code4;
    private String code5;
    private String cdinf1;
    private String cdinf2;
    private String cdinf3;
    private String cdinf4;
    private String cdinf5;
    @Column(name = "modified_by")
    private String modifiedBy;
    @Column(name = "modified_on")
    private LocalDateTime modifiedOn;
    @Column(name = "authorised_by")
    private String authorisedBy;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "dept_id")
    private Department department;

    @JsonIgnore
    @OneToMany(mappedBy = "item")
    private List<Detail> details = new ArrayList<>();

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNameOfItem() { return nameOfItem; }
    public void setNameOfItem(String nameOfItem) { this.nameOfItem = nameOfItem; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getSequence() { return sequence; }
    public void setSequence(String sequence) { this.sequence = sequence; }
    public String getCode1() { return code1; }
    public void setCode1(String code1) { this.code1 = code1; }
    public String getCode2() { return code2; }
    public void setCode2(String code2) { this.code2 = code2; }
    public String getCode3() { return code3; }
    public void setCode3(String code3) { this.code3 = code3; }
    public String getCode4() { return code4; }
    public void setCode4(String code4) { this.code4 = code4; }
    public String getCode5() { return code5; }
    public void setCode5(String code5) { this.code5 = code5; }
    public String getCdinf1() { return cdinf1; }
    public void setCdinf1(String cdinf1) { this.cdinf1 = cdinf1; }
    public String getCdinf2() { return cdinf2; }
    public void setCdinf2(String cdinf2) { this.cdinf2 = cdinf2; }
    public String getCdinf3() { return cdinf3; }
    public void setCdinf3(String cdinf3) { this.cdinf3 = cdinf3; }
    public String getCdinf4() { return cdinf4; }
    public void setCdinf4(String cdinf4) { this.cdinf4 = cdinf4; }
    public String getCdinf5() { return cdinf5; }
    public void setCdinf5(String cdinf5) { this.cdinf5 = cdinf5; }
    public String getModifiedBy() { return modifiedBy; }
    public void setModifiedBy(String modifiedBy) { this.modifiedBy = modifiedBy; }
    public LocalDateTime getModifiedOn() { return modifiedOn; }
    public void setModifiedOn(LocalDateTime modifiedOn) { this.modifiedOn = modifiedOn; }
    public String getAuthorisedBy() { return authorisedBy; }
    public void setAuthorisedBy(String authorisedBy) { this.authorisedBy = authorisedBy; }
    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
    public List<Detail> getDetails() { return details; }
    public void setDetails(List<Detail> details) { this.details = details; }
}
