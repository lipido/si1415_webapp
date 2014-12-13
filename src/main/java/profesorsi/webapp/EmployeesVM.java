package profesorsi.webapp;

import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zkplus.jpa.JpaUtil;

import persistence.Department;
import persistence.Employee;

public class EmployeesVM {

	private Employee currentEmployee = null;
	
	public Employee getCurrentEmployee() {
		return currentEmployee;
	}
	
	public Collection<Department>
	getDepartments() {
		System.out.println("getDepartments()");
		EntityManager em = JpaUtil.getEntityManager();
		
		List<Department> departments = em.createQuery("SELECT d FROM Department d",
				Department.class).getResultList();
		for (Department d : departments) {
			d.getEmployees().size();
		}
		
		return departments;
	}
	
	public Collection<Employee>
		getEmployees() {
		System.out.println("getEmployees()");
		EntityManager em = JpaUtil.getEntityManager();
		
		return em.createQuery("SELECT e FROM Employee e",
				Employee.class).getResultList();
	}
	
	@NotifyChange("currentEmployee")
	@Command
	public void edit(@BindingParam("employee") Employee employee) {
		this.currentEmployee = employee;
	}
	
	@NotifyChange("employees")
	@Command
	public void delete(@BindingParam("employee") Employee employee) {
		EntityManager em = JpaUtil.getEntityManager();
		em.remove(em.merge(employee));
	}
	

	
	@NotifyChange("currentEmployee")
	@Command
	public void newEmployee() {
		this.currentEmployee = new Employee();
	}
	
	@NotifyChange({"currentEmployee", "employees", "departments"})
	@Command
	public void save() {
		EntityManager em = JpaUtil.getEntityManager();
		em.merge(this.currentEmployee);
		this.currentEmployee = null;
	}
	
	@NotifyChange("currentEmployee")
	@Command
	public void cancel() {
		this.currentEmployee = null;
	}
}
