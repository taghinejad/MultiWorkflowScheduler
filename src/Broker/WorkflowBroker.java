package Broker;

import Broker.result;

import java.util.List;

import Broker.WorkflowBroker.ResourceProvision;
import DAG.*;

import algorithms.NoConstrained.CheapestCP;
import algorithms.NoConstrained.CheapestPolicy;
import algorithms.NoConstrained.FastestCP;
import algorithms.NoConstrained.FastestPolicy;
import algorithms.NoConstrained.HEFTAlgorithm;

import algorithms.NoConstrained.MyFast;


import algorithms.multiworkflow.CUSF;

import algorithms.multiworkflow.EUSF;
import algorithms.multiworkflow.NOSF;

public class WorkflowBroker {
	public WorkflowGraph graph;
	public WorkflowGraph[] graphs;
	public ResourceSet resources;
	public WorkflowPolicy policy;
	public static int interval = 3600;
	static long bandwidth = 20000000;
	public static float minFrequency = (float) 0.4;
	public static Boolean unifyRunTime = true;
	String SchedulerName;
	int time = 0;
	float cost = 0;
	public static ResourceTypes ResourceType;

	public ResourceSet getResources() {
		return resources;
	}

	public void setResources(ResourceSet resources) {
		this.resources = resources;
	}

	public enum ResourceProvision {
		ListAbrishami, EC2, EC2v2, Sample1, EC2ArabNejad, EC2020, RitaGargEnergyReliability, ENERGY1, EnergyHomogeneous,
		EnergyTang, ENERGY2, ENERGYfixPerEnergy, EnergyVarfied, EnergyLi_Z_CEAS, EnergySmall, EnergyJustFrequency,
		EnergyGeorgios, EnergyBasedOnRealEC2, EnergyBasedOnReal, EnergyRealEC2_2020,
	}

	public enum ResourceTypes {
		NotEnergy, EnergyFrequencyVoltage, EnergyJustFrequency, EnergyReliabilityFrequency,
		EnergyReliabilityFrequencyVoltage, EnergyFrequncyLoadLevel, FrquencyLoadLevel
	}

	public static int BindResource(ResourceProvision resource) {
		int res;
		if (resource == ResourceProvision.ListAbrishami)
			res = 0;
		else if (resource == ResourceProvision.EC2)
			res = 1;
		else if (resource == ResourceProvision.EC2v2)
			res = 2;
		else if (resource == ResourceProvision.Sample1)
			res = 3;
		else if (resource == ResourceProvision.EC2ArabNejad)
			res = 4;
		else if (resource == ResourceProvision.EC2020)
			res = 2020;
		else if (resource == ResourceProvision.EnergyRealEC2_2020)
			res = 20201;
		else if (resource == ResourceProvision.ENERGY1)
			res = 10;
		else if (resource == ResourceProvision.ENERGY2)
			res = 12;
		else if (resource == ResourceProvision.ENERGYfixPerEnergy)
			res = 101;
		else if (resource == ResourceProvision.EnergySmall)
			res = 20;
		else if (resource == ResourceProvision.EnergyVarfied)
			res = 21;
		else if (resource == ResourceProvision.EnergyLi_Z_CEAS)
			res = 22;
		else if (resource == ResourceProvision.EnergyJustFrequency)
			res = 23;
		else if (resource == ResourceProvision.EnergyHomogeneous)
			res = 24;
		else if (resource == ResourceProvision.EnergyGeorgios)
			res = 25;
		else if (resource == ResourceProvision.EnergyBasedOnReal)
			res = 26;
		else if (resource == ResourceProvision.EnergyBasedOnRealEC2)
			res = 27;
		else if (resource == ResourceProvision.EnergyTang)
			res = 28;
		else
			res = 2020;
		return res;
	}

	public void ReadyResources(int resGen) {
		switch (resGen) {
		case 0:
			createResourceList();
			break;
		case 1:
			createResourceListEC2();
			break;
		case 2:
			createResourceListEC2v2();
			break;
		case 3:
			createResourceListSample1();
			break;
		case 4:
			createResourceListEC2ArabNejad();
			break;
		case 2020:
			createResourceListEC2020();
			break;
		case 20201:
			createResourceListEnergyMyBasedonRealEC2_2020();
			break;
		case 9:
			createResourceListEnergyReliabilityFreq_Rita();
			break;

		case 10:
			createResourceListRitaEnergyFrequency();
		case 101:
			createResourceListRitaEnergyFrequencyFixEnergyPerSec();
			break;
		case 20:
			createResourceListRitaEnergyFrequencySmall();
			break;
		case 21:
			createResourceListEnergyFrequencyVerfied();
			break;
		case 22:
			createResourceListEnergyFrequencyLiZUNG();
			break;
		case 23:
			createResourceListJustFrequency();
			break;
		case 24:
			createResourceListEnergyHomogenious();
			break;
		case 25:
			createResourceListEnergyGeorgios();
			break;
		case 26:
			createResourceListEnergyMyBasedonReal();
			break;

		case 27:
			createResourceListEnergyMyBasedonRealEC2();
			break;
		case 28:
			createResourceListEnergyTang();
			break;
		case 11:
			createResourceListEnergyReliabilityFreq_Rita2();
			break;
		case 12:
			createResourceListEnergyReliabilityFreq_Rita3();
			break;
		case -1:
			createResourceListSample2();
			break;
		case 2021:
			createResourceListEnergyOnline_MultiWorkflow();
			break;

		}
	}

