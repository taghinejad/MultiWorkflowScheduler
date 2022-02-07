package Broker;

import java.util.ArrayList;

public class InstanceSet {
	private ResourceSet resources;
	private ArrayList<Instance> instances;
	private int size = 0;

	public InstanceSet(ResourceSet rs) {
		resources = rs;
		instances = new ArrayList<Instance>();
	}
	public ArrayList<Instance>  getinstances() {
		return this.instances;
	}

	public static  ArrayList<Instance> copy(ArrayList<Instance> ins) {
		ArrayList<Instance> newins = new ArrayList<Instance>();
		for (int i = 0; i < ins.size(); i++) {
			newins.add(ins.get(i));
		}
		return newins;
	}

	public void addInstance(Instance inst) {
		instances.add(inst);
		size++;
	}

	public void removeInstance(Instance inst) {
		instances.remove(inst);
		size--;
	}

	public Instance getInstance(int index) {
		if (index < size)
			return (instances.get(index));
		else
			return null;
	}

	public ArrayList<Instance> returnAllInstances() {
		return this.instances;
	}

	public void addInstanceSet(ArrayList<Instance> instancesSet) {
		instances.addAll(instancesSet);
		size += instancesSet.size();
	}

	public void replaceInstanceSet(ArrayList<Instance> instancesSet) {
		removeAllInstances();
		instances.addAll(instancesSet);
		size = instancesSet.size();
	}

	public int getSize() {
		return (size);
	}

	public void removeAllInstances() {
		size = 0;
		instances.removeAll(instances);
	}

}
