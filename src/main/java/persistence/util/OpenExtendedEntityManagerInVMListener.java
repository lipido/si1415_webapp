package persistence.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

import org.jboss.logging.Logger;
import org.zkoss.bind.BindComposer;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.WebApp;
import org.zkoss.zk.ui.metainfo.ComponentInfo;
import org.zkoss.zk.ui.util.Composer;
import org.zkoss.zk.ui.util.ComposerExt;
import org.zkoss.zk.ui.util.DesktopCleanup;
import org.zkoss.zk.ui.util.FullComposer;
import org.zkoss.zk.ui.util.WebAppCleanup;
import org.zkoss.zk.ui.util.WebAppInit;

/**
 * <p>
 * A ZK listener aiming at finding ZK ViewModels with attributes annotated
 * with extended {@link PersistenceContext}.
 * </p> 
 * 
 * <p>
 * This is the standard way of requesting an EntityManager in Java EE
 * applications. However, the Java EE container should inject the EntityManager 
 * instance. In order to be able to run a ZK application directly in non-full 
 * Java EE servers, such as Apache Tomcat, this listener does this work. 
 * </p>
 * 
 * <p>
 * To enable it, edit the zk.xml file and add a &lt;listener&gt; node with
 * this class, as well as, the 
 * {@code OpenExtendedEntityManagerInVMListener.DefaultPersistenceUnitName}
 * preference with the name of the persistence unit you want to inject.
 * </p>
 *  
 * @author lipido
 *
 */
public class OpenExtendedEntityManagerInVMListener implements 
	Composer<Component>, 
	ComposerExt<Component>, 
	FullComposer, 
	WebAppInit, 
	WebAppCleanup,
	DesktopCleanup {

	private static final String DEFAULT_PERSISTENCE_UNIT_PREFERENCE = 
			"OpenExtendedEntityManagerInVMListener.DefaultPersistenceUnitName";

	private static String defaultPersistenceUnitName = null;
	private static Logger LOGGER = 
			Logger.getLogger(
					OpenExtendedEntityManagerInVMListener.class.getName());
	
	private static final Map<Desktop, EntityManager> openEntityManagers =
			new HashMap<Desktop, EntityManager>();
	
	@Override
	public ComponentInfo doBeforeCompose(
			Page page, 
			Component arg0, 
			ComponentInfo compInfo) throws Exception {
		
		return compInfo;
	}

	@Override
	public void init(WebApp arg0) throws Exception {
		OpenExtendedEntityManagerInVMListener
			.defaultPersistenceUnitName = 
				arg0.getConfiguration().getPreference(
						DEFAULT_PERSISTENCE_UNIT_PREFERENCE, null);
		
		
	}
	@Override
	public void cleanup(WebApp wapp) throws Exception {
		for (EntityManager em : openEntityManagers.values()) {
			closeEntityManager(em);
		}
		EntityManagerFactoryMultiton.getInstance().close();
		
	}
	
	@Override
	public void cleanup(Desktop desktop) throws Exception {
		if (openEntityManagers.containsKey(desktop) &&
			openEntityManagers.get(desktop).isOpen() ) {
			EntityManager em = openEntityManagers.get(desktop);
			closeEntityManager(em);
		}
		
	}
	private void closeEntityManager(EntityManager em) {
		if (em.isOpen()) {
			LOGGER.info("Closing EntityManager: "+em);
			em.close();
		}
	}

	@Override
	public void doBeforeComposeChildren(Component component) throws Exception {
		Object ocomposer = component.getAttribute("$composer");
		if (ocomposer ==null || ! (ocomposer instanceof BindComposer)){
			return;
		}
		
		BindComposer<?> composer = (BindComposer<?>) ocomposer;
		Object vmo = composer.getViewModel();
		
		if (vmo == null) return;
			
		Class<?> clazz = vmo.getClass();
		
		//find PersistenceContext
		for (Field field: clazz.getDeclaredFields()){
			if (!EntityManager.class.isAssignableFrom(field.getType()))
				continue;
			
			for (Annotation annotation : field.getAnnotations()){
				if (!annotation.annotationType().equals(
						PersistenceContext.class))
					continue;
				
				PersistenceContext annot = (PersistenceContext) annotation;
				
				if (annot.type() == PersistenceContextType.EXTENDED){
					String name = annot.unitName();
					if (name == null || name.equals("")){
						name = defaultPersistenceUnitName;
					}
					if (name != null){
						EntityManager em = EntityManagerFactoryMultiton
								.getInstance().getEntityManagerFactory(name)
								.createEntityManager();
						field.setAccessible(true);
						field.set(vmo, em);
						
						LOGGER.info("Injected entity manager "+em+" on "+vmo);
						
						openEntityManagers.put(component.getDesktop(), em);
					}
					else {
						LOGGER.warn(
							"Cannot inject PersistenceContext in View Model "
							+ "of class " + clazz+ ". "
							+ "No persistence unit name found in the "
							+ "@PersistenceContext "
							+ "annotation nor the zk.xml preference "
							+ DEFAULT_PERSISTENCE_UNIT_PREFERENCE);
					}
					
				}else{
					LOGGER.warn(
							"Cannot inject PersistenceContext in View Model "
							+ "of class "+ clazz+ ". "
							+ "Only @PersistenceContext("
							+ "type=PersistenceContextType.EXTENDED)"
							+ " can be injected");
				}
			}
		}
	}

	@Override
	public boolean doCatch(Throwable ex) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void doFinally() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doAfterCompose(Component comp) throws Exception {
		// TODO Auto-generated method stub
	}

	
}

/**
 * A multiton of {@link EntityManagerFactory} instances. For every persistence
 * unit, there will be only one factory.
 * 
 * @author lipido
 *
 */
class EntityManagerFactoryMultiton {
	
	private static Logger LOGGER = Logger
			.getLogger(EntityManagerFactoryMultiton.class.getName());
	
	private Map<String, EntityManagerFactory> emfs = 
			new HashMap<String, EntityManagerFactory>();
	
	private static EntityManagerFactoryMultiton _instance = 
			new EntityManagerFactoryMultiton();
	
	public static EntityManagerFactoryMultiton getInstance(){
		return _instance;
	}
	
	public void close() {
		for (EntityManagerFactory emf : emfs.values()){
			try{
				LOGGER.info("Closing EntityManagerFactory: "+emf);
				emf.close();
			}catch(Throwable e){
				LOGGER.error("Could not close EntityManagerFactory "+ emf);
			}
		}
	}
	
	public EntityManagerFactory getEntityManagerFactory(String unitName){
		
		if (!emfs.containsKey(unitName)){
			emfs.put(
					unitName, 
					Persistence.createEntityManagerFactory(unitName));
		}
		return emfs.get(unitName);
	}
}