	public void ReadySingleWorkflowAlgorithms(ScheduleType type, WorkflowGraph graph, ResourceSet resources,
			long bandwidth) {
		if (type == ScheduleType.Fastest)
			policy = new FastestPolicy(graph, resources, bandwidth);
		else if (type == ScheduleType.Cheapest)
			policy = new CheapestPolicy(graph, resources, bandwidth);
		else if (type == ScheduleType.FastestCP)
			policy = new FastestCP(graph, resources, bandwidth);
		
	
		else if (type == ScheduleType.MY_FAST)
			policy = new MyFast(graph, resources, bandwidth);
		
		else if (type == ScheduleType.HEFT)
			policy = new HEFTAlgorithm(graph, resources, bandwidth);
	
	    else if (type == ScheduleType.CheapestCP)
			policy = new CheapestCP(graph, resources, bandwidth);
		
	}

	public void ReadyMultiWorkflowAlgorithms(ScheduleType type, WorkflowGraph[] graphs, ResourceSet resources,
			long bandwidth) {

		if (type == ScheduleType.EUSF)
			policy = new EUSF(graphs, resources, bandwidth);
		else if (type == ScheduleType.CUSF)
			policy = new CUSF(graphs, resources, bandwidth);
		else if (type == ScheduleType.NOSF)
			policy = new NOSF(graphs, resources, bandwidth);
		
		else if (type == ScheduleType.HEFT)
			policy = new HEFTAlgorithm(graphs, resources, bandwidth);
		
	}

	public WorkflowBroker(String wfDescFile, ScheduleType type, int interval, long bandwidth, int resGen)
			throws Exception {
		DAG.Adag dag = null;
		this.interval = interval;
		this.bandwidth = bandwidth;
		utility.configuration.setBandwidth(this.getBandwidth());
		try {
			dag = DagUtils.readWorkflowDescription(wfDescFile);
		} catch (Throwable e) {
			System.out.println("Error reading Workflow File " + e);
		}
		graph = new WorkflowGraph();
		graph.convertDagToWorkflowGraph(dag);

		ReadyResources(resGen);
		ReadySingleWorkflowAlgorithms(type, graph, resources, bandwidth);
	}

	public WorkflowBroker(List <String> wfDescFile, ScheduleType type, int interval, long bandwidth, int resGen)
			throws Exception {
		int dagNumber = wfDescFile.size();
		DAG.Adag dag = null;
		DAG.Adag[] dags = new DAG.Adag[dagNumber];

		this.interval = interval;
		this.bandwidth = bandwidth;
		utility.configuration.setBandwidth(this.getBandwidth());
		int i = 0;
		for (String wfd : wfDescFile) {
			try {
				dag = DagUtils.readWorkflowDescription(wfd);
				dags[i] = dag;
			} catch (Throwable e) {
				System.out.println("Error reading Workflow File " + e);
			}
			i++;
		}
		graphs = new WorkflowGraph[dagNumber];

		for (int j = 0; j < dagNumber; j++) {

			graphs[j] = new WorkflowGraph();
			graphs[j].convertDagToWorkflowGraph(dags[j], wfDescFile.get(j));
		}

		ReadyResources(resGen);
		ReadyMultiWorkflowAlgorithms(type, graphs, resources, bandwidth);

	}

	public float schedule(int startTime, int deadline) {
		return (policy.schedule(startTime, deadline));
	}

	public float schedule(int startTime, int deadline, float cost) {
		return (policy.schedule(startTime, deadline, cost));
	}



	public float schedule(List<Long> startTimes, List<Long> deadlines, float cost) {
		return (policy.schedule(startTimes, deadlines, cost));
	}

	private void createResourceList() {
		resources = new ResourceSet(interval);
		// Resource(Id, Cost, MIPS)
		resources.addResource(new Resource(0, 5, 100));
		resources.addResource(new Resource(1, (float) 4.5, 90));
		resources.addResource(new Resource(2, (float) 4, 80));
		resources.addResource(new Resource(3, (float) 3.5, 70));
		resources.addResource(new Resource(4, (float) 3, 60));
		resources.addResource(new Resource(5, (float) 2.5, 50));
		resources.addResource(new Resource(6, (float) 2, 40));
		resources.addResource(new Resource(7, (float) 1.5, 30));
		resources.addResource(new Resource(8, (float) 1.25, 25));
		resources.addResource(new Resource(9, (float) 1, 20));

		resources.sort();
	}

//	private void createResourceListEnergyReliabilityRita() {
//		resources = new ResourceSet(interval);
//		// Resource(Id, Cost, MIPS,)
//		resources.addResource(new Resource(0, 5, 100, 0.00001, 5));
//		resources.addResource(new Resource(1, (float) 4.5, 90, 0.00001, (float) 4.5));
//		resources.addResource(new Resource(2, (float) 3.75, 75, 0.00001, (float) 3.75));
//		resources.addResource(new Resource(3, (float) 2.5, 50, 0.00001, (float) 2.5));
//		resources.sort();
//	}

