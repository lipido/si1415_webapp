package webapp;

import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;

import persistence.Department;
import persistence.Employee;
import persistence.util.Transaction;
import persistence.util.TransactionUtil;

public class EmployeesVM {

	private Employee currentEmployee = null;
	private boolean edit = false; //edit / new mode
	
	@PersistenceContext(type=PersistenceContextType.EXTENDED)
	private EntityManager em;
	
	public Employee getCurrentEmployee() {
		return currentEmployee;
	}
	
	public Collection<Department> getDepartments() {
		System.out.println("getDepartments()");
		
		List<Department> departments = 
				em.createQuery("SELECT d FROM Department d", Department.class)
		.getResultList();
		
		return departments;
	}
	
	public Collection<Employee>
		getEmployees() {
		System.out.println("getEmployees() Extended");
		
		return em.createQuery("SELECT e FROM Employee e",
				Employee.class).getResultList();
	}
	
	@NotifyChange("currentEmployee")
	@Command
	public void edit(@BindingParam("employee") Employee employee) {
		this.edit = true;
		this.currentEmployee = employee;
	}
	
	@NotifyChange("employees")
	@Command
	public void delete(@BindingParam("employee") final Employee employee) {
		TransactionUtil.doTransaction(em, new Transaction(){
			@Override
			public void doTransation(EntityManager em) {
				em.remove(employee); //without merge!!
			}}
		);
	}
	
	@NotifyChange("currentEmployee")
	@Command
	public void newEmployee() {
		this.edit = false;
		this.currentEmployee = new Employee();
	}
	
	@NotifyChange({"currentEmployee", "employees"})
	@Command
	public void save() {
		
		TransactionUtil.doTransaction(em, new Transaction(){

			@Override
			public void doTransation(EntityManager em) {
				if (!edit) {
					// new employee
					em.persist(currentEmployee);
				} else {
					// update existing employee
					// to save a persistent entity, there is no need to 
					// do anything, only to close this transaction
				}
			}
		});
		
		this.currentEmployee = null;
	}
	
	@NotifyChange("currentEmployee")
	@Command
	public void cancel() {
		this.currentEmployee = null;
	}
}
