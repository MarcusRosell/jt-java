package uk.ac.liv.jt.internal;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BundleAccessor
{
	public static final String PLUGIN_ID = "uk.ac.liv.jt";	//$NON-NLS-1$

	private BundleAccessor()
	{
		// intentionally made empty
	}

	static BundleContext getContext() {
		return FrameworkUtil.getBundle( BundleAccessor.class ).getBundleContext();
	}

	public static Logger getLogger()
	{
		return LoggerFactory.getLogger( BundleAccessor.PLUGIN_ID );
	}
}