	private void createResourceListEnergyReliabilityFreq_Rita() {
		resources = new ResourceSet(interval);
		// Resource(Id, Cost, MIPS,FaultRate, EnergyPerSec, MinFrequency,ChangeFrequncy
		// )
//		resources.addResource(new Resource(0, 5, 100, 0.00001, (float)2.5, (float) 0.5, (float) 0.2));
//		resources.addResource(new Resource(1, (float) 4.5, 90, 0.00001, (float)2.25, (float) 0.5, (float) 0.2));
//		resources.addResource(new Resource(2, (float) 3.75, 75, 0.00001, (float)1.8, (float) 0.5, (float) 0.2));
//		resources.addResource(new Resource(3, (float) 2.5, 50, 0.00001, (float)1.25, (float) 0.5, (float) 0.2));
//		resources.addResource(new Resource(4, (float) 1.25, 25, 0.00001, (float)0.62, (float) 0.5, (float) 0.2));
//		resources.addResource(new Resource(5, (float) 0.5, 10, 0.00001, (float)0.25, (float) 0.5, (float) 0.2));
//		
		resources.addResource(new Resource(5, (float) 5.64, 168, 0.00001, (float) 0.25, (float) 0.5, (float) 0.2));
		resources.addResource(new Resource(4, (float) 3.76, 128, 0.00001, (float) 0.225, (float) 0.5, (float) 0.2));
		resources.addResource(new Resource(3, (float) 1.88, 70, 0.00001, (float) 0.18, (float) 0.5, (float) 0.2));
		resources.addResource(new Resource(2, (float) 0.94, 37, 0.00001, (float) 0.15, (float) 0.5, (float) 0.2));
		resources.addResource(new Resource(1, (float) 0.47, 16, 0.00001, (float) 0.10, (float) 0.5, (float) 0.2));
		resources.addResource(new Resource(0, (float) 0.235, 10, 0.00001, (float) 0.0825, (float) 0.5, (float) 0.2));

		resources.sort();
	}

	private void createResourceListRitaEnergyFrequency() {
		resources = new ResourceSet(interval);

//									
//		( id,  CostPerInterval,  MIPS,  FaultRate,  EnergyPerSec, MaxFrequency,  MinFrequency, ChangeFrequency) 

		resources.addResource(
				new Resource(5, (float) 5.64, 168, 0.00001, (float) 0.1, (float) 3.00, (float) 1.50, (float) 0.2));
		resources.addResource(
				new Resource(4, (float) 3.76, 128, 0.00001, (float) 0.1, (float) 2.30, (float) 1.1, (float) 0.2));
		resources.addResource(
				new Resource(3, (float) 1.88, 70, 0.00001, (float) 0.1, (float) 2.3, (float) 1.21, (float) 0.2));
		resources.addResource(
				new Resource(2, (float) 0.94, 37, 0.00001, (float) 0.1, (float) 1.21, (float) 0.605, (float) 0.2));
		resources.addResource(
				new Resource(1, (float) 0.47, 16, 0.00001, (float) 0.1, (float) 1.21, (float) 0.605, (float) 0.2));
		resources.addResource(
				new Resource(0, (float) 0.235, 10, 0.00001, (float) 0.1, (float) 1.21, (float) 0.605, (float) 0.2));

		resources.sort();
	}

	private void createResourceListJustFrequency() {
		resources = new ResourceSet(interval);

//		
//( id,  CostPerInterval,  MIPS,  FaultRate,  EnergyPerSec, MaxFrequency,  MinFrequency, ChangeFrequency) 

		resources.addResource(
				new Resource(3, (float) 3.76, 168, 0.00001, (float) 0.1, (float) 5, (float) 2.5, (float) 0.2));
		resources.addResource(
				new Resource(2, (float) 1.88, 128, 0.00001, (float) 0.1, (float) 3.81, (float) 1.9, (float) 0.2));
		resources.addResource(
				new Resource(1, (float) 0.94, 70, 0.00001, (float) 0.1, (float) 2.08, (float) 1.039, (float) 0.2));
		resources.addResource(
				new Resource(0, (float) 0.47, 40, 0.00001, (float) 0.1, (float) 1.19, (float) 0.59, (float) 0.2));

		resources.sort();
	}

	private void createResourceListEnergyHomogenious() {
		resources = new ResourceSet(interval);

//		
//( id,  CostPerInterval,  MIPS,  FaultRate,  EnergyPerSec, MaxFrequency,  MinFrequency, ChangeFrequency) 

		resources.addResource(new Resource(0, (float) 0.94, 70, 0.00001, (float) 0.1, 2, 1, (float) 0.2));

		resources.sort();
	}

