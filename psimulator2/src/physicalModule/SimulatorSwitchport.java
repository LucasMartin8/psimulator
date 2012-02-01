/*
 * created 24.1.2012
 */
package physicalModule;

import dataStructures.L2Packet;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents "interface" on L2.
 * For switchport connected to real network implement your own class.
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class SimulatorSwitchport extends Switchport {

	public SimulatorSwitchport(String name, Connector connector, PhysicMod physicMod) {
		super(connector, physicMod);
	}

	public SimulatorSwitchport(String name, PhysicMod physicMod) {
		super(physicMod);
	}
	/**
	 * Storage for packets to be sent.
	 */
	private List<L2Packet> buffer = Collections.synchronizedList(new LinkedList<L2Packet>());

	/**
	 * Current size of buffer in bytes.
	 */
	private int size = 0;
	/**
	 * Capacity of buffer in bytes.
	 */
	private int capacity = 150000; // zatim: 100 x max velikost ethernetovyho pakatu
	/**
	 * Count of dropped packets.
	 *
	 * @param packet
	 * @return
	 */
	private int dropped = 0;

	@Override
	public void sendPacket(L2Packet packet) {
		int packetSize = packet.getSize();
		if ((size + packetSize > capacity) || connector == null) { // (drop packet, run out of capacity) || (no cable is connected)
			dropped++;
		} else {
			size += packetSize;
			buffer.add(packet);
			connector.getCable().worker.wake();
		}
	}

	/**
	 * Receives packet from cable and pass it to physical module.
	 */
	@Override
	public void receivePacket(L2Packet packet) {
		physicMod.receivePacket(packet, this);
	}

	/**
	 * Removes packet form buffer and returns it, decrements size of buffer. Synchronised via buffer. Throws exception when this method
	 * is called and no packet is in buffer.
	 *
	 * @return
	 */
	public L2Packet popPacket() {
		L2Packet packet;
		packet = buffer.remove(0);
		size -= packet.getSize();
		return packet;
	}

	/**
	 * Return true if buffer is empty.
	 * Synchronied via buffer.
	 */
	public boolean isEmptyBuffer() {
		if (buffer.isEmpty()) {
			return true;
		}
		return false;
	}
}