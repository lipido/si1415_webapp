package webapp;

import java.util.Collection;

import javax.persistence.EntityManager;

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zkplus.jpa.JpaUtil;

import persistence.Department;

public class DepartmentsVM {

	private Department currentDepartment = null;
	
	public Department getCurrentDepartment() {
		return currentDepartment;
	}
	public Collection<Department>
		getDepartments() {
		EntityManager em = JpaUtil.getEntityManager();
		
		return em.createQuery("SELECT d FROM Department d",
				Department.class).getResultList();
	}
	
	@NotifyChange("currentDepartment")
	@Command
	public void edit(@BindingParam("department") Department department) {
		this.currentDepartment = department;
	}
	
	@NotifyChange("departments")
	@Command
	public void delete(@BindingParam("department") Department department) {
		EntityManager em = JpaUtil.getEntityManager();
		em.remove(em.merge(department));
	}
	

	
	@NotifyChange("currentDepartment")
	@Command
	public void newDepartment() {
		this.currentDepartment = new Department();
	}
	
	@NotifyChange({"currentDepartment", "departments"})
	@Command
	public void save() {
		EntityManager em = JpaUtil.getEntityManager();
		em.merge(this.currentDepartment);
		this.currentDepartment = null;
	}
	
	@NotifyChange("currentDepartment")
	@Command
	public void cancel() {
		this.currentDepartment = null;
	}
}