	private void createResourceListEnergyGeorgios() {
		resources = new ResourceSet(interval);

		// ( id, CostPerInterval, MIPS, FaultRate, EnergyPerSec, MaxFrequency,
		// MinFrequency, MaxVoltage,minVoltage, ChangeFrequency)
		resources.addResource(new Resource(4, (float) 2, 128, 0.00001, (float) 0.1, (float) 3.73, (float) 1.246,
				(float) 2.072, (float) 0.75, (float) 0.1));
		resources.addResource(new Resource(3, (float) 1.44, 70, 0.00001, (float) 0.1, (float) 3.16, (float) 1.755,
				(float) 1.35, (float) 0.85, (float) 0.1));
		resources.addResource(new Resource(2, (float) 0.72, 37, 0.00001, (float) 0.1, (float) 2.93, (float) 1.627,
				(float) 1.35, (float) 0.75, (float) 0.1));
		resources.addResource(new Resource(1, (float) 0.36, 16, 0.00001, (float) 0.1, (float) 2.13, (float) 1.42,
				(float) 1.5, (float) 1, (float) 0.1));
		resources.addResource(new Resource(0, (float) 0.18, 10, 0.00001, (float) 0.1, (float) 1.87, (float) 1.246,
				(float) 1.5, (float) 1, (float) 0.1));
		resources.sort();
	}

	private void createResourceListEnergyTang() {
		resources = new ResourceSet(interval);

		// ( id, CostPerInterval, MIPS, FaultRate, EnergyPerSec, MaxFrequency,
		// MinFrequency, MaxVoltage,minVoltage, ChangeFrequency)
		resources.addResource(new Resource(3, (float) 1.44, 70, 0.00001, (float) 0.1, (float) 2.0, (float) 0.8,
				(float) 1.5, (float) 0.9, (float) 0.1));
		resources.addResource(new Resource(2, (float) 0.72, 37, 0.00001, (float) 0.1, (float) 1.4, (float) 0.6,
				(float) 1.484, (float) 0.956, (float) 0.1));
		resources.addResource(new Resource(1, (float) 0.36, 16, 0.00001, (float) 0.1, (float) 2.6, (float) 1.0,
				(float) 1.30, (float) 1.05, (float) 0.1));
		resources.addResource(new Resource(0, (float) 0.18, 10, 0.00001, (float) 0.1, (float) 1.8, (float) 0.8,
				(float) 1.20, (float) 0.9, (float) 0.1));
		resources.sort();
	}

	private void createResourceListEnergyMyBasedonRealEC2() {
		resources = new ResourceSet(interval);

		// ( id, CostPerInterval, MIPS, FaultRate, EnergyPerSec, MaxFrequency,
		// MinFrequency, MaxVoltage,minVoltage, ChangeFrequency)
		resources.addResource(new Resource(4, (float) 2, 128, 0.00001, (float) 0.1, (float) 3.73, (float) 1.246,
				(float) 2.072, (float) 0.75, (float) 0.1));
		resources.addResource(new Resource(3, (float) 1.44, 70, 0.00001, (float) 0.1, (float) 3.16, (float) 1.755,
				(float) 1.35, (float) 0.85, (float) 0.1));
		resources.addResource(new Resource(2, (float) 0.72, 37, 0.00001, (float) 0.1, (float) 2.93, (float) 1.627,
				(float) 1.35, (float) 0.75, (float) 0.1));
		resources.addResource(new Resource(1, (float) 0.36, 16, 0.00001, (float) 0.1, (float) 2.13, (float) 1.42,
				(float) 1.5, (float) 1, (float) 0.1));
		resources.addResource(new Resource(0, (float) 0.18, 10, 0.00001, (float) 0.1, (float) 1.87, (float) 1.246,
				(float) 1.5, (float) 1, (float) 0.1));
		resources.sort();
	}

	private void createResourceListEnergyMyBasedonRealEC2_20201() {
		resources = new ResourceSet(interval);

		// ( id, CostPerInterval, MIPS, FaultRate, EnergyPerSec, MaxFrequency,
		// MinFrequency, MaxVoltage,minVoltage, ChangeFrequency)
		resources.addResource(new Resource("Intel-Xeon", 5, (float) 5.64, 168, 0.00001, (float) 0.1, (float) 2.8,
				(float) 1.2, (float) 1.5, (float) 0.9, (float) 0.1));
		resources.addResource(new Resource("AMD Opteron 2218", 4, (float) 3.76, 128, 0.00001, (float) 0.1, (float) 2.6,
				(float) 1, (float) 1.30, (float) 1.05, (float) 0.1));
		resources.addResource(new Resource("AMD Atholon-64", 3, (float) 1.88, 70, 0.00001, (float) 0.1, (float) 2.0,
				(float) 0.8, (float) 1.50, (float) 0.9, (float) 0.1));
		resources.addResource(new Resource("AMD Turion MT-34", 2, (float) 0.94, 37, 0.00001, (float) 0.1, (float) 1.8,
				(float) 0.8, (float) 1.2, (float) 0.90, (float) 0.1));
		resources.addResource(new Resource("Intel Pentium M", 1, (float) 0.47, 16, 0.00001, (float) 0.1, (float) 1.4,
				(float) 0.6, (float) 1.484, (float) 0.956, (float) 0.1));
		resources.addResource(new Resource(0, (float) 0.18, 10, 0.00001, (float) 0.1, (float) 1.2, (float) 0.5,
				(float) 1.5, (float) 1, (float) 0.1));

		resources.sort();
	}

