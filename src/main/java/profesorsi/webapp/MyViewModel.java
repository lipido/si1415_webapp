package profesorsi.webapp;

import javax.persistence.EntityManager;

import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zkplus.jpa.JpaUtil;

import persistence.Employee;

public class MyViewModel {

	private int count;

	@Init
	public void init() {
		count = 100;
	}

	@Command
	@NotifyChange({"count", "countEmployees"})
	public void cmd() {
		System.out.println("cmd()");
		++count;
		
		Employee e = new Employee();
		e.setName("Employee no. "+count);
		
		EntityManager em = JpaUtil.getEntityManager();
		em.persist(e);
	}

	public int getCount() {
		System.out.println("getCount()");
		return count;
	}
	
	public int getCountEmployees() {
		System.out.println("getCountEmployees()");
		EntityManager em = JpaUtil.getEntityManager();
		return em.createQuery("Select e FROM Employee e")
				.getResultList().size();
	}
}
