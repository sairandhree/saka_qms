package com.saka.qms.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "departments")
public class Department {
    @Id
    private Integer id;

    @Column(name = "dept_name")
    private String deptName;
    private String manager;
    @Column(name = "modified_by")
    private String modifiedBy;
    @Column(name = "modified_on")
    private LocalDateTime modifiedOn;
    @Column(name = "authorised_by")
    private String authorisedBy;

    @JsonIgnore
    @OneToMany(mappedBy = "department")
    private List<NameOfItem> items = new ArrayList<>();

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    public String getManager() { return manager; }
    public void setManager(String manager) { this.manager = manager; }
    public String getModifiedBy() { return modifiedBy; }
    public void setModifiedBy(String modifiedBy) { this.modifiedBy = modifiedBy; }
    public LocalDateTime getModifiedOn() { return modifiedOn; }
    public void setModifiedOn(LocalDateTime modifiedOn) { this.modifiedOn = modifiedOn; }
    public String getAuthorisedBy() { return authorisedBy; }
    public void setAuthorisedBy(String authorisedBy) { this.authorisedBy = authorisedBy; }
    public List<NameOfItem> getItems() { return items; }
    public void setItems(List<NameOfItem> items) { this.items = items; }
}
