package com.saka.qms.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.GenerationType;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "employees")
public class Employee implements Identifiable<Integer>, Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "employees-id")
    @SequenceGenerator(name = "employees-id", sequenceName = "employees_id_seq",
            schema = "qms_checklist", allocationSize = 1)
    private Integer id;
    @Column(name = "emp_id")
    private Integer employeeId;
    @Column(name = "emp_name")
    private String employeeName;
    @Column(name = "username")
    private String username;
    @Column(name = "password")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    @Column(name = "isadmin")
    private Boolean isAdmin;
    @Column(name = "is_department_head")
    private Boolean isDepartmentHead;
    @Column(name = "modified_by")
    private String modifiedBy;
    @Column(name = "modified_on")
    private LocalDateTime modifiedOn;
    @Column(name = "authorised_by")
    private String authorisedBy;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "employee_departments",
            joinColumns = @JoinColumn(name = "emp_id"),
            inverseJoinColumns = @JoinColumn(name = "dept_id"))
    private Set<Department> departments = new HashSet<>();

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getEmployeeId() { return employeeId; }
    public void setEmployeeId(Integer employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Boolean getIsAdmin() { return isAdmin; }
    public void setIsAdmin(Boolean isAdmin) { this.isAdmin = isAdmin; }
    public Boolean getIsDepartmentHead() { return isDepartmentHead; }
    public void setIsDepartmentHead(Boolean isDepartmentHead) { this.isDepartmentHead = isDepartmentHead; }
    public String getModifiedBy() { return modifiedBy; }
    public void setModifiedBy(String modifiedBy) { this.modifiedBy = modifiedBy; }
    public LocalDateTime getModifiedOn() { return modifiedOn; }
    public void setModifiedOn(LocalDateTime modifiedOn) { this.modifiedOn = modifiedOn; }
    public String getAuthorisedBy() { return authorisedBy; }
    public void setAuthorisedBy(String authorisedBy) { this.authorisedBy = authorisedBy; }
    public Set<Department> getDepartments() { return departments; }
    public void setDepartments(Set<Department> departments) { this.departments = departments; }
}
