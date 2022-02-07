package Broker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.io.Serializable;

public class ResourceSet implements Serializable {
	private ArrayList<Resource>  resources;
	private float maxMIPS, meanMIPS, minMIPS;
	private float maxCost, minCost;
	private int size=0;
	private int timeInterval;
	
	public ResourceSet(int interval) {
		resources = new ArrayList<Resource>();
		timeInterval = interval;
	}
	
	public float getMaxMIPS() {
		return(maxMIPS);
	}

	public float getMinMIPS() {
		return(minMIPS);
	}
	
	public float getMeanMIPS() {
		return(meanMIPS);
	}
	
	public float getMaxCost() {
		return(maxCost);
	}

	public float getMinCost() {
		return(minCost);
	}

	public int getSize() {
		return(size);
	}
	
	public int getInterval() {
		return (timeInterval);
	}
	
	public void addResource(Resource res) {
		resources.add(res) ;
		size ++;
	}
	
	

	public Resource getResource(int index) {
		if (index < size)
			return (resources.get(index));
		else 
			return null;
	}
	public Resource getResourceByID(int index) {
		for (Resource resource : resources) {
			if (resource.getId()==index)
				return (resource);
		}
		return null;
	}
	
	public Resource getMinResource() {
		return (resources.get(size-1));
	}
	
	public int getMinId() {
		return (size-1);
	}

	public Resource getMaxResource() {
		return (resources.get(0));
	}
	public Resource getMyMaxResource() {
		//Resource rs;

		Resource maxrs=resources.get(0);
		for(Resource rs:resources)
		{
			if (rs.getMIPS()>maxrs.getMIPS())
			{
				maxrs=rs;
			}
		}
		return maxrs;
	}
	public int getMyMaxResourceId() {
		//Resource rs;
		
		Resource maxrs=resources.get(0);
		for(Resource rs:resources)
		{
			if (rs.getMIPS()>maxrs.getMIPS())
			{
				maxrs=rs;
			}
		}
		return maxrs.getId();
	}
	public int getMyMaxResourceIndex() {
		//Resource rs;
		int index=0;
		Resource maxrs=resources.get(0);
		for (int i = 1; i < resources.size(); i++) {
			if (resources.get(i).getMIPS()>maxrs.getMIPS())
			{
				index=i;
			}
		}

		return index;
	}
	
	public int getMaxId() {
		return (0);
	}

	public void sort() {
		Collections.sort(resources, new MIPSComparator());
		
		maxMIPS = resources.get(0).getMIPS();
		maxCost = resources.get(0).getCost();
		minMIPS = resources.get(size-1).getMIPS();
		minCost = resources.get(size-1).getCost();
		
		meanMIPS = 0;
		for (Resource res : resources) 
			meanMIPS += res.getMIPS();
		meanMIPS /= size; 
	}

	public void computeParameters() {
		maxMIPS = resources.get(0).getMIPS();
		maxCost = resources.get(0).getCost();
		minMIPS = resources.get(size-1).getMIPS();
		minCost = resources.get(size-1).getCost();
		
		meanMIPS = 0;
		for (Resource res : resources) 
			meanMIPS += res.getMIPS();
		meanMIPS /= size; 
	}
	
	
	private class MIPSComparator implements Comparator<Resource> {
		public int compare(Resource res1, Resource res2) {
			if (res1.getMIPS() < res2.getMIPS())
				return (1) ;
			else if  (res1.getMIPS() > res2.getMIPS())
				return (-1);
			else 
				return(0) ;
		}
	}
}
