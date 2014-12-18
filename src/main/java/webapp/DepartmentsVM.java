package webapp;

import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;

import persistence.Department;
import persistence.util.Transaction;
import persistence.util.TransactionUtil;

public class DepartmentsVM {

	private Department currentDepartment = null;
	
	@PersistenceContext(type=PersistenceContextType.EXTENDED)
	private EntityManager em;
	
	private boolean edit = false; //edit / new department
	
	public Department getCurrentDepartment() {
		return currentDepartment;
	}
	public Collection<Department>
		getDepartments() {
		
		return em.createQuery("SELECT d FROM Department d",
				Department.class).getResultList();
	}
	
	@NotifyChange("currentDepartment")
	@Command
	public void edit(@BindingParam("department") Department department) {
		this.edit = true;
		this.currentDepartment = department;
	}
	
	@NotifyChange("departments")
	@Command
	public void delete(@BindingParam("department") final Department department) {
		TransactionUtil.doTransaction(em, new Transaction(){
			@Override
			public void doTransation(EntityManager em) {
				em.remove(department);
			}
		});
	}
	
	@NotifyChange("currentDepartment")
	@Command
	public void newDepartment() {
		this.edit = false;
		this.currentDepartment = new Department();
	}
	
	@NotifyChange({"currentDepartment", "departments"})
	@Command
	public void save() {
		TransactionUtil.doTransaction(em, new Transaction(){
			@Override
			public void doTransation(EntityManager em) {
				if (!edit) {
					em.persist(currentDepartment);
				} else {
					// do nothing, only save changes when transaction ends...
				}
			}
		});
		
		this.currentDepartment = null;
	}
	
	@NotifyChange("currentDepartment")
	@Command
	public void cancel() {
		this.currentDepartment = null;
	}
}