	private void createResourceListEnergyMyBasedonRealEC2_2020() {
		resources = new ResourceSet(interval);

		// ( id, CostPerInterval, MIPS, FaultRate, EnergyPerSec, MaxFrequency,
		// MinFrequency, MaxVoltage,minVoltage, ChangeFrequency)
		resources.addResource(new Resource(5, (float) 5.64, 168, 0.00001, (float) 1.68, (float) 3.0, (float) 1.74,
				(float) 1.35, (float) 0.85, (float) 0.1));
		resources.addResource(new Resource(4, (float) 3.76, 128, 0.00001, (float) 1.28, (float) 3.0, (float) 1.74,
				(float) 1.35, (float) 0.85, (float) 0.1));
		resources.addResource(new Resource(3, (float) 1.88, 70, 0.00001, (float) 0.70, (float) 3.0, (float) 1.74,
				(float) 1.35, (float) 0.85, (float) 0.1));
		resources.addResource(new Resource(2, (float) 0.94, 37, 0.00001, (float) 0.37, (float) 3.0, (float) 1.74,
				(float) 1.35, (float) 0.85, (float) 0.1));
		resources.addResource(new Resource(1, (float) 0.47, 16, 0.00001, (float) 0.16, (float) 3.0, (float) 1.74,
				(float) 1.35, (float) 0.85, (float) 0.1));
		resources.addResource(new Resource(0, (float) 0.235, 10, 0.00001, (float) 0.10, (float) 3.0, (float) 1.74,
				(float) 1.35, (float) 0.85, (float) 0.1));

		resources.sort();
	}

	private void createResourceListEnergyOnline_MultiWorkflow() {
		resources = new ResourceSet(interval);
//1. Liu J, Ren J, Dai W, et al. Online Multi-Workflow Scheduling under Uncertain Task Execution Time in IaaS Clouds. IEEE Trans Cloud Comput. 2019;PP(c):1. doi:10.1109/TCC.2019.2906300

		// ( id, CostPerInterval, MIPS, FaultRate, EnergyPerSec, MaxFrequency,
		// MinFrequency, MaxVoltage,minVoltage, ChangeFrequency)

		// VCPUes impact is calculated in EnergyPerSec;

		resources.addResource(new Resource(6, (float) 0.98, 80, 0.00001, (float) 1.6, (float) 3.0, (float) 1.74,
				(float) 1.35, (float) 0.85, (float) 0.1));
		resources.addResource(new Resource(5, (float) 0.49, 34, 0.00001, (float) 0.66, (float) 3.0, (float) 1.74,
				(float) 1.35, (float) 0.85, (float) 0.1));
		resources.addResource(new Resource(4, (float) 0.35, 30, 0.00001, (float) 0.6, (float) 3.0, (float) 1.74,
				(float) 1.35, (float) 0.85, (float) 0.1));
		resources.addResource(new Resource(3, (float) 0.245, 14, 0.00001, (float) 0.28, (float) 3.0, (float) 1.74,
				(float) 1.35, (float) 0.85, (float) 0.1));
		resources.addResource(new Resource(2, (float) 0.175, 12, 0.00001, (float) 0.24, (float) 3.0, (float) 1.74,
				(float) 1.35, (float) 0.85, (float) 0.1));
		resources.addResource(new Resource(1, (float) 0.87, 6, 0.00001, (float) 0.12, (float) 3.0, (float) 1.74,
				(float) 1.35, (float) 0.85, (float) 0.1));
		resources.addResource(new Resource(0, (float) 0.044, 5, 0.00001, (float) 0.10, (float) 3.0, (float) 1.74,
				(float) 1.35, (float) 0.85, (float) 0.1));

		resources.sort();
	}

	private void createResourceListEnergyMyBasedonReal() {
		resources = new ResourceSet(interval);

		// ( id, CostPerInterval, MIPS, FaultRate, EnergyPerSec, MaxFrequency,
		// MinFrequency, MaxVoltage,minVoltage, ChangeFrequency)
		resources.addResource(new Resource(4, (float) 1, 100, 0.00001, (float) 0.1, (float) 2.0, (float) 1.4,
				(float) 2.0, (float) 0.75, (float) 0.1));
		resources.addResource(new Resource(3, (float) 0.65, 60, 0.00001, (float) 0.1, (float) 1.4, (float) 0.6,
				(float) 1.35, (float) 0.85, (float) 0.1));
		resources.addResource(new Resource(2, (float) 0.5, 48, 0.00001, (float) 0.1, (float) 2.6, (float) 1.0,
				(float) 1.35, (float) 0.75, (float) 0.1));
		resources.addResource(new Resource(1, (float) 0.27, 30, 0.00001, (float) 0.1, (float) 2.13, (float) 1.42,
				(float) 1.5, (float) 1, (float) 0.1));
		resources.addResource(new Resource(0, (float) 0.18, 20, 0.00001, (float) 0.1, (float) 1.87, (float) 1.246,
				(float) 1.5, (float) 1, (float) 0.1));
		resources.sort();
	}

