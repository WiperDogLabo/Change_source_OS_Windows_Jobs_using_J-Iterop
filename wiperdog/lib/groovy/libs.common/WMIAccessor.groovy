import org.jinterop.dcom.core.IJIComObject;
import org.jinterop.dcom.core.JIArray;
import org.jinterop.dcom.impls.automation.IJIEnumVariant;

import static org.jinterop.dcom.core.JIProgId.valueOf;
import static org.jinterop.dcom.impls.JIObjectFactory.narrowObject;
import static org.jinterop.dcom.impls.automation.IJIDispatch.IID;

import java.util.List;
import java.util.logging.Level;

import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.common.JISystem;
import org.jinterop.dcom.core.JIComServer;
import org.jinterop.dcom.core.JISession;
import org.jinterop.dcom.core.JIString;
import org.jinterop.dcom.core.JIVariant;
import org.jinterop.dcom.impls.automation.IJIDispatch;
import jcifs.smb.SmbAuthException
class WMIAccesor implements WMIAccessorService {
		String domain;
	String hostname;
	String username;
	String password;
	private IJIDispatch wbemServices;
	public Object[] params = null;

	public WMIAccesor(String domain, String hostname, String username,
	String password) {
		this.domain = domain;
		this.hostname = hostname;
		this.username = username;
		this.password = password;
		JISystem.getLogger().setLevel(Level.OFF);
		JISystem.setAutoRegisteration(true);
		JISession dcomSession = JISession.createSession(domain, username, password);
		dcomSession.useSessionSecurity(true);
		try {
			JIComServer comServer = new JIComServer(
					valueOf("WbemScripting.SWbemLocator"), hostname,
					dcomSession);
			IJIDispatch wbemLocator = (IJIDispatch) narrowObject(comServer
					.createInstance().queryInterface(IID));
			params = [
				new JIString(hostname),
				new JIString("ROOT\\CIMV2"),
				JIVariant.OPTIONAL_PARAM(),
				JIVariant.OPTIONAL_PARAM(),
				JIVariant.OPTIONAL_PARAM(),
				JIVariant.OPTIONAL_PARAM(),
				new Integer(0),
				JIVariant.OPTIONAL_PARAM()
			];
			def results = wbemLocator.callMethodA("ConnectServer",params);
			this.wbemServices = (IJIDispatch) narrowObject(results[0]
					.getObjectAsComObject());
		} catch (SmbAuthException e) {
			println "[WMIMonitoring] - Failed to authenticate: " + e
		} catch(org.jinterop.dcom.common.JIException e) {
             println "[WMIMonitoring] - " +  e.getMessage()
         	if(e.getMessage().contains("Windows Registry")){
         		println "[WMIMonitoring] - Please try to start Remote Registry service from Windows services catalogue before using WMI moitoring"
         	}
	    } catch(Exception e){
			println "[WMIMonitoring] - Failed to init WMIAccessor: " + e
		}
	}

	public List<IJIDispatch> getObjectsDispatch(String WMIClazz) {
		def variants = getJIVariants(WMIClazz)
		try {
			IJIDispatch wbemObjectSet = (IJIDispatch) narrowObject(variants[0]
					.getObjectAsComObject());

			JIVariant newEnumvariant = wbemObjectSet.get("_NewEnum");
			IJIComObject object2 = newEnumvariant.getObjectAsComObject();
			IJIEnumVariant enumVARIANT = (IJIEnumVariant) narrowObject(object2
					.queryInterface(IJIEnumVariant.IID));

			JIVariant countVariant = wbemObjectSet.get("Count");
			int numberOfServices = countVariant.getObjectAsInt();
			List<IJIDispatch> lstWbemObjectDisp = new ArrayList<IJIDispatch>();
			for (int i = 0; i < numberOfServices; i++) {
				Object[] elements = enumVARIANT.next(1);
				JIArray aJIArray = (JIArray) elements[0];			
				JIVariant[] array = (JIVariant[]) aJIArray.getArrayInstance();
				for (JIVariant variant : array) {
					lstWbemObjectDisp.add((IJIDispatch) narrowObject(variant
							.getObjectAsComObject()));
				}				
			}
			return lstWbemObjectDisp
			
		} catch (JIException e) {
			e.printStackTrace();
			return null
		}
	}
	public JIVariant[] getJIVariants(String WMIClazz) {
		JIVariant[] arrJIVariant = null;
		try {
			params = [
				new JIString(WMIClazz),
				new Integer(0),
				JIVariant.OPTIONAL_PARAM()
			];
			arrJIVariant = wbemServices.callMethodA("InstancesOf", params);
		} catch (JIException e) {
			e.printStackTrace();
			return null
		}
		return arrJIVariant;
	}
}