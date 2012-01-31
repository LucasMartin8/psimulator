/*
 * Erstellt am 27.10.2011.
 */

package device;

import physicalModule.PhysicMod;
import networkModule.NetMod;

/**
 *
 * @author neiss
 */
public class AbstractDevice {

	String name;
	PhysicMod physicalModule;
	NetMod networkModule;
	ApplicationsList applications;

	public ApplicationsList getApplications() {
		return applications;
	}

	public String getName() {
		return name;
	}

	public NetMod getNetworkModule() {
		return networkModule;
	}

	public PhysicMod getPhysicalModule() {
		return physicalModule;
	}
	
}