	private void createResourceListEnergyFrequencyVerfied() {
		resources = new ResourceSet(interval);

//									
//		( id,  CostPerInterval,  MIPS,  FaultRate,  EnergyPerSec, MaxFrequency,  MinFrequency, ChangeFrequency) 

		resources.addResource(
				new Resource(4, (float) 6, 168, 0.00001, (float) 0.4, (float) 3, (float) 1.5, (float) 0.2));
		resources.addResource(
				new Resource(3, (float) 4.57, 128, 0.00001, (float) 0.4, (float) 2.3, (float) 1.15, (float) 0.2));
		resources.addResource(
				new Resource(2, (float) 2.28, 64, 0.00001, (float) 0.2, (float) 2.30, (float) 1.15, (float) 0.2));
		resources.addResource(
				new Resource(1, (float) 1.61, 45, 0.00001, (float) 0.2, (float) 1.6171, (float) 0.8085, (float) 0.2));
		resources.addResource(
				new Resource(0, (float) 0.80, 23, 0.00001, (float) 0.1, (float) 1.6171, (float) 0.8085, (float) 0.2));

		resources.sort();
	}

	private void createResourceListEnergyFrequencyLiZUNG() {
		resources = new ResourceSet(interval);
///Li, Z., Ge, J., Hu, H., Song, W., Hu, H., & Luo, B. (2015). Cost and Energy Aware Scheduling Algorithm for Scientific Workflows with Deadline Constraint in Clouds. EEE TRANSACTIONS ON SERVICES COMPUTING, 11(4), 713�726.
//									
//		( id,  CostPerInterval,  MIPS,  FaultRate,  EnergyPerSec, MaxFrequency,  MinFrequency, ChangeFrequency) 
		resources.addResource(
				new Resource(9, (float) 1.40, 55, 0.00001, (float) 0.01, (float) 5.5, (float) 2.75, (float) 0.2));
		resources.addResource(
				new Resource(8, (float) 1.21, 50, 0.00001, (float) 0.01, (float) 5, (float) 2.5, (float) 0.2));
		resources.addResource(
				new Resource(7, (float) 1.05, 45, 0.00001, (float) 0.01, (float) 4.5, (float) 2.25, (float) 0.2));
		resources.addResource(
				new Resource(6, (float) 0.90, 40, 0.00001, (float) 0.01, (float) 4, (float) 2, (float) 0.2));
		resources.addResource(
				new Resource(5, (float) 0.73, 35, 0.00001, (float) 0.01, (float) 3.5, (float) 1.75, (float) 0.2));
		resources.addResource(
				new Resource(4, (float) 0.58, 30, 0.00001, (float) 0.01, (float) 3, (float) 1.5, (float) 0.2));
		resources.addResource(
				new Resource(3, (float) 0.46, 25, 0.00001, (float) 0.01, (float) 2.5, (float) 1.25, (float) 0.2));
		resources.addResource(
				new Resource(2, (float) 0.32, 20, 0.00001, (float) 0.01, (float) 2, (float) 1, (float) 0.2));
		resources.addResource(
				new Resource(1, (float) 0.20, 15, 0.00001, (float) 0.01, (float) 1.5, (float) 0.75, (float) 0.2));
		resources.addResource(
				new Resource(0, (float) 0.10, 10, 0.00001, (float) 0.01, (float) 1, (float) 0.50, (float) 0.2));

		resources.sort();
	}

	private void createResourceListRitaEnergyFrequencyFixEnergyPerSec() {
		resources = new ResourceSet(interval);
		// 2.3 GHz (base) and 2.7 GHz (turbo) Intel Xeon E5-2686 v4 Processor
		// https://aws.amazon.com/ec2/instance-types/?trkCampaign=acq_paid_search_brand&sc_channel=PS&sc_campaign=acquisition_GB&sc_publisher=Google&sc_category=Cloud%20Computing&sc_country=GB&sc_geo=EMEA&sc_outcome=acq&sc_detail=%2Bec2&sc_content={ad%20group}&sc_matchtype=b&sc_segment=474715257970&sc_medium=ACQ-P|PS-GO|Brand|Desktop|SU|Cloud%20Computing|EC2|GB|EN|Sitelink&s_kwcid=AL!4422!3!474715257970!b!!g!!%2Bec2&ef_id=CjwKCAiAqJn9BRB0EiwAJ1SztfDpTuHAcDGzPE-cuW2ItiJBqytyy2zM498NbT9KFcSzSIa0tMBwBRoCtFQQAvD_BwE:G:s&s_kwcid=AL!4422!3!474715257970!b!!g!!%2Bec2

		// ( id, CostPerInterval, MIPS, FaultRate, EnergyPerSec, MaxFrequency,
		// MinFrequency, ChangeFrequency)

		resources.addResource(
				new Resource(5, (float) 5.64, 168, 0.00001, (float) 1, (float) 2.70, (float) 1.35, (float) 0.2));
		resources.addResource(
				new Resource(4, (float) 3.76, 128, 0.00001, (float) 0.76, (float) 2.70, (float) 1.35, (float) 0.2));
		resources.addResource(
				new Resource(3, (float) 1.88, 70, 0.00001, (float) 0.416, (float) 2.70, (float) 1.35, (float) 0.2));
		resources.addResource(
				new Resource(2, (float) 0.94, 37, 0.00001, (float) 0.22, (float) 2.70, (float) 1.35, (float) 0.2));
		resources.addResource(
				new Resource(1, (float) 0.47, 16, 0.00001, (float) 0.095, (float) 2.70, (float) 1.35, (float) 0.2));
		resources.addResource(
				new Resource(0, (float) 0.235, 10, 0.00001, (float) 0.0595, (float) 2.70, (float) 1.35, (float) 0.2));

		resources.sort();
	}

