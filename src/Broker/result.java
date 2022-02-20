package Broker;

import java.util.List;

public class result {
	public float cost;
	public int finishTime, startTime;
	public long energy;
	public float reliability = 1;
	public float freq;
	public int usage;
	public float gapFitpercent = 0;
	public int gapid;
	// in-place means that set the task in specific position (startTime and
	// finishTime) of the instance.

	public boolean inplace = false;
	public long totalEnergy = 0;
	public float freqLevel = 1;
	public int transferTime = 0;
	public int instId = -1;
	public int currentInstance = -1;
	public int RunnedWorkflows = 0;

	public int MissedWorkflows = 0;

	public List<Integer> finishTimes;

	public List<Long> deadlines;
	public List<Long> starts;
	public List<Integer> energies;
	public List<Integer> WfTaskSizes;
	public List<Float> costs;
	public int count = 0;
	public String algorithmName;
	public float utilization;
	public int instanceSize = 0;

	public float getEnergyKillo() {
		if (energy == 0)
			energy = totalEnergy;
		return (energy / (float) 1000);

	}

	public float getTotalEnergyKillo() {
		return (totalEnergy / (float) 1000);

	}

	public boolean isLimitSuccessfull() {
		// TODO Auto-generated method stub
		return false;
	}
}