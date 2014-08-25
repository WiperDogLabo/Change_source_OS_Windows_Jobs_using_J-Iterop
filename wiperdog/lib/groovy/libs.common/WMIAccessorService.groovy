import java.util.List;

import org.jinterop.dcom.impls.automation.IJIDispatch;
import org.jinterop.dcom.core.JIVariant;

interface WMIAccessorService{
	JIVariant[] getJIVariants(String WMIClazz);
	List<IJIDispatch> getObjectsDispatch(String WMIClazz);
}