	private void createResourceListRitaEnergyFrequencySmall() {
		resources = new ResourceSet(interval);
		// 2.3 GHz (base) and 2.7 GHz (turbo) Intel Xeon E5-2686 v4 Processor
		// https://aws.amazon.com/ec2/instance-types/?trkCampaign=acq_paid_search_brand&sc_channel=PS&sc_campaign=acquisition_GB&sc_publisher=Google&sc_category=Cloud%20Computing&sc_country=GB&sc_geo=EMEA&sc_outcome=acq&sc_detail=%2Bec2&sc_content={ad%20group}&sc_matchtype=b&sc_segment=474715257970&sc_medium=ACQ-P|PS-GO|Brand|Desktop|SU|Cloud%20Computing|EC2|GB|EN|Sitelink&s_kwcid=AL!4422!3!474715257970!b!!g!!%2Bec2&ef_id=CjwKCAiAqJn9BRB0EiwAJ1SztfDpTuHAcDGzPE-cuW2ItiJBqytyy2zM498NbT9KFcSzSIa0tMBwBRoCtFQQAvD_BwE:G:s&s_kwcid=AL!4422!3!474715257970!b!!g!!%2Bec2

		// ( id, CostPerInterval, MIPS, FaultRate, EnergyPerSec, MaxFrequency,
		// MinFrequency, ChangeFrequency)

		resources.addResource(
				new Resource(5, (float) 5.64, 168, 0.00001, (float) 0.5, (float) 2.70, (float) 1.9, (float) 0.2));
		resources.addResource(
				new Resource(4, (float) 3.76, 128, 0.00001, (float) 0.5, (float) 2.70, (float) 1.9, (float) 0.2));
		resources.addResource(
				new Resource(3, (float) 1.88, 70, 0.00001, (float) 0.5, (float) 2.70, (float) 1.9, (float) 0.2));
		resources.addResource(
				new Resource(2, (float) 0.94, 37, 0.00001, (float) 0.5, (float) 2.70, (float) 1.9, (float) 0.2));
		resources.addResource(
				new Resource(1, (float) 0.47, 16, 0.00001, (float) 0.5, (float) 2.70, (float) 1.9, (float) 0.2));
		resources.addResource(
				new Resource(0, (float) 0.235, 10, 0.00001, (float) 0.5, (float) 2.70, (float) 1.9, (float) 0.2));

		resources.sort();
	}

	public static float getVoltageByFrequency(float freqLevel) {
		// A Green Energy-aware task scheduling using the DVFS technique in Cloud
		// Computing
		int f = Math.round(freqLevel * 100);
		float voltage = (float) 1.5;
		switch (f) {
		case 40:
			voltage = (float) 0.9;
			break;
		case 50:
			voltage = (float) 0.95;
			break;
		case 60:
			voltage = (float) 1.0;
			break;
		case 70:
			voltage = (float) 1.05;
			break;
		case 80:
			voltage = (float) 1.1;
			break;
		case 90:
			voltage = (float) 1.15;
			break;
		case 100:
			voltage = (float) 1.2;
			break;
		}
		return voltage;
	}

	private void createResourceListEnergyReliabilityFreq_Rita2() {
		resources = new ResourceSet(interval);
		// Resource(Id, Cost, MIPS,FaultRate, EnergyPerSec, MinFrequency,ChangeFrequncy
		// )
		resources.addResource(new Resource(0, 5, 100, 0.00001, 5, (float) 1, (float) 0.2));
		resources.addResource(new Resource(1, (float) 4.5, 90, 0.00001, (float) 4.5, (float) 1, (float) 0.2));
		resources.addResource(new Resource(2, (float) 3.75, 75, 0.00001, (float) 3.75, (float) 1, (float) 0.2));
		resources.addResource(new Resource(3, (float) 2.5, 50, 0.00001, (float) 2.5, (float) 1, (float) 0.2));
		resources.sort();
	}

	private void createResourceListEnergyReliabilityFreq_Rita3() {
		resources = new ResourceSet(interval);
		// Resource(Id, Cost, MIPS,FaultRate, EnergyPerSec, MinFrequency,ChangeFrequncy
		// )
		resources.addResource(new Resource(5, (float) 5.64, 168, 0.00001, 5, (float) 1, (float) 0.2));
		resources.addResource(new Resource(4, (float) 3.76, 128, 0.00001, 5, (float) 1, (float) 0.2));
		resources.addResource(new Resource(3, (float) 1.88, 70, 0.00001, 5, (float) 1, (float) 0.2));
		resources.addResource(new Resource(2, (float) 0.94, 37, 0.00001, (float) 4.5, (float) 1, (float) 0.2));
		resources.addResource(new Resource(1, (float) 0.47, 16, 0.00001, (float) 3.75, (float) 1, (float) 0.2));
		resources.addResource(new Resource(0, (float) 0.235, 10, 0.00001, (float) 2.5, (float) 1, (float) 0.2));

		resources.sort();
	}

