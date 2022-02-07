package Broker;

import java.io.Serializable;

import Broker.WorkflowBroker.ResourceTypes;

public class Resource implements Serializable {
	private int id;
	private String name;
	private float costPerInterval;
	private float MIPS;
	private double faultRate;
	private float energyPerSec;
	private float minFrequency, maxFrequency = 0, changeFrequency, Voltage, minVoltage, loadLevel, frequency;
	private int VCPU;
	

	public Resource(int newId) {
		id = newId;
		costPerInterval = 0;
		MIPS = 0;
	}

	public Resource(int newId, float cost, float mips) {
		WorkflowBroker.ResourceType = ResourceTypes.NotEnergy;
		id = newId;
		costPerInterval = cost;
		MIPS = mips;
	}

//	public Resource(int id, float CostPerInterval, float MIPS, double FaultRate) {
//		super();
//		this.id = id;
//		
//		setCostPerInterval(CostPerInterval);
//		setMIPS(MIPS);
//	
//		setFaultRate(FaultRate);
//	}
//	public Resource(int id,  float CostPerInterval, float MIPS, double FaultRate, float EnergyPerSec) {
//		super();
//		this.id = id;
//
//		 WorkflowBroker.ResourceType=ResourceTypes.EnergyFrequncyLoadLevel;
//		setCostPerInterval(CostPerInterval);
//		setMIPS(MIPS);
//		setEnergyPerSec(EnergyPerSec);
//		setFaultRate(FaultRate);
//
//	}

	public Resource(int id, float CostPerInterval, float MIPS, double FaultRate, float EnergyPerSec, float MinFrequency,
			float ChangeFrequency) {
		super();
		this.id = id;
		setCostPerInterval(CostPerInterval);
		setMIPS(MIPS);
		setChangeFrequency(ChangeFrequency);
		setFaultRate(FaultRate);
		setEnergyPerSec(EnergyPerSec);
		setMinFrequency(MinFrequency);

	}

//	public Resource(int id, float CostPerInterval, float MIPS, double FaultRate, float EnergyPerSec, float MinFrequency,
//			float ChangeFrequency, float Voltage,float frequency) {
//		super();
//		this.id = id;
//		this.costPerInterval = costPerInterval;
//		MIPS = MIPS;
//		this.faultRate = faultRate;
//		
//		this.minFrequency = minFrequency;
//		this.changeFrequency = changeFrequency;
//		setCostPerInterval(CostPerInterval);
//		setMIPS(MIPS);
//		setChangeFrequency(ChangeFrequency);
//		setFaultRate(FaultRate);
//		setEnergyPerSec(EnergyPerSec);
//		setMinFrequency(MinFrequency);
//		
//		setVoltage(Voltage);
//
//	}

	public Resource(int id, float CostPerInterval, float MIPS, double FaultRate, float EnergyPerSec, float MaxFrequency,
			float MinFrequency, float ChangeFrequency) {
		super();
		this.id = id;
		//
		WorkflowBroker.ResourceType = ResourceTypes.EnergyFrequncyLoadLevel;

		setCostPerInterval(CostPerInterval);
		setMIPS(MIPS);
		setChangeFrequency(ChangeFrequency);
		setFaultRate(FaultRate);
		setEnergyPerSec(EnergyPerSec);
		setMinFrequency(MinFrequency);
		setMaxFrequency(MaxFrequency);
	}

	public Resource(int id, float CostPerInterval, float MIPS, double FaultRate, float EnergyPerSec, float MaxFrequency,
			float MinFrequency, float voltage, float minvoltage, float ChangeFrequency) {
		super();
		this.id = id;
		//
		WorkflowBroker.ResourceType = ResourceTypes.EnergyFrequncyLoadLevel;

		setCostPerInterval(CostPerInterval);
		setMIPS(MIPS);
		setChangeFrequency(ChangeFrequency);
		setFaultRate(FaultRate);
		setEnergyPerSec(EnergyPerSec);
		setMinFrequency(MinFrequency);
		setMaxFrequency(MaxFrequency);
		this.setVoltage(voltage);
		this.setMinVoltage(minvoltage);
	}

	public Resource(String name, int id, float CostPerInterval, float MIPS, double FaultRate, float EnergyPerSec,
			float MaxFrequency, float MinFrequency, float voltage, float minvoltage, float ChangeFrequency) {
		super();
		this.id = id;
		this.name = name;
		//
		WorkflowBroker.ResourceType = ResourceTypes.EnergyFrequncyLoadLevel;

		setCostPerInterval(CostPerInterval);
		setMIPS(MIPS);
		setChangeFrequency(ChangeFrequency);
		setFaultRate(FaultRate);
		setEnergyPerSec(EnergyPerSec);
		setMinFrequency(MinFrequency);
		setMaxFrequency(MaxFrequency);
		this.setVoltage(voltage);
		this.setMinVoltage(minvoltage);
	}

	public Resource(Resource r) {
		id = r.id;
		costPerInterval = r.costPerInterval;
		MIPS = r.MIPS;
		this.faultRate = r.faultRate;
	}

	public int getId() {
		return (id);
	}

	public float getLoadLevel(float freq) {
		return ((float) freq / maxFrequency);

	}

//	public void setLoadLevel(float loadLevel) {
//		this.loadLevel = loadLevel;
//	}

	public float getMaxFrequency() {
		return maxFrequency;
	}

	public float getMaxFrequencyOr1() {
		if (maxFrequency == 0)
			return 1;
		return maxFrequency;
	}

	public void setMaxFrequency(float maxFrequency) {
		this.maxFrequency = maxFrequency;
	}

	public float getVoltage() {
		return Voltage;
	}

	public void setVoltage(float voltage) {
		this.Voltage = voltage;
	}

	public float getMinVoltage() {
		return minVoltage;
	}

	public void setMinVoltage(float minVoltage) {
		this.minVoltage = minVoltage;
	}

	public float getCost() {
		return (costPerInterval);
	}

	public float getMIPS() {
		return (MIPS);
	}

	public void setMIPS(float mips) {
		this.MIPS = mips;
	}

	public void setCost(float newCost) {
		if (newCost >= 0)
			costPerInterval = newCost;
	}

	public double getFaultRate() {
		return faultRate;
	}

	public void setFaultRate(double faultRate) {
		this.faultRate = faultRate;
	}

	public float getCostPerInterval() {
		return costPerInterval;
	}

	public void setCostPerInterval(float costPerInterval) {
		this.costPerInterval = costPerInterval;
	}

	public float getEnergyPerSec() {
		return energyPerSec;
	}

	public void setEnergyPerSec(float EnergyPerSec) {
		this.energyPerSec = EnergyPerSec;
	}

	public float getMinFrequency() {
		return minFrequency;
	}

	public float getMinFrequencyLevel() {
		if (maxFrequency > 0)
			return ((float) minFrequency / maxFrequency);
		else
			return 1;

	}

	public void setMinFrequency(float minFrequency) {
		this.minFrequency = minFrequency;
	}

	public float getChangeFrequency() {
		return changeFrequency;
	}

	public void setChangeFrequency(float changeFrequency) {
		this.changeFrequency = changeFrequency;
	}

}
