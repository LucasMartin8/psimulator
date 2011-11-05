/*
 * created 28.10.2011
 */
package networkDevice;

import java.util.ArrayList;
import java.util.List;
import networkModule.NetworkModule;
import physicalModule.AbstractNetworkInterface;
import psimulator2.WorkerThread;

/**
 * Seznam sitovych rozhrani reprezentujici fyzicke rozhrani
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class PhysicalInterfaces extends WorkerThread {

	private List<AbstractNetworkInterface> interfaceList;
	private NetworkModule networkModule;

	public PhysicalInterfaces(NetworkModule networkModule, List<AbstractNetworkInterface> ifaces) {
		this.networkModule = networkModule;
		this.interfaceList = ifaces;
	}

	public PhysicalInterfaces(NetworkModule networkModule) {
		this.networkModule = networkModule;
		interfaceList = new ArrayList<AbstractNetworkInterface>();
	}

	public void addInterface(AbstractNetworkInterface iface) {
		interfaceList.add(iface);
	}

	public boolean removeInterface(AbstractNetworkInterface iface) {
		return interfaceList.remove(iface);
	}

	@Override
	protected void doMyWork() { // TODO: dopsat obsluhu prichozich paketu - jen jedno kolecko nebo cyklus?

		for (AbstractNetworkInterface iface : interfaceList) {
			if (!iface.isBufferEmpty()) {
				networkModule.acceptPacket(iface.getL2PacketFromBuffer(), iface);
			}
		}

		throw new UnsupportedOperationException("Not implemented completaly yet.");
	}
}