	private void createResourceListEC2() {
		// based on S. Sadat, M. Nik, M. Naghibzadeh, and Y. Sedaghat, “with deadline
		// and reliability constraints,” Computing, no. 0123456789, 2019.
		resources = new ResourceSet(interval);
		// Resource(Id, Cost, MIPS)
		resources.addResource(new Resource(5, (float) 1.0, 26));
		resources.addResource(new Resource(4, (float) 0.50, 13));
		resources.addResource(new Resource(3, (float) 0.48, 8));
		resources.addResource(new Resource(2, (float) 0.24, 4));
		resources.addResource(new Resource(1, (float) 0.12, 2));
		resources.addResource(new Resource(0, (float) 0.06, 1));
		resources.sort();
	}

	private void createResourceListEC2020() {
		resources = new ResourceSet(interval);
		// Resource(Id, Cost, MIPS)
		// https://aws.amazon.com/ec2/pricing/on-demand/
		// name ECU Memory (GiB) Instance Storage (GB) Linux/UNIX Usage
		// m5.large 10 8-GiB EBS Only $0.096 per Hour
		// m5.xlarge 16 16-GiB EBS Only $0.192 per Hour
		// m5.2xlarge 37 32-GiB EBS Only $0.384 per Hour
		// m5.4xlarge 70 64-GiB EBS Only $0.768 per Hour
		// m5.8xlarge 128 128-GiB EBS Only $1.536 per Hour
		// m5.12xlarge 168 192-GiB EBS Only $2.304 per Hour

		resources.addResource(new Resource(5, (float) 5.64, 168));
		resources.addResource(new Resource(4, (float) 3.76, 128));
		resources.addResource(new Resource(3, (float) 1.88, 70));
		resources.addResource(new Resource(2, (float) 0.94, 37));
		resources.addResource(new Resource(1, (float) 0.47, 16));
		resources.addResource(new Resource(0, (float) 0.235, 10));
		resources.sort();
	}

	private void createResourceListEC2ArabNejad() {
		resources = new ResourceSet(interval);
		// Resource(Id, Cost, MIPS)
		resources.addResource(new Resource(5, (float) 2.520, (float) 124.5));
		resources.addResource(new Resource(4, (float) 1.008, (float) 53.5));
		resources.addResource(new Resource(3, (float) 0.504, 26));
		resources.addResource(new Resource(2, (float) 0.266, 13));
		resources.addResource(new Resource(1, (float) 0.126, (float) 6.5));
		resources.addResource(new Resource(0, (float) 0.067, 3));
		resources.sort();
	}

	private void createResourceListEC2v2() {
		resources = new ResourceSet(interval);
		// Resource(Id, Cost, MIPS)
		resources.addResource(new Resource(5, (float) 1.0, 26000));
		resources.addResource(new Resource(4, (float) 0.50, 13000));
		resources.addResource(new Resource(3, (float) 0.48, 8000));
		resources.addResource(new Resource(2, (float) 0.24, 4000));
		resources.addResource(new Resource(1, (float) 0.12, 2000));
		resources.addResource(new Resource(0, (float) 0.06, 1000));
		resources.sort();
	}

	private void createResourceListSample1() {
		resources = new ResourceSet(interval);
		// Resource(Id, Cost, MIPS)
		// resources.addResource(new Resource(2, (float) 3, 30));

		resources.addResource(new Resource(3, (float) 0.4, 40));
		resources.addResource(new Resource(2, (float) 0.3, 30));
		resources.addResource(new Resource(1, (float) 0.2, 20));
		resources.addResource(new Resource(0, (float) 0.1, 10));
		resources.sort();
	}

	private void createResourceListSample2() {
		resources = new ResourceSet(interval);
		// Resource(Id, Cost, MIPS)
		// resources.addResource(new Resource(2, (float) 3, 30));

//		resources.addResource(new Resource(3, (float) 0.4, 40));
//		resources.addResource(new Resource(2, (float) 0.3, 30));
		resources.addResource(new Resource(1, (float) 1, 40, 0.00001, (float) 2, (float) 1, (float) 0.2));
		resources.addResource(new Resource(0, (float) 2, 80, 0.00001, (float) 3, (float) 1, (float) 0.2));
		resources.sort();

	}

	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}

	public long getBandwidth() {
		return bandwidth;
	}

	public void setBandwidth(long bandwidth) {
		this.bandwidth = bandwidth;
	}

	public WorkflowBroker(String wfDescFile, ScheduleType type, int interval, long bandwidth) throws Exception {
		DAG.Adag dag = null;
		this.interval = interval;
		this.bandwidth = bandwidth;
		try {
			dag = DagUtils.readWorkflowDescription(wfDescFile);
		} catch (Throwable e) {
			System.out.println("Error reading Workflow File " + e);
		}
		graph = new WorkflowGraph();
		graph.convertDagToWorkflowGraph(dag);
		// createResourceList()
		//

		createResourceListEC2();

		if (type == ScheduleType.Fastest)
			policy = new FastestPolicy(graph, resources, bandwidth);
	
		else if (type == ScheduleType.Cheapest)
			policy = new CheapestPolicy(graph, resources, bandwidth);
		
	
		else if (type == ScheduleType.MY_FAST)
			policy = new MyFast(graph, resources, bandwidth);
		
		else if (type == ScheduleType.HEFT)
			policy = new HEFTAlgorithm(graph, resources, bandwidth);
		

	}

}
