package Broker;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import Broker.execution;
import Broker.result;
import utility.configuration;
import Broker.WorkflowBroker.ResourceTypes;

public abstract class WorkflowPolicy {
	protected WorkflowGraph graph;
	protected WorkflowGraph[] graphs;
	public ResourceSet resources;
	public InstanceSet instances;
	protected long bandwidth;
	protected final long MB = 1000000;
	protected final double pricePerMB = 0;
	float fq = (float) 0.2;
	public String currentAlgrotihm = "";
	protected int nodeSize = 0;
	public static float fluctpercent = (float) 0;
	public static int alarmTaskSizeLimit = 0;

	public WorkflowPolicy(WorkflowGraph g, ResourceSet rs, long bw) {
		graph = g;
		resources = rs;
		instances = new InstanceSet(resources);
		bandwidth = bw;

	}

	public WorkflowPolicy(WorkflowGraph[] gs, ResourceSet rs, long bw) {
		graphs = gs;
		resources = rs;
		instances = new InstanceSet(resources);
		bandwidth = bw;

	}

	public long getBandwith() {
		return this.bandwidth;
	}

	abstract public float schedule(int startTime, int deadline);

	abstract public float schedule(int startTime, int deadline, float cost);

	abstract public float schedule(List<Long> startTime, List<Long> deadline, float cost);

	protected void setRuntimes() {
		float maxMIPS = resources.getMaxMIPS();
		for (WorkflowNode node : graph.getNodes().values())
			node.setRunTime((int) Math.ceil((float) node.getInstructionSize() / maxMIPS));
	}

	protected void setRuntimes(WorkflowGraph wg) {
		float maxMIPS = resources.getMaxMIPS();
		for (WorkflowNode node : wg.getNodes().values())
			node.setRunTime((int) Math.ceil((float) node.getInstructionSize() / maxMIPS));

	}

	protected void setRuntimesStochastic(WorkflowGraph wg) {
		float maxMIPS = resources.getMaxMIPS();
		int run = 0;
		for (WorkflowNode node : wg.getNodes().values()) {
			run = (int) Math.ceil((float) node.getInstructionSize() / maxMIPS);
			run = (int) Math.round((run + fluctpercent*Math.sqrt(calcFluctuation(run))));
			node.setRunTime(run);
		}

	}

	protected void setRuntimesStochastic2(WorkflowGraph wg) {
		float maxMIPS = resources.getMaxMIPS();
		int run = 0;
		for (WorkflowNode node : wg.getNodes().values()) {
			run = Math.round((float) node.getInstructionSize() / maxMIPS);
			run = Math.round(run + calcFluctuation(run));
			node.setRunTime(run);
		}

	}

	public int GetFileTransferTimeInInstance(List<File> Filesets, Instance in) {
		long transferSize = in.getNotExistedFileSizes(Filesets);
		int tTime = (int) Math.round((double) transferSize / bandwidth);
		return tTime;
	}

	public int getTransferTime(WorkflowNode curNode, Instance in) {
		float ts = 0;
		for (Link parent : curNode.getParents()) {
			WorkflowNode parentNode = null;
			try {
				parentNode = graph.getNodes().get(parent.getId());
			} catch (Exception e) {
				for (WorkflowGraph workflowGraph : graphs) {
					parentNode = workflowGraph.getNodes().get(parent.getId());
					if (parentNode != null)
						break;
				}

			}

			if (parentNode.getSelectedResource() != in.getId())
				ts += (float) parent.getDataSize() / bandwidth;
		}
		return Math.round(ts);
	}

	public int getTransferTime(WorkflowNode curNode, Instance in, WorkflowGraph graph) {
		float ts = 0;
		for (Link parent : curNode.getParents()) {
			WorkflowNode parentNode = graph.getNodes().get(parent.getId());
			if (parentNode.getSelectedResource() != in.getId())
				ts += (float) parent.getDataSize() / bandwidth;
		}
		return Math.round(ts);
	}

	public long GetMyFileTransferTime(long curStart, WorkflowNode curNode, Instance in) {
		if (!utility.Utility.readConsideration) {
			int start;
			for (Link parent : curNode.getParents()) {
				WorkflowNode parentNode = null;
				if (curNode.graphid >= 0) {
					graph = graphs[curNode.graphid];
				}
				try {
					parentNode = graph.getNodes().get(parent.getId());

				} catch (Exception e) {
					// TODO: handle exception
					for (int i = 0; i < graphs.length; i++) {
						parentNode = graphs[i].getNodes().get(parent.getId());
						if (parentNode != null)
							break;
					}
				}

				start = parentNode.getEFT();
				if (parentNode.getSelectedResource() != in.getId())
					start += Math.round((float) parent.getDataSize() / bandwidth);
				if (start > curStart)
					curStart = start;
			}
			return curStart;

		} else {
			try {
				if (curNode.getId().contains("start") || curNode.getId().contains("end"))
					return 0;

				int start = 0;
				int max = -1;
				List<File> nodeFiles = new ArrayList<File>();
				nodeFiles.addAll(curNode.getFileSet());
				List<File> ParentsFiles = new ArrayList<File>();
				for (Link parent : curNode.getParents()) {
					WorkflowNode parentNode = graph.getNodes().get(parent.getId());
					List<File> ParentFiles = filesFromParentNode(curNode, parentNode);
					ParentsFiles.addAll(ParentFiles);
					// calculate file transfer Time from nodes parents if them does not exist in the
					// instance;
					int parentData = GetFileTransferTimeInInstance(ParentFiles, in);
					start = parentData + parentNode.getEFT();
					if (max < start)
						max = start;
				}

				// calculates SelfReadDataTransferTime;
				nodeFiles.removeAll(ParentsFiles);
				nodeFiles.removeAll(curNode.getOutputFileSet());
				long SelfInputFileSize = 0;
				// calculate file transfer Time from nodes parents if them does not exist in the
				// instance;
				int SelfData = GetFileTransferTimeInInstance(nodeFiles, in);
				// SelfData=0;
				// end

				if ((SelfData) > max)
					max = SelfData;

				if (max > curStart)
					curStart = max;

				if (utility.Utility.readConsideration == true)
					InsertFilesToInstance(curNode, in);

				return curStart;
			} catch (Exception e) {
				// System.out.println(e.getMessage());

				return curStart;
			}
		}

	}

	protected void MysetInstance(WorkflowNode curNode, Instance curInst) {
		long start, curStart = curInst.getFinishTime(), curFinish, readStart;
		// checks Latest start Time in Instances
		readStart = curStart;

		int interval = resources.getInterval();
		long finishTime = curInst.getFinishTime();
		long startTime = curInst.getStartTime();
		double curCost = Math.ceil((double) (finishTime - startTime) / (double) interval) * curInst.getType().getCost();
		curStart = GetMyFileTransferTime(curStart, curNode, curInst);
//		if (utility.Utility.readConsideration == true) {
//			curStart = GetMyFileTransferTime(curStart, curNode, curInst);
//		} else {
//			for (Link parent : curNode.getParents()) {
//				WorkflowNode parentNode = graph.getNodes().get(parent.getId());
//
//				start = parentNode.getEFT();
//				if (parentNode.getSelectedResource() != curInst.getId())
//					start += Math.round((float) parent.getDataSize() / bandwidth);
//				if (start > curStart)
//					curStart = start;
//			}
//		}
		long curRuntime = Math.round((float) curNode.getInstructionSize() / curInst.getType().getMIPS());
		curRuntime = (long) utility.configuration.GetUncertaionExecutionTime(curRuntime);

		curFinish = curStart + curRuntime;
		float Cost = (float) (Math.ceil((double) (curFinish - curInst.getStartTime()) / (double) interval)
				* curInst.getType().getCost() - curCost);

		//

		if (curInst.getFinishTime() == 0) {
			// curInst.setStartTime(curStart);
			// sets start of the instances to the time that it reads files from storage or
			// parent of a node;
			// curInst.setStartTime(readStart);
			curStart = configuration.getProvisioningDelay();
			curInst.setStartTime(curStart - configuration.getProvisioningDelay());

			curInst.setFirstTask(curNode.getId());

		}

		curNode.setAST((int) curStart);
		curNode.setAFT((int) curFinish);
		curNode.setEST((int) curStart);
		curNode.setEFT((int) curFinish);
		curNode.setSelectedResource(curInst.getId());
		curNode.setStartReading((int) readStart);
		curNode.setScheduled();

		curInst.addExe(curNode.getId(), (int) curStart, curFinish, readStart, Cost);
		if (curInst.getFinishTime() < curFinish) {
			curInst.setFinishTime(curFinish);
			curInst.setLastTask(curNode.getId());
		}
	}

	// for BDHEFT
	protected float meanCost(WorkflowNode wn) {
		int interval = resources.getInterval();
		float NodeCost = 0;
		long nodeFinish = 0;

		for (int curInst = 0; curInst < instances.getSize(); curInst++) {

			nodeFinish = instances.getInstance(curInst).getFinishTime()
					+ Math.round((float) wn.getInstructionSize() / instances.getInstance(curInst).getType().getMIPS());
			NodeCost += checkInstanceFinish(nodeFinish, instances.getInstance(curInst)).cost;

		}

		// calculated mean Cost of nodes that is not scheduled by TotalCost variable;
		for (int curRes = resources.getSize() - 1; curRes >= 0; curRes--) {

			nodeFinish = Math.round((float) wn.getInstructionSize() / resources.getResource(curRes).getMIPS());
			NodeCost += Math.ceil((double) (nodeFinish - 0) / (double) interval)
					* resources.getResource(curRes).getCost();

		}
		NodeCost = NodeCost / (resources.getSize() + instances.getSize());
		return NodeCost;
	}

	protected float meanRunTime(WorkflowNode curNode) {
		float cost = 0;
		result r;
		float finishTime = 0;

		for (int curRes = 0; curRes < resources.getSize(); curRes++) { // because the cheapest one is // the last
			// Instance inst = new Instance(instances.getSize(),
			// resources.getResource(curRes));
			// r = checkInstanceRun(curNode, inst);
			int readTime = curNode.getReadTime(bandwidth);
			finishTime += readTime
					+ Math.round((float) curNode.getInstructionSize() / resources.getResource(curRes).getMIPS());
		}
		finishTime = finishTime / resources.getSize();
		return finishTime;
	}

	protected result checkInstanceFinish(long curFinish, Instance curInst) {

		long finishTime = curInst.getFinishTime();
		long startTime = curInst.getStartTime();

		int interval = resources.getInterval();
		double curCost = Math.ceil((double) (finishTime - startTime) / (double) interval) * curInst.getType().getCost();
		long curIntervalFinish = startTime
				+ (long) Math.ceil((double) (finishTime - startTime) / (double) interval) * interval;
		long start, curStart = (int) finishTime;

		// if (utility.Utility.readConsideration == true) {
		// curStart = super.GetMyFileTransferTime(curStart, curNode, curInst);
		// } else {
		// for (Link parent : curNode.getParents()) {
		// WorkflowNode parentNode = graph.getNodes().get(parent.getId());
		// start = parentNode.getEFT();
		// if (parentNode.getSelectedResource() != curInst.getId())
		// start += Math.round((float) parent.getDataSize() / bandwidth);
		// if (start > curStart)
		// curStart = start;
		// }
		// }

		if (finishTime == 0)
			startTime = curStart;

		result r = new result();
		if (curFinish < startTime)
			return r;
		// curFinish = curStart + Math.round((float) curNode.getInstructionSize() /
		// curInst.getType().getMIPS());
		r.finishTime = (int) curFinish;

		r.cost = (float) (Math.ceil((double) (curFinish - startTime) / (double) interval) * curInst.getType().getCost()
				- curCost);
		return r;
	}

	public WorkflowNode GetCriticalParent(WorkflowNode curNode, Instance in) {

		WorkflowNode CP = null;

		long curStart;
		int start = 0;
		int max = -1;
		List<File> nodeFiles = new ArrayList<File>();
		nodeFiles.addAll(curNode.getFileSet());
		List<File> ParentsFiles = new ArrayList<File>();
		for (Link parent : curNode.getParents()) {
			WorkflowNode parentNode = graph.getNodes().get(parent.getId());
			List<File> ParentFiles = filesFromParentNode(curNode, parentNode);
			ParentsFiles.addAll(ParentFiles);
			// calculate file transfer Time from nodes parents if them does not exist in the
			// instance;
			int parentData = GetFileTransferTimeInInstance(ParentFiles, in);
			start = parentData + parentNode.getEFT();
			if (max < start) {
				max = start;
				CP = parentNode;
			}
		}

		return CP;

	}

	public long GetMyStartTimeNoParentFile(long curStart, WorkflowNode curNode, Instance in) {
		int start = 0;
		int max = -1;
		List<File> nodeFiles = new ArrayList<File>();
		nodeFiles.addAll(curNode.getFileSet());
		List<File> ParentsFiles = new ArrayList<File>();
		for (Link parent : curNode.getParents()) {
			WorkflowNode parentNode = graph.getNodes().get(parent.getId());
			List<File> ParentFiles = filesFromParentNode(curNode, parentNode);
			ParentsFiles.addAll(ParentFiles);
			// calculate file transfer Time from nodes parents if them does not exist in the
			// instance;
			// int parentData = CalculateFileTransferTimeInInstance(ParentFiles, in);
			start = 0 + parentNode.getEFT();
			if (max < start)
				max = start;
		}

		// calculates SelfReadDataTransferTime;
		nodeFiles.removeAll(ParentsFiles);
		nodeFiles.removeAll(curNode.getOutputFileSet());
		long SelfInputFileSize = 0;
		// calculate file transfer Time from nodes parents if them does not exist in the
		// instance;
		int SelfData = GetFileTransferTimeInInstance(nodeFiles, in);
		// SelfData=0;
		// end

		if ((SelfData) > max)
			max = SelfData;

		if (max > curStart)
			curStart = max;
		return curStart;
	}

	public List<File> getSelfReadFileSet(WorkflowNode curNode) {
		List<File> nodeFiles = new ArrayList<File>();
		nodeFiles.addAll(curNode.getFileSet());
		List<File> ParentsFiles = new ArrayList<File>();
		for (Link parent : curNode.getParents()) {
			WorkflowNode parentNode = graph.getNodes().get(parent.getId());
			List<File> ParentFiles = filesFromParentNode(curNode, parentNode);
			ParentsFiles.addAll(ParentFiles);
		}

		// calculates SelfReadDataTransferTime;
		nodeFiles.removeAll(ParentsFiles);
		nodeFiles.removeAll(curNode.getOutputFileSet());
		return nodeFiles;
	}

	public long GetMyStartTimeForPath(long curStart, int NodeIndexOnPath, Instance in, List<Broker.WorkflowNode> path,
			int[] newESTs) {
		int start = 0;
		int max = -1;
		Broker.WorkflowNode curNode = path.get(NodeIndexOnPath);
		List<File> nodeFiles = new ArrayList<File>();
		nodeFiles.addAll(curNode.getFileSet());
		List<File> ParentsFiles = new ArrayList<File>();
		for (Link parent : curNode.getParents()) {
			WorkflowNode parentNode = graph.getNodes().get(parent.getId());
			List<File> ParentFiles = filesFromParentNode(curNode, parentNode);
			ParentsFiles.addAll(ParentFiles);
			// calculate file transfer Time from nodes parents if them does not exist in the
			// instance;
			// int parentData = CalculateFileTransferTimeInInstance(ParentFiles, in);
			start = 0 + parentNode.getEFT();
			if (max < start)
				max = start;
		}

		// calculates SelfReadDataTransferTime;
		nodeFiles.removeAll(ParentsFiles);
		nodeFiles.removeAll(curNode.getOutputFileSet());
		long SelfInputFileSize = 0;
		// calculate file transfer Time from nodes parents if them does not exist in the
		// instance;
		int SelfData = GetFileTransferTimeInInstance(nodeFiles, in);
		// SelfData=0;
		// end

		if ((SelfData) > max)
			max = SelfData;

		if (max > curStart)
			curStart = max;
		return curStart;
	}

	// for BDAS AND DBDAS algorithm
	protected Instance getFastestInstance(WorkflowNode curNode) {
		result r;
		int bestInst = -1;
		float bestCost = Float.MAX_VALUE;
		long bestFinish = Long.MAX_VALUE;
		int curInst;
		for (Link parent : curNode.getParents()) {
			if (parent.id.contains("start") || parent.id.contains("end")) {
				break;
			}
			if (curNode.graphid >= 0) {
				graph = graphs[curNode.graphid];
			}
			WorkflowNode parentNode = graph.getNodes().get(parent.getId());
			curInst = parentNode.getSelectedResource();
			if (instances.getSize() > 0) {
				r = checkInstance(curNode, instances.getInstance(curInst));
				if (r.finishTime < bestFinish) {
					bestCost = r.cost;
					bestFinish = r.finishTime;
					bestInst = curInst;
				}
			}
		}

		Instance inst1 = new Instance(instances.getSize(), resources.getMaxResource());
		r = checkInstance(curNode, inst1);
		if (r.finishTime < bestFinish) {
			bestCost = r.cost;
			bestFinish = r.finishTime;
			bestInst = 10000 + resources.getMaxId();
		}

		if (bestInst < 10000)
			return instances.getInstance(bestInst);
		// setInstance(curNode, instances.getInstance(bestInst));
		else {
			bestInst -= 10000;
			Instance inst = new Instance(instances.getSize(), resources.getResource(bestInst));
			return inst;
			// instances.addInstance(inst);
			// setInstance(curNode, inst);
		}

	}

	protected int getFastestInstanceIndex(WorkflowNode curNode) {
		result r;
		int bestInst = -1;
		float bestCost = Float.MAX_VALUE;
		long bestFinish = Long.MAX_VALUE;

		int curInst;
		for (Link parent : curNode.getParents()) {
			if (parent.id.contains("start") || parent.id.contains("end")) {
				break;
			}
			if (curNode.graphid >= 0) {
				graph = graphs[curNode.graphid];
			}
			WorkflowNode parentNode = graph.getNodes().get(parent.getId());
			curInst = parentNode.getSelectedResource();

			r = checkInstance(curNode, instances.getInstance(curInst));
			if (r.finishTime < bestFinish) {
				bestCost = r.cost;
				bestFinish = r.finishTime;
				bestInst = curInst;
			}
		}

		
		int curRes = resources.getMaxId();
		Instance inst = new Instance(instances.getSize(), resources.getResource(curRes));
		r = checkInstance(curNode, inst);
		if (r.finishTime < bestFinish) {
			bestCost = r.cost;
			bestFinish = r.finishTime;
			bestInst = 10000 + curRes;
		}
		
		return bestInst;

	}
	protected int getFastestInstanceIndex2(WorkflowNode curNode) {
		result r;
		int bestInst = -1;
		float bestCost = Float.MAX_VALUE;
		long bestFinish = Long.MAX_VALUE;

		int curInst;
		for (Link parent : curNode.getParents()) {
			if (parent.id.contains("start") || parent.id.contains("end")) {
				break;
			}
			if (curNode.graphid >= 0) {
				graph = graphs[curNode.graphid];
			}
			WorkflowNode parentNode = graph.getNodes().get(parent.getId());
			curInst = parentNode.getSelectedResource();

			r = checkInstance(curNode, instances.getInstance(curInst));
			if (r.finishTime < bestFinish) {
				bestCost = r.cost;
				bestFinish = r.finishTime;
				bestInst = curInst;
			}
		}

		
		int curRes = resources.getMaxId();
		Instance inst = new Instance(instances.getSize(), resources.getResource(curRes));
		r = checkInstance(curNode, inst);
		if (r.finishTime < bestFinish) {
			bestCost = r.cost;
			bestFinish = r.finishTime;
			bestInst = 50000 + curRes;
		}
		
		return bestInst;

	}
	protected Instance getEcoEnergyInstance(WorkflowNode curNode) {
		result r;
		int bestInst = -1;
		float bestCost = Float.MAX_VALUE;
		long bestFinish = Long.MAX_VALUE;
		long minEnery = Long.MAX_VALUE;
		for (int curInst = 0; curInst < instances.getSize(); curInst++) {
			r = checkInstanceE(curNode, instances.getInstance(curInst));
			if (r.energy < minEnery) {
				bestCost = r.cost;
				bestFinish = r.finishTime;
				bestInst = curInst;
				minEnery = r.energy;
			}
		}
		for (int curRes = resources.getSize() - 1; curRes >= 0; curRes--) { // because the cheapest one is the last
			Instance inst = new Instance(instances.getSize(), resources.getResource(curRes));
			r = checkInstanceE(curNode, inst);
			if (r.energy < minEnery) {
				bestCost = r.cost;
				bestFinish = r.finishTime;
				bestInst = 10000 + curRes;
				minEnery = r.energy;
			}
		}
		if (bestInst < 10000)
			return instances.getInstance(bestInst);
		// setInstance(curNode, instances.getInstance(bestInst));
		else {
			bestInst -= 10000;
			Instance inst = new Instance(instances.getSize(), resources.getResource(bestInst));
			return inst;
			// instances.addInstance(inst);
			// setInstance(curNode, inst);
		}
	}

	protected result getMinEnergy(WorkflowNode curNode) {
		result r;
		result bestr = new result();
		long minEnery = Long.MAX_VALUE;

		for (int curRes = resources.getSize() - 1; curRes >= 0; curRes--) { // because the cheapest one is the last
			Instance inst = new Instance(instances.getSize(), resources.getResource(curRes));

			r = checkInstanceEFrequency(curNode, inst, inst.getType().getMinFrequency());
			if (r.energy < minEnery) {
				bestr = r;
				minEnery = r.energy;
			} else {
				break;
			}

		}
		return bestr;

	}

	protected result getMaxEnergy(WorkflowNode curNode) {
		result r;
		result bestr = new result();
		long maxEnery = -1;
		float maxFreq;
		int maxEnergyIndex;
		for (int curRes = 0; curRes < resources.getSize(); curRes++) { // because the cheapest one is the last
			Instance inst = new Instance(instances.getSize(), resources.getResource(curRes));

			r = checkInstanceEFrequency(curNode, inst, inst.getType().getMaxFrequencyOr1());

			if (r.energy > maxEnery) {
				bestr = r;
				maxEnery = r.energy;
				maxEnergyIndex = curRes;
			} else {
				break;
			}

		}
		return bestr;

	}

	protected Instance getMaxEnergyInstance(WorkflowNode curNode) {
		result r;
		int bestInst = -1;

		long maxEnergy = -1;

		for (int curRes = resources.getSize() - 1; curRes >= 0; curRes--) { // because the cheapest one is the last
			Instance inst = new Instance(instances.getSize(), resources.getResource(curRes));
			r = checkInstanceE(curNode, inst);
			if (r.energy > maxEnergy) {

				bestInst = 10000 + curRes;
				maxEnergy = r.energy;
			}
		}
		if (bestInst < 10000)
			return instances.getInstance(bestInst);

		else {
			bestInst -= 10000;
			Instance inst = new Instance(instances.getSize(), resources.getResource(bestInst));
			return inst;

		}
	}

	// for BDAS AND DBDAS algorithm
	protected Instance getCheapInstance(WorkflowNode curNode, float deadline) {
		result r;
		int bestInst = -1;
		float bestCost = Float.MAX_VALUE;
		long bestFinish = 0;
		for (int curInst = 0; curInst < instances.getSize(); curInst++) {
			r = checkInstance(curNode, instances.getInstance(curInst));
			if (r.cost < bestCost && deadline >= r.finishTime) {
				bestCost = r.cost;
				bestFinish = r.finishTime;
				bestInst = curInst;
			}
		}
		for (int curRes = resources.getSize() - 1; curRes >= 0; curRes--) { // because the cheapest one is the last
			Instance inst = new Instance(instances.getSize(), resources.getResource(curRes));
			r = checkInstance(curNode, inst);
			if (r.cost < bestCost && deadline >= r.finishTime) {
				bestCost = r.cost;
				bestFinish = r.finishTime;
				bestInst = 10000 + curRes;
			}
		}
		if (bestInst == -1) {
			// if it could not find any resources to satisfy the deadline
			bestFinish = Long.MAX_VALUE;
			Instance inst = new Instance(instances.getSize(), resources.getResource(resources.getSize() - 1));
			r = checkInstance(curNode, inst);
			if (r.finishTime < bestFinish) {
				bestFinish = r.finishTime;
				bestInst = 10000 + resources.getSize() - 1;
			}
		}
		if (bestInst < 10000)
			return instances.getInstance(bestInst);
		// setInstance(curNode, instances.getInstance(bestInst));
		else {
			bestInst -= 10000;
			Instance inst = new Instance(instances.getSize(), resources.getResource(bestInst));
			return inst;
			// instances.addInstance(inst);
			// setInstance(curNode, inst);
		}

	}

	protected Instance getCheapInstanceNoDeadline(WorkflowNode curNode) {
		result r;
		int bestInst = -1;
		float bestCost = Float.MAX_VALUE;
		long bestFinish = 0;
		for (int curInst = 0; curInst < instances.getSize(); curInst++) {
			r = checkInstance(curNode, instances.getInstance(curInst));
			if (r.cost < bestCost) {
				bestCost = r.cost;
				bestFinish = r.finishTime;
				bestInst = curInst;
			}
		}
		for (int curRes = resources.getSize() - 1; curRes >= 0; curRes--) { // because the cheapest one is the last
			Instance inst = new Instance(instances.getSize(), resources.getResource(curRes));
			r = checkInstance(curNode, inst);
			if (r.cost < bestCost) {
				bestCost = r.cost;
				bestFinish = r.finishTime;
				bestInst = 10000 + curRes;
			} else {
				break;
			}
		}
		if (bestInst == -1) {
			// if it could not find any resources to satisfy the deadline
			bestFinish = Long.MAX_VALUE;
			Instance inst = new Instance(instances.getSize(), resources.getResource(resources.getSize() - 1));
			r = checkInstance(curNode, inst);
			if (r.finishTime < bestFinish) {
				bestFinish = r.finishTime;
				bestInst = 10000 + resources.getSize() - 1;
			}
		}
		if (bestInst < 10000)
			return instances.getInstance(bestInst);
		// setInstance(curNode, instances.getInstance(bestInst));
		else {
			bestInst -= 10000;
			Instance inst = new Instance(instances.getSize(), resources.getResource(bestInst));
			return inst;
			// instances.addInstance(inst);
			// setInstance(curNode, inst);
		}

	}

	protected Instance getExpensiveInstance(WorkflowNode curNode) {
		result r;
		int bestInst = -1;
		float bestCost = -1;
		long bestFinish = 0;

		for (int curRes = 0; curRes < resources.getSize(); curRes++) { // because the cheapest one is the last
			Instance inst = new Instance(instances.getSize(), resources.getResource(curRes));
			r = checkInstance(curNode, inst);
			if (r.cost > bestCost) {
				bestCost = r.cost;
				bestFinish = r.finishTime;
				bestInst = curRes;
			}
		}

		Instance inst = new Instance(instances.getSize(), resources.getResource(bestInst));
		return inst;

	}

	public float calcReliability(int exTimeWith100Percent, int exTimewithFreq, float freq, Instance curInst) {
		double y = curInst.getType().getFaultRate();
		float compute = 0;
		if (exTimewithFreq > 0)
			compute = (int) Math.round(exTimewithFreq * freq);
		else
			compute = exTimeWith100Percent;
		// freq=freq+1;

		// compute should be the with the frequency of 100%
		float freqOp = (float) Math.pow(10, (fq * (1 - freq)) / (1 - curInst.getType().getMinFrequency()));
		float reliability = (float) Math.exp((-1 * y * freqOp) * (compute / freq));

//			if (reliability<0.1) 
//				System.out.print("\n fuck reliability");
		return reliability;
	}

	protected void setInstanceFull(WorkflowNode curNode, Instance curInst) {
		long start, curStart = curInst.getFinishTime(), curFinish, readStart;
		// checks Latest start Time in Instances
		readStart = curStart;

		int interval = resources.getInterval();
		long finishTime = curInst.getFinishTime();
		long startTime = curInst.getStartTime();
		double curCost = Math.ceil((double) (finishTime - startTime) / (double) interval) * curInst.getType().getCost();
		curStart = (int) GetMyFileTransferTime(curStart, curNode, curInst);

		if (curInst.getFinishTime() == 0) {
			curInst.setStartTime(curStart);
			curStart += configuration.getProvisioningDelay();
			curInst.setFirstTask(curNode.getId());
		}

		curFinish = curStart + Math.round((float) curNode.getInstructionSize() / curInst.getType().getMIPS());

		float Cost = (float) (Math.ceil((double) (curFinish - curInst.getStartTime()) / (double) interval)
				* curInst.getType().getCost() - curCost);

		curNode.setAST((int) curStart);
		curNode.setAFT((int) curFinish);
		curNode.setEST((int) curStart);
		curNode.setEFT((int) curFinish);
		curNode.setSelectedResource(curInst.getId());
		curNode.setStartReading((int) readStart);
		curNode.setScheduled();

		int computation = (int) (curFinish - curStart);
		float freqMax = curInst.getType().getMaxFrequencyOr1();
		int energy = calcEnergy(computation, freqMax, curInst); // Math

		float reliability = calcReliability(0, (int) computation, freqMax, curInst);

		curInst.addExe(curNode.getId(), curStart, curFinish, readStart, Cost, energy, reliability, freqMax, 1);

		if (curInst.getFinishTime() < curFinish) {
			curInst.setFinishTime(curFinish);
			curInst.setLastTask(curNode.getId());
		}

	}

	public List<File> getFileNotExistedInParent(WorkflowNode curNode, WorkflowNode parent) {
		List<File> newFiles = new ArrayList<File>();

		List<File> f1 = curNode.getFileSet();
		List<File> f2 = parent.getFileSet();
		if (f1 == null)
			return null;
		if (f2 == null)
			return f1;

		for (File file : f1) {
			if (!f2.contains(file))
				newFiles.add(file);
		}
		return newFiles;
	}

	public List<File> filesFromParentNode(WorkflowNode curNode, WorkflowNode parent) {
		List<File> newFiles = new ArrayList<File>();

		List<File> f1 = curNode.getFileSet();
		List<File> f2 = parent.getFileSet();
		if (f1 == null || f2 == null)
			return newFiles;

		for (File f : f1) {
			for (File ff : f2) {
				if (f.fileName.contains(ff.fileName)) {
					newFiles.add(f);
					continue;
				}
			}
		}
		return newFiles;
	}

	public void InsertFilesToInstance(WorkflowNode wn, Instance in) {
		List<File> Filesets = wn.getFileSet();
		in.addFiles(Filesets);
		// long transferSize =in.getNotExistedFileSizes(Filesets);
		// int tTime= (int) Math.round((double)transferSize/bandwidth);
		// return tTime;
	}

	protected void computeLSTandLFT(int deadline) {
		Queue<String> candidateNodes = new LinkedList<String>();
		Map<String, WorkflowNode> nodes = graph.getNodes();
		WorkflowNode curNode, parentNode, childNode;
		Instance curInst = new Instance(0, resources.getMyMaxResource());
		curNode = nodes.get(graph.getEndId());
		curNode.setLFT(deadline);
		curNode.setLST(deadline);
		curNode.setScheduled();
		for (Link parent : curNode.getParents())
			candidateNodes.add(parent.getId());

		while (!candidateNodes.isEmpty()) {
			int thisTime, minTime;
			curNode = nodes.get(candidateNodes.remove());
			minTime = Integer.MAX_VALUE;
			for (Link child : curNode.getChildren()) {
				childNode = nodes.get(child.getId());
				int startTime = (int) GetMyFileTransferTime(0, curNode, curInst);
				thisTime = childNode.getLFT() - childNode.getRunTime();
				thisTime -= Math.round((float) child.getDataSize() / bandwidth);
				if (thisTime < minTime)
					minTime = thisTime;
			}
			curNode.setLFT(minTime);
			curNode.setLST(minTime - curNode.getRunTime());
			curNode.setScheduled();

			for (Link parent : curNode.getParents()) {
				boolean isCandidate = true;
				parentNode = nodes.get(parent.getId());
				for (Link child : parentNode.getChildren())
					if (!nodes.get(child.getId()).isScheduled())
						isCandidate = false;
				if (isCandidate)
					candidateNodes.add(parent.getId());
			}
		}

		for (WorkflowNode node : nodes.values())
			node.setUnscheduled();
	}

	protected float calcFluctuation(double runningTime) {
		return (float) (fluctpercent * runningTime * utility.configuration.ExecutionDeviation);
	}

	protected void computeLSTandLFT(WorkflowGraph graph, int deadline) {
		Queue<String> candidateNodes = new LinkedList<String>();
		Map<String, WorkflowNode> nodes = graph.getNodes();
		WorkflowNode curNode, parentNode, childNode;
		Instance curInst = new Instance(0, resources.getMyMaxResource());
		curNode = nodes.get(graph.getEndId());
		curNode.setLFT(deadline);
		curNode.setLST(deadline);
		curNode.setScheduled();
		for (Link parent : curNode.getParents())
			candidateNodes.add(parent.getId());

		while (!candidateNodes.isEmpty()) {
			int thisTime, minTime;
			curNode = nodes.get(candidateNodes.remove());
			minTime = Integer.MAX_VALUE;
			for (Link child : curNode.getChildren()) {
				childNode = nodes.get(child.getId());
				int startTime = (int) GetMyFileTransferTime(0, curNode, curInst);
				thisTime = childNode.getLFT() - childNode.getRunTime();
				thisTime -= Math.round((float) child.getDataSize() / bandwidth);
				if (thisTime < minTime)
					minTime = thisTime;
			}
			curNode.setLFT(minTime);
			curNode.setDeadline(minTime);
			curNode.setLST(minTime - curNode.getRunTime());
			curNode.setScheduled();

			for (Link parent : curNode.getParents()) {
				boolean isCandidate = true;
				parentNode = nodes.get(parent.getId());
				for (Link child : parentNode.getChildren())
					if (!nodes.get(child.getId()).isScheduled())
						isCandidate = false;
				if (isCandidate)
					candidateNodes.add(parent.getId());
			}
		}

		for (WorkflowNode node : nodes.values())
			node.setUnscheduled();
	}

	protected void computeLSTandLFT_Fluct(WorkflowGraph graph, int deadline) {
		Queue<String> candidateNodes = new LinkedList<String>();
		Map<String, WorkflowNode> nodes = graph.getNodes();
		WorkflowNode curNode, parentNode, childNode;
		Instance curInst = new Instance(0, resources.getMyMaxResource());
		curNode = nodes.get(graph.getEndId());
		curNode.setLFT(deadline);
		curNode.setLST(deadline);
		curNode.setScheduled();
		int fluct;
		for (Link parent : curNode.getParents())
			candidateNodes.add(parent.getId());

		while (!candidateNodes.isEmpty()) {
			int thisTime, minTime;
			curNode = nodes.get(candidateNodes.remove());
			minTime = Integer.MAX_VALUE;

			for (Link child : curNode.getChildren()) {
				childNode = nodes.get(child.getId());
				fluct = Math.round(calcFluctuation(childNode.getRunTime()));

				int startTime = (int) GetMyFileTransferTime(0, curNode, curInst);
				thisTime = childNode.getLFT() - childNode.getRunTime() + fluct;
				thisTime -= Math.round((float) child.getDataSize() / bandwidth);
				if (thisTime < minTime)
					minTime = thisTime;
			}

			fluct = Math.round(calcFluctuation(curNode.getRunTime()));
			curNode.setLFT(minTime);
			curNode.setLST(minTime - curNode.getRunTime() + fluct);
			curNode.setScheduled();

			for (Link parent : curNode.getParents()) {
				boolean isCandidate = true;
				parentNode = nodes.get(parent.getId());
				for (Link child : parentNode.getChildren())
					if (!nodes.get(child.getId()).isScheduled())
						isCandidate = false;
				if (isCandidate)
					candidateNodes.add(parent.getId());
			}
		}

		for (WorkflowNode node : nodes.values())
			node.setUnscheduled();
	}

	protected void computeMyLSTandLFT(int deadline) {
		Queue<String> candidateNodes = new LinkedList<String>();
		Map<String, WorkflowNode> nodes = graph.getNodes();
		WorkflowNode curNode, parentNode, childNode;
		Instance curInst = new Instance(0, resources.getMyMaxResource());
		curNode = nodes.get(graph.getEndId());
		curNode.setLFT(deadline);
		curNode.setLST(deadline);
		curNode.setScheduled();
		for (Link parent : curNode.getParents())
			candidateNodes.add(parent.getId());

		while (!candidateNodes.isEmpty()) {
			int thisTime, minTime;
			curNode = nodes.get(candidateNodes.remove());
			minTime = Integer.MAX_VALUE;
			for (Link child : curNode.getChildren()) {
				childNode = nodes.get(child.getId());
				int startTime = (int) GetMyFileTransferTime(0, curNode, curInst);
				thisTime = childNode.getLFT() - childNode.getRunTime();
				// thisTime -= Math.round((float) child.getDataSize() / bandwidth);
				thisTime -= startTime;
				if (thisTime < minTime)
					minTime = thisTime;
			}
			curNode.setLFT(minTime);
			curNode.setLST(minTime - curNode.getRunTime());
			curNode.setScheduled();

			for (Link parent : curNode.getParents()) {
				boolean isCandidate = true;
				parentNode = nodes.get(parent.getId());
				for (Link child : parentNode.getChildren())
					if (!nodes.get(child.getId()).isScheduled())
						isCandidate = false;
				if (isCandidate)
					candidateNodes.add(parent.getId());
			}
		}

		for (WorkflowNode node : nodes.values())
			node.setUnscheduled();
	}

	protected int getCommunicationTime(WorkflowNode curNode) {
		int curStart = 0, start = 0, longEFTparent = 0;
		for (Link parent : curNode.getParents()) {
			WorkflowNode parentNode = graph.getNodes().get(parent.getId());
			start = parentNode.getEFT();
			start += Math.round((float) parent.getDataSize() / bandwidth);
			if (start > curStart) {
				curStart = start;
				longEFTparent = parentNode.getEFT();
			}
		}
		return curStart - longEFTparent;
	}

	protected int getCommunicationTime(WorkflowNode curNode, Instance curInst) {
		int curStart = 0, start = 0;
		if (utility.Utility.readConsideration == true) {
			curStart = (int) GetMyFileTransferTime(curStart, curNode, curInst);
		} else {
			for (Link parent : curNode.getParents()) {
				WorkflowNode parentNode = graph.getNodes().get(parent.getId());

				start = parentNode.getEFT();
				if (parentNode.getSelectedResource() != curInst.getId())
					start += Math.round((float) parent.getDataSize() / bandwidth);
				if (start > curStart)
					curStart = start;
			}
		}
		return curStart;
	}

	protected void computeESTandEFT(int startTime) {
		Queue<String> candidateNodes = new LinkedList<String>();
		Map<String, WorkflowNode> nodes = graph.getNodes();
		WorkflowNode curNode, parentNode, childNode;

		curNode = nodes.get(graph.getStartId());
		curNode.setEST(startTime);
		curNode.setEFT(startTime);
		curNode.setScheduled();
		for (Link child : curNode.getChildren())
			candidateNodes.add(child.getId());

		while (!candidateNodes.isEmpty()) {
			int thisTime, maxTime;
			curNode = nodes.get(candidateNodes.remove());
			maxTime = -1;
			float datatransfertime = 0;
			maxTime = (int) computeDataTransferTime(curNode);
//			for (Link parent : curNode.getParents()) {
//				parentNode = nodes.get(parent.getId());
//				datatransfertime = Math.round((float) parent.getDataSize() / bandwidth);
//
//				thisTime = (int) (parentNode.getEFT() + datatransfertime);
//				if (thisTime > maxTime)
//					maxTime = thisTime;
//			}
			curNode.setEST(maxTime);
			curNode.setEFT(maxTime + curNode.getRunTime());
			curNode.setScheduled();

			for (Link child : curNode.getChildren()) {
				boolean isCandidate = true;
				childNode = nodes.get(child.getId());
				for (Link parent : childNode.getParents())
					if (!nodes.get(parent.getId()).isScheduled())
						isCandidate = false;
				if (isCandidate)
					candidateNodes.add(child.getId());
			}
		}
		for (WorkflowNode node : nodes.values())
			node.setUnscheduled();
	}

	protected long computeDataTransferTime(WorkflowNode curNode) {
		Map<String, WorkflowNode> nodes = graph.getNodes();
		long thisTime, maxTime;
		maxTime = -1;
		float datatransfertime = 0, selfTransfer = 0;
		WorkflowNode parentNode;

		for (Link parent : curNode.getParents()) {
			parentNode = nodes.get(parent.getId());
			datatransfertime = Math.round((float) parent.getDataSize() / bandwidth);
//		selfTransfer = Math.round((float) curNode.getReadFileSize() / bandwidth);
			if (selfTransfer > datatransfertime)
				datatransfertime = selfTransfer;

			thisTime = (int) (parentNode.getEFT() + datatransfertime);
			if (thisTime > maxTime)
				maxTime = thisTime;
		}
		return maxTime;
	}

	protected long computeDataTransferTime(WorkflowNode curNode, WorkflowGraph graph) {
		Map<String, WorkflowNode> nodes = graph.getNodes();
		long thisTime, maxTime;
		maxTime = -1;
		float datatransfertime = 0, selfTransfer = 0;
		WorkflowNode parentNode;

		for (Link parent : curNode.getParents()) {
			parentNode = nodes.get(parent.getId());
			datatransfertime = Math.round((float) parent.getDataSize() / bandwidth);
//		selfTransfer = Math.round((float) curNode.getReadFileSize() / bandwidth);
			if (selfTransfer > datatransfertime)
				datatransfertime = selfTransfer;

			thisTime = (int) (parentNode.getEFT() + datatransfertime);
			if (thisTime > maxTime)
				maxTime = thisTime;
		}
		return maxTime;
	}

	protected void computeESTandEFT2(int startTime) {
		Queue<String> candidateNodes = new LinkedList<String>();
		Map<String, WorkflowNode> nodes = graph.getNodes();
		WorkflowNode curNode, parentNode, childNode;

		curNode = nodes.get(graph.getStartId());
		curNode.setEST(startTime);
		curNode.setEFT(startTime);
		curNode.setScheduled();
		for (Link child : curNode.getChildren())
			candidateNodes.add(child.getId());

		while (!candidateNodes.isEmpty()) {
			int thisTime, maxTime;
			curNode = nodes.get(candidateNodes.remove());
			maxTime = -1;
			maxTime = (int) computeDataTransferTime(curNode);

//			float datatransfertime = 0, selfTransfer = 0;
//			for (Link parent : curNode.getParents()) {
//				parentNode = nodes.get(parent.getId());
//				datatransfertime = Math.round((float) parent.getDataSize() / bandwidth);
//				selfTransfer = Math.round((float) curNode.getReadFileSize() / bandwidth);
//				if (selfTransfer > datatransfertime)
//					datatransfertime = selfTransfer;
//
//				thisTime = (int) (parentNode.getEFT() + datatransfertime);
//				if (thisTime > maxTime)
//					maxTime = thisTime;
//			}
			curNode.setEST(maxTime);
			curNode.setEFT(maxTime + curNode.getRunTime());
			curNode.setScheduled();

			for (Link child : curNode.getChildren()) {
				boolean isCandidate = true;
				childNode = nodes.get(child.getId());
				for (Link parent : childNode.getParents())
					if (!nodes.get(parent.getId()).isScheduled())
						isCandidate = false;
				if (isCandidate)
					candidateNodes.add(child.getId());
			}
		}
		for (WorkflowNode node : nodes.values())
			node.setUnscheduled();
	}

	protected void computeESTandEFT2(WorkflowGraph graph, int startTime) {
		Queue<String> candidateNodes = new LinkedList<String>();
		Map<String, WorkflowNode> nodes = graph.getNodes();
		WorkflowNode curNode, parentNode, childNode;

		curNode = nodes.get(graph.getStartId());
		curNode.setEST(startTime);
		curNode.setEFT(startTime);
		curNode.setScheduled();
		for (Link child : curNode.getChildren())
			candidateNodes.add(child.getId());

		while (!candidateNodes.isEmpty()) {
			int thisTime, maxTime;
			curNode = nodes.get(candidateNodes.remove());
			maxTime = -1;
			maxTime = (int) computeDataTransferTime(curNode, graph);

//			float datatransfertime = 0, selfTransfer = 0;
//			for (Link parent : curNode.getParents()) {
//				parentNode = nodes.get(parent.getId());
//				datatransfertime = Math.round((float) parent.getDataSize() / bandwidth);
//				selfTransfer = Math.round((float) curNode.getReadFileSize() / bandwidth);
//				if (selfTransfer > datatransfertime)
//					datatransfertime = selfTransfer;
//
//				thisTime = (int) (parentNode.getEFT() + datatransfertime);
//				if (thisTime > maxTime)
//					maxTime = thisTime;
//			}
			curNode.setEST(maxTime);
			curNode.setEFT(maxTime + curNode.getRunTime());
			curNode.setScheduled();

			for (Link child : curNode.getChildren()) {
				boolean isCandidate = true;
				childNode = nodes.get(child.getId());
				for (Link parent : childNode.getParents())
					if (!nodes.get(parent.getId()).isScheduled())
						isCandidate = false;
				if (isCandidate)
					candidateNodes.add(child.getId());
			}
		}
		for (WorkflowNode node : nodes.values())
			node.setUnscheduled();
	}

	protected void computeESTandEFT_fluct(WorkflowGraph graph, int startTime) {
		Queue<String> candidateNodes = new LinkedList<String>();
		Map<String, WorkflowNode> nodes = graph.getNodes();
		WorkflowNode curNode, parentNode, childNode;

		int fluct = 0;

		curNode = nodes.get(graph.getStartId());
		curNode.setEST(startTime);
		curNode.setEFT(startTime);
		curNode.setScheduled();
		for (Link child : curNode.getChildren())
			candidateNodes.add(child.getId());

		while (!candidateNodes.isEmpty()) {
			int thisTime, maxTime;
			curNode = nodes.get(candidateNodes.remove());
			maxTime = -1;
			maxTime = (int) computeDataTransferTime(curNode, graph);

//			float datatransfertime = 0, selfTransfer = 0;
//			for (Link parent : curNode.getParents()) {
//				parentNode = nodes.get(parent.getId());
//				datatransfertime = Math.round((float) parent.getDataSize() / bandwidth);
//				selfTransfer = Math.round((float) curNode.getReadFileSize() / bandwidth);
//				if (selfTransfer > datatransfertime)
//					datatransfertime = selfTransfer;
//
//				thisTime = (int) (parentNode.getEFT() + datatransfertime);
//				if (thisTime > maxTime)
//					maxTime = thisTime;
//			}
			curNode.setEST(maxTime);
			fluct = Math.round(calcFluctuation(curNode.getRunTime()));
			curNode.setEFT(maxTime + curNode.getRunTime() + fluct);
			curNode.setScheduled();

			for (Link child : curNode.getChildren()) {
				boolean isCandidate = true;
				childNode = nodes.get(child.getId());
				for (Link parent : childNode.getParents())
					if (!nodes.get(parent.getId()).isScheduled())
						isCandidate = false;
				if (isCandidate)
					candidateNodes.add(child.getId());
			}
		}
		for (WorkflowNode node : nodes.values())
			node.setUnscheduled();
	}

	protected void computeMyESTandEFT(int startTime) {
		Queue<String> candidateNodes = new LinkedList<String>();
		Map<String, WorkflowNode> nodes = graph.getNodes();
		WorkflowNode curNode, parentNode, childNode;
		Instance curInst = new Instance(0, resources.getMyMaxResource());
		curNode = nodes.get(graph.getStartId());
		curNode.setEST(startTime);
		curNode.setEFT(startTime);
		curNode.setScheduled();
		for (Link child : curNode.getChildren())
			candidateNodes.add(child.getId());

		while (!candidateNodes.isEmpty()) {
			int thisTime, maxTime;
			curNode = nodes.get(candidateNodes.remove());
			maxTime = -1;
			float datatransfertime = 0;
			maxTime = (int) GetMyFileTransferTime(0, curNode, curInst);
			// for (Link parent : curNode.getParents()) {
			// parentNode = nodes.get(parent.getId());
			// datatransfertime = Math.round((float) parent.getDataSize() / bandwidth);
			// thisTime = (int) (parentNode.getEFT() + datatransfertime);
			// if (thisTime > maxTime)
			// maxTime = thisTime;
			// }

			curNode.setEST(maxTime);
			curNode.setEFT(maxTime + curNode.getRunTime());
			curNode.setScheduled();

			for (Link child : curNode.getChildren()) {
				boolean isCandidate = true;
				childNode = nodes.get(child.getId());
				for (Link parent : childNode.getParents())
					if (!nodes.get(parent.getId()).isScheduled())
						isCandidate = false;
				if (isCandidate)
					candidateNodes.add(child.getId());
			}
		}
		for (WorkflowNode node : nodes.values())
			node.setUnscheduled();
	}

	protected void computeUpRank() {
		Queue<String> candidateNodes = new LinkedList<String>();
		Map<String, WorkflowNode> nodes = graph.getNodes();
		WorkflowNode curNode, parentNode, childNode;

		curNode = nodes.get(graph.getEndId());
		curNode.setUpRank(0);
		curNode.setScheduled();
		for (Link parent : curNode.getParents())
			candidateNodes.add(parent.getId());

		while (!candidateNodes.isEmpty()) {
			int thisTime, maxTime;
			float maxMIPS;

			curNode = nodes.get(candidateNodes.remove());
			maxTime = -1;
			for (Link child : curNode.getChildren()) {
				childNode = nodes.get(child.getId());
				thisTime = childNode.getUpRank() + Math.round((float) child.getDataSize() / bandwidth);
				if (thisTime > maxTime)
					maxTime = thisTime;
			}
			maxMIPS = resources.getMeanMIPS();
			maxTime += Math.round((float) curNode.getInstructionSize() / maxMIPS);
			curNode.setUpRank(maxTime);
			curNode.setScheduled();

			for (Link parent : curNode.getParents()) {
				boolean isCandidate = true;
				parentNode = nodes.get(parent.getId());
				for (Link child : parentNode.getChildren())
					if (!nodes.get(child.getId()).isScheduled())
						isCandidate = false;
				if (isCandidate)
					candidateNodes.add(parent.getId());
			}
		}

		for (WorkflowNode node : nodes.values())
			node.setUnscheduled();
	}

	protected void clearNodesSchedule() {
		// SETS NODES TO UNSCHEDULED STATES.
		for (WorkflowNode node : graph.getNodes().values())
			node.setUnscheduled();
	}

	public execution getExecutedTask(String id) {

		for (int i = 0; i < instances.getSize(); i++) {
			if (instances.getInstance(i).getExeList() == null) {
				continue;
			}
			for (execution ex : instances.getInstance(i).getExeList()) {
				if (ex.id.contains(id))
					return ex;
			}
		}
		return null;

	}

	public long getExecutedTaskNextFreeSpace(String id) {
		long exSize = 0;
		for (int i = 0; i < instances.getSize(); i++) {
			exSize = instances.getInstance(i).getExeList().size();
			for (int j = 0; j < exSize; j++) {
				if (instances.getInstance(i).getExeList().get(j).id.contains(id)) {
					if (j + 1 < exSize) {
						return instances.getInstance(i).getExeList().get(j + 1).getStart();
					}
				}
			}

		}
		return Integer.MAX_VALUE;

	}

	public Instance getExecutedTaskInstance(String id) {

		for (int i = 0; i < instances.getSize(); i++) {
			for (execution ex : instances.getInstance(i).getExeList()) {
				if (ex.id.contains(id))
					return instances.getInstance(i);
			}
		}
		return null;

	}

	protected void computeDownRank() {
		Queue<String> candidateNodes = new LinkedList<String>();
		Map<String, WorkflowNode> nodes = graph.getNodes();
		WorkflowNode curNode, parentNode, childNode;

		curNode = nodes.get(graph.getStartId());
		curNode.setDownRank((0));
		curNode.setScheduled();
		for (Link child : curNode.getChildren())
			candidateNodes.add(child.getId());

		while (!candidateNodes.isEmpty()) {
			int thisTime, maxTime;
			float maxMIPS;
			curNode = nodes.get(candidateNodes.remove());
			maxTime = -1;
			for (Link parent : curNode.getParents()) {
				parentNode = nodes.get(parent.getId());
				thisTime = parentNode.getDownRank() + Math.round((float) parent.getDataSize() / bandwidth);
				if (thisTime > maxTime)
					maxTime = thisTime;
			}
			maxMIPS = resources.getMeanMIPS();
			maxTime += Math.round((float) curNode.getInstructionSize() / maxMIPS);
			curNode.setDownRank(maxTime);
			curNode.setScheduled();

			for (Link child : curNode.getParents()) {
				boolean isCandidate = true;
				childNode = nodes.get(child.getId());
				for (Link parent : childNode.getParents())
					if (!nodes.get(parent.getId()).isScheduled())
						isCandidate = false;
				if (isCandidate)
					candidateNodes.add(child.getId());
			}
		}

		for (WorkflowNode node : nodes.values())
			node.setUnscheduled();
	}

	protected void computeDownRank(WorkflowGraph graph) {
		Queue<String> candidateNodes = new LinkedList<String>();
		Map<String, WorkflowNode> nodes = graph.getNodes();
		WorkflowNode curNode, parentNode, childNode;

		curNode = nodes.get(graph.getStartId());
		curNode.setDownRank((0));
		curNode.setScheduled();
		for (Link child : curNode.getChildren())
			candidateNodes.add(child.getId());

		while (!candidateNodes.isEmpty()) {
			int thisTime, maxTime;
			float maxMIPS;
			curNode = nodes.get(candidateNodes.remove());
			maxTime = -1;
			for (Link parent : curNode.getParents()) {
				parentNode = nodes.get(parent.getId());
				thisTime = parentNode.getDownRank() + Math.round((float) parent.getDataSize() / bandwidth);
				if (thisTime > maxTime)
					maxTime = thisTime;
			}
			maxMIPS = resources.getMeanMIPS();
			maxTime += Math.round((float) curNode.getInstructionSize() / maxMIPS);
			curNode.setDownRank(maxTime);
			curNode.setScheduled();

			for (Link child : curNode.getParents()) {
				boolean isCandidate = true;
				childNode = nodes.get(child.getId());
				for (Link parent : childNode.getParents())
					if (!nodes.get(parent.getId()).isScheduled())
						isCandidate = false;
				if (isCandidate)
					candidateNodes.add(child.getId());
			}
		}

		for (WorkflowNode node : nodes.values())
			node.setUnscheduled();
	}

	protected void computeSigmaUpRank() {
		Queue<String> candidateNodes = new LinkedList<String>();
		Map<String, WorkflowNode> nodes = graph.getNodes();
		WorkflowNode curNode, parentNode, childNode;

		curNode = nodes.get(graph.getStartId());
		curNode.setUpRank((0));
		curNode.setScheduled();
		for (Link child : curNode.getChildren())
			candidateNodes.add(child.getId());

		while (!candidateNodes.isEmpty()) {
			int thisTime = 0, maxTime;
			float maxMIPS;
			curNode = nodes.get(candidateNodes.remove());
			maxTime = -1;
			for (Link parent : curNode.getParents()) {
				parentNode = nodes.get(parent.getId());
				thisTime += parentNode.getUpRank() + Math.round((float) parent.getDataSize() / bandwidth);

			}
			maxMIPS = resources.getMeanMIPS();
			maxTime += Math.round((float) curNode.getInstructionSize() / maxMIPS);
			curNode.setUpRank(maxTime);
			curNode.setScheduled();

			for (Link child : curNode.getParents()) {
				boolean isCandidate = true;
				childNode = nodes.get(child.getId());
				for (Link parent : childNode.getParents())
					if (!nodes.get(parent.getId()).isScheduled())
						isCandidate = false;
				if (isCandidate)
					candidateNodes.add(child.getId());
			}
		}

		for (WorkflowNode node : nodes.values())
			node.setUnscheduled();
	}

	protected void computeSigmaDownRank() {
		Queue<String> candidateNodes = new LinkedList<String>();
		Map<String, WorkflowNode> nodes = graph.getNodes();
		WorkflowNode curNode, parentNode, childNode;

		curNode = nodes.get(graph.getEndId());
		curNode.setDownRank((0));
		curNode.setScheduled();
		for (Link child : curNode.getChildren())
			candidateNodes.add(child.getId());

		while (!candidateNodes.isEmpty()) {
			int thisTime = 0, maxTime;
			float maxMIPS;
			curNode = nodes.get(candidateNodes.remove());
			maxTime = -1;
			for (Link parent : curNode.getChildren()) {
				parentNode = nodes.get(parent.getId());
				thisTime += parentNode.getDownRank() + Math.round((float) parent.getDataSize() / bandwidth);

			}
			maxMIPS = resources.getMeanMIPS();
			maxTime += Math.round((float) curNode.getInstructionSize() / maxMIPS);
			curNode.setDownRank(maxTime);
			curNode.setScheduled();

			for (Link child : curNode.getParents()) {
				boolean isCandidate = true;
				childNode = nodes.get(child.getId());
				for (Link parent : childNode.getParents())
					if (!nodes.get(parent.getId()).isScheduled())
						isCandidate = false;
				if (isCandidate)
					candidateNodes.add(child.getId());
			}
		}

		for (WorkflowNode node : nodes.values())
			node.setUnscheduled();
	}

	protected void computeMyUpRank() {
		Queue<String> candidateNodes = new LinkedList<String>();
		Map<String, WorkflowNode> nodes = graph.getNodes();
		WorkflowNode curNode, parentNode, childNode;

		curNode = nodes.get(graph.getEndId());
		curNode.setUpRank(0);
		curNode.setScheduled();
		for (Link parent : curNode.getParents())
			candidateNodes.add(parent.getId());

		while (!candidateNodes.isEmpty()) {
			int thisTime, maxTime;
			float maxMIPS;
			curNode = nodes.get(candidateNodes.remove());
			maxTime = -1;
			for (Link child : curNode.getChildren()) {
				childNode = nodes.get(child.getId());
				thisTime = childNode.getUpRank() + Math.round((float) child.getDataSize() / bandwidth);
				if (thisTime > maxTime)
					maxTime = thisTime;
			}
			maxMIPS = resources.getMeanMIPS();
			maxTime += Math.round((float) curNode.getInstructionSize() / maxMIPS)
					+ Math.round((float) curNode.getReadFileSize() / bandwidth);
			curNode.setUpRank(maxTime);
			curNode.setScheduled();

			for (Link parent : curNode.getParents()) {
				boolean isCandidate = true;
				parentNode = nodes.get(parent.getId());
				for (Link child : parentNode.getChildren())
					if (!nodes.get(child.getId()).isScheduled())
						isCandidate = false;
				if (isCandidate)
					candidateNodes.add(parent.getId());
			}
		}

		for (WorkflowNode node : nodes.values())
			node.setUnscheduled();
	}

	protected void computeMeanRunTime() {
		Queue<String> candidateNodes = new LinkedList<String>();
		Map<String, WorkflowNode> nodes = graph.getNodes();
		WorkflowNode curNode, parentNode, childNode;
		int meanRuntime = 0;
		curNode = nodes.get(graph.getStartId());
		curNode.setScheduled();
		for (Link child : curNode.getChildren())
			candidateNodes.add(child.getId());

		while (!candidateNodes.isEmpty()) {
			int thisTime, maxTime;
			curNode = nodes.get(candidateNodes.remove());
			maxTime = -1;
			meanRuntime = 0;
			for (int curRes = 0; curRes < resources.getSize(); curRes++) { // because the cheapest one is // the last
				// int readTime= curNode.getReadTime(bandwidth);
				meanRuntime += Math
						.round((float) curNode.getInstructionSize() / resources.getResource(curRes).getMIPS());
			}
			meanRuntime = meanRuntime / (resources.getSize());
			curNode.setMeanRunTime(meanRuntime);
			curNode.setScheduled();
			for (Link child : curNode.getChildren()) {
				boolean isCandidate = true;
				childNode = nodes.get(child.getId());
				for (Link parent : childNode.getParents())
					if (!nodes.get(parent.getId()).isScheduled())
						isCandidate = false;
				if (isCandidate)
					candidateNodes.add(child.getId());
			}
		}
		for (WorkflowNode node : nodes.values())
			node.setUnscheduled();
	}

	protected long getDataSize(WorkflowNode parent, WorkflowNode child) {
		long size = 0;
		for (Link link : parent.getChildren())
			if (link.getId().equals(child.getId())) {
				size = link.getDataSize();
				break;
			}
		return (size);
	}

	protected long setEndNodeAST() {
		int endTime = -1;
		WorkflowNode endNode = graph.getNodes().get(graph.getEndId());

		for (Link parent : endNode.getParents()) {
			int curEndTime = graph.getNodes().get(parent.getId()).getAFT();
			if (endTime < curEndTime)
				endTime = curEndTime;
		}
		endNode.setAST(endTime);
		endNode.setAFT(endTime);
		return endTime;
	}

	protected void initializeStartEndNodes(int startTime, int deadline) {
		Map<String, WorkflowNode> nodes = graph.getNodes();

		nodes.get(graph.getStartId()).setScheduled();
		nodes.get(graph.getEndId()).setScheduled();
	}

	protected void initializeStartEndNodes(WorkflowGraph graph, long startTime, long deadline) {
		Map<String, WorkflowNode> nodes = graph.getNodes();

		nodes.get(graph.getStartId()).setScheduled();
		nodes.get(graph.getEndId()).setScheduled();
	}

	public float computeFinalCost() {
		float totalCost = 0, curCost;
		int graphNode = 0;
		try {
			graphNode = graph.getNodeNum();
		} catch (Exception e) {
			// TODO: handle exception
		}

		int count = 0;
		int VmRunTime = 0, VmRuntime2 = 0;
		int startEx = Integer.MAX_VALUE, endEx = 0;
		for (int instId = 0; instId < instances.getSize(); instId++) {
			Instance inst = instances.getInstance(instId);
			startEx = Integer.MAX_VALUE;
			endEx = 0;
			if (inst.getFinishTime() == 0)
				continue;
			// curCost = (float)Math.ceil((double)(inst.getFinishTime() -
			// inst.getStartTime()) / (double)resources.getInterval())
			// * inst.getType().getCost() ;
			for (execution ex : inst.getExeList()) {
				if (ex.getStart() < startEx)
					startEx = (int) ex.getStart();
				if (ex.getFinish() > endEx) {
					endEx = (int) ex.getFinish();
				}
				count++;
			}

			VmRunTime = (int) (inst.getFinishTime() - inst.getStartTime());
			if (VmRunTime != (endEx - startEx)) {
//				if (currentAlgrotihm != "MyPCP")
//				throw new ArithmeticException(
//						currentAlgrotihm + " instCompletion:" + VmRunTime + "/ " + (endEx - startEx));
			}
//			VmRuntime2 = (last.getEFT() - first.getEST());
//			curCost = (float) Math.ceil((double) (last.getEFT() - first.getEST()) / (double) resources.getInterval())
//					* inst.getType().getCost();
			curCost = (float) Math.ceil((double) (endEx - startEx) / (double) resources.getInterval())
					* inst.getType().getCost();
			totalCost += curCost;
		}

		try {

			if (graphs.length > 0) {
				int nodeSize = 0;
				int workloads = 0;
				for (WorkflowGraph workflowGraph : graphs) {
					nodeSize += workflowGraph.getNodes().size();
					workloads++;
				}
				System.err.print(
						"\n ==> Workloads: " + workloads );
			} 
		} catch (Exception e) {
			// TODO: handle exception
			
		}
//		computeFinalEnergy();
		return (totalCost);
	}

	public long computeInstanceUtilization(String algName, int instanceId) {
		Instance inst = instances.getInstance(instanceId);
		int util = 0;

		if (inst.getFinishTime() == 0)
			return util;
		// curCost = (float)Math.ceil((double)(inst.getFinishTime() -
		// inst.getStartTime()) / (double)resources.getInterval())
		// * inst.getType().getCost() ;

		int busyTime = 0;

		int compute = (int) (inst.getFinishTime() - inst.getStartTime());

		int cs = (int) Math.ceil(((float) compute / resources.getInterval()));
		cs = resources.getInterval() * cs;

		for (execution ex : inst.getExeList()) {
			busyTime += ex.getExecutionTime();
		}
		util = Math.round((float) busyTime * 100 / cs);
//		if (compute!= busyTime)
//		{
//			throw new ArithmeticException( algName+" ins:"+instanceId +" insCompute:" +compute+ "instanceExecutionTasks"+ busyTime);
//		}
		return util;
	}

	public long setEndNodeEST() {
		int endTime = -1;

		WorkflowNode endNode = graph.getNodes().get(graph.getEndId());
		int finishtime = -1;
		finishtime = computeFinalTimeBasedExecutions();

		for (Link parent : endNode.getParents()) {
			int curEndTime = graph.getNodes().get(parent.getId()).getEFT();
			if (endTime < curEndTime)
				endTime = curEndTime;
		}

		endNode.setEST(endTime);
		endNode.setEFT(endTime);
		return endTime;
	}

	public long setEndNodeEST(WorkflowGraph graph) {
		int endTime = -1;

		WorkflowNode endNode = graph.getNodes().get(graph.getEndId());
		int finishtime = -1;
		finishtime = computeFinalTimeBasedExecutions();

		for (Link parent : endNode.getParents()) {
			int curEndTime = graph.getNodes().get(parent.getId()).getEFT();
			if (endTime < curEndTime)
				endTime = curEndTime;
		}

		endNode.setEST(endTime);
		endNode.setEFT(endTime);
		return endTime;
	}

	public long computeFinalEnergy() {
		result rs = new result();
		float totalCost = 0, curCost;
		int busyTime = 0;
		int energyBusy = 0, energyIdle = 0;
		float voltage = 0;
		int totalEnergy = 0, totalenergyIdle = 0, totalenergyBusy = 0;
		long startIns = Integer.MAX_VALUE, finishIns = 0;
		long totalRunVMs = 0;
		long totalIdle = 0, totalBusy = 0;
		int idle;
		for (int instId = 0; instId < instances.getSize(); instId++) {
			Instance inst = instances.getInstance(instId);
			energyBusy = 0;
			energyIdle = 0;
			busyTime = 0;
			if (inst.getFinishTime() == 0)
				continue;
			// curCost = (float)Math.ceil((double)(inst.getFinishTime() -
			// inst.getStartTime()) / (double)resources.getInterval())
			// * inst.getType().getCost() ;
			startIns = Integer.MAX_VALUE;
			finishIns = 0;
			execution ex;
			int exSize = inst.getExeList().size();
			for (int i = 0; i < exSize; i++) {
				ex = inst.getExeList().get(i);
				if (i < exSize - 1)
					idle = (int) (inst.getExeList().get(i + 1).getStart() - ex.getFinish());
				else
					idle = 0;
				if (idle > 15) {
					// sleep mode energy
					energyIdle += Math
							.round((float) calcEnergy((int) idle, inst.getType().getMinFrequency(), inst) / 5);

				} else
					energyIdle += calcEnergy((int) idle, inst.getType().getMinFrequency(), inst);
				busyTime = (int) (ex.getFinish() - ex.getStart());
				totalBusy += busyTime;
				energyBusy += calcEnergy((int) (busyTime), ex.Frequency, inst);// busyTime * (voltage *
				if (startIns > ex.getStart())
					startIns = ex.getStart();
				if (finishIns < ex.getFinish())
					finishIns = ex.getFinish();
			}

			try {
				WorkflowNode first = graph.getNodes().get(inst.getFirstTask()),
						last = graph.getNodes().get(inst.getLastTask());
				if (startIns > first.getEST())
					startIns = first.getEST();
				if (finishIns < last.getEFT())
					finishIns = last.getEFT();
			} catch (Exception e) {
				// TODO: handle exception
			}

			long runningVmTime = finishIns - startIns;
			totalRunVMs += runningVmTime;
			float minFreq = inst.getType().getMinFrequency();

			int idleTime = (int) (runningVmTime - busyTime);
			totalIdle += idleTime;
			energyIdle = calcEnergy((int) idleTime, minFreq, inst); // (int) ((energyPerSec * (minvoltage *
																	// minvoltage) * minFreq) * idleTime);

			inst.setEnergyIdle(energyIdle);
			inst.setEnergyBusy(energyBusy);
			totalenergyIdle += energyIdle;
			totalenergyBusy += energyBusy;
			if (totalenergyIdle < 0)
				totalenergyIdle = 0;

			curCost = (float) Math.ceil((double) (finishIns - startIns) / (double) resources.getInterval())
					* inst.getType().getCost();
			totalCost += curCost;
		}
//		totalEnergy = (totalenergyIdle + totalenergyBusy);
		totalEnergy = (totalenergyBusy + totalenergyIdle);
		String KW = String.format("%.2f", (float) totalEnergy / 1000);
		float EnergyPerHour = ((float) totalEnergy * 3600) / totalRunVMs / 1000;
		if (totalEnergy > 0)
			System.err.print("\n ==>#KW:" + KW + " #Energy: " + totalEnergy + " ,en_B:" + totalenergyBusy + " ,en_I:"
					+ totalenergyIdle + "  ,KW/H: " + EnergyPerHour + " ,idlTime:" + totalIdle + " ,exTime:" + totalBusy
					+ "\n");
		rs.energy = totalenergyBusy;
		rs.totalEnergy = totalEnergy;
		rs.cost = totalCost;
		return (totalEnergy);
	}

	public int calcEnergy(int exTime, float freq, Instance curInst) {
		// float voltage =
		// WorkflowBroker.getVoltageByFrequency(curInst.getFrequencyLevel(freq));
		float energypersec = curInst.getType().getEnergyPerSec();
//		return (calcEnergyBasedOnSurvay(exTime,freq,curInst));
		return (calcEnergyBasedLi(exTime, freq, curInst));

	}

	public int calcEnergyBasedOnSurvay(int exTime, float freq, Instance curInst) {
//		Bambagini, M., Marinoni, M., Superiore, S., & Anna, S. (2016). Energy-Aware Scheduling for Real-Time Systems: A Survey. Association for Computing Machinery, 15(1). https://doi.org/10.1145/2808231
		float freqLevel = curInst.getFrequencyLevel(freq);
		int energy = (int) Math.round((0.2 + 0.8 * Math.pow(freqLevel, 3)) * exTime);
		return energy;
	}

	public int calcEnergyBasedLi(int exTime, float freq, Instance curInst) {
//		Li, Z., Ge, J., Hu, H., Song, W., Hu, H., & Luo, B. (2018). Cost and Energy Aware Scheduling Algorithm for Scientific Workflows with Deadline Constraint in Clouds. IEEE TRANSACTIONS ON SERVICES COMPUTING, 11(4), 713–726.

//		curInst.getType().getEnergyPerSec());
		float freqLevel = curInst.getFrequencyLevel(freq);
		float voltage = curInst.getType().getVoltage();
		voltage = voltage * freqLevel;
		if (voltage == 0) {
			// System.out.print("Voltage is not set honey. ");
			return (int) Math.ceil((float) exTime * Math.pow(freq, 3) * curInst.getType().getEnergyPerSec());
		}
		float lenVol = curInst.getType().getVoltage() - curInst.getType().getMinVoltage();
		float lenFreq = curInst.getType().getMaxFrequencyOr1() - curInst.getType().getMinFrequency();
		float f = freq - curInst.getType().getMinFrequency();
		f = (f * 1 / (float) 1.2);
		voltage = curInst.getType().getMinVoltage() + (lenVol * f);
		int energy = (int) Math
				.ceil((float) exTime * freq * Math.pow(voltage, 2) * curInst.getType().getEnergyPerSec());
		return energy;
	}

	public int computeFinalTime() {
		float totalCost = 0, curCost;
		int ft = 0;
		for (int instId = 0; instId < instances.getSize(); instId++) {
			Instance inst = instances.getInstance(instId);

			if (inst.getFinishTime() == 0)
				break;
			// curCost = (float)Math.ceil((double)(inst.getFinishTime() -
			// inst.getStartTime()) / (double)resources.getInterval())
			// * inst.getType().getCost() ;
			WorkflowNode first = graph.getNodes().get(inst.getFirstTask()),
					last = graph.getNodes().get(inst.getLastTask());
			if (last.getEFT() > ft)
				ft = last.getEFT();

		}

		return (ft);
	}

	public void deleteNotExecutedInstances() {
		Boolean state = true;
		while (state) {
			for (Instance it : instances.getinstances()) {
				state = false;
				try {

					if (it.getFinishTime() == 0 && it.getExeList().size() > 0) {
						if (it.getExeList().get(0).getFinish() > 0) {
							throw new ArithmeticException(
									"Deleting instance error: finishTime is 0 and exeList have tasks");
						}
					}
					if (it.getFinishTime() == 0 || it == null) {
						instances.removeInstance(it);
						state = true;
						break;
					}
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		}
	}

	public int computeFinalTimeBasedExecutions() {
		deleteNotExecutedInstances();
		int count = 0;
		float totalCost = 0, curCost;
		int ft = 0;

		for (int instId = 0; instId < instances.getSize(); instId++) {
			Instance inst = instances.getInstance(instId);

			if (inst.getFinishTime() == 0)
				continue;
			for (execution ex : inst.getExeList()) {
				count++;
				if (ex.getFinish() > ft)
					ft = (int) ex.getFinish();
			}
		}
		try {
			int margin = 2;
			if (graphs.length > 0) {
				nodeSize = 0;
				for (int i = 0; i < graphs.length; i++) {
					nodeSize += graphs[i].nodes.size();
					margin += 2;
				}
			} else {
				nodeSize = graph.nodes.size();
			}


		} catch (Exception e) {
			// TODO: handle exception
		}

		return (ft);
	}

	public static class UpRankComparator implements Comparator<WorkflowNode> {
		public int compare(WorkflowNode node1, WorkflowNode node2) {
			if (node1.getUpRank() < node2.getUpRank())
				return (1);
			else if (node1.getUpRank() > node2.getUpRank())
				return (-1);
			else
				return (0);
		}
	}

	public static class DownRankComparator implements Comparator<WorkflowNode> {
		public int compare(WorkflowNode node1, WorkflowNode node2) {
			if (node1.getDownRank() < node2.getDownRank())
				return (1);
			else if (node1.getDownRank() > node2.getDownRank())
				return (-1);
			else
				return (0);
		}
	}

	public static class DownLevelComparator implements Comparator<WorkflowNode> {
		public int compare(WorkflowNode node1, WorkflowNode node2) {
			if (node1.getLevelBottem() < node2.getLevelBottem())
				return (1);
			else if (node1.getLevelBottem() > node2.getLevelBottem())
				return (-1);
			else
				return (0);
		}
	}

	public static class subDeadlineAscending implements Comparator<WorkflowNode> {
		public int compare(WorkflowNode node1, WorkflowNode node2) {
			if (node1.getDeadline() < node2.getDeadline())
				return (-1);
			else if (node1.getDeadline() > node2.getDeadline())
				return (1);
			else
				return (0);
		}
	}

	public static class EarliestStartTime implements Comparator<WorkflowNode> {

		public int compare(WorkflowNode node1, WorkflowNode node2) {
			if (node1.getEST() >= node2.getEST())
				return (1);
			else if (node1.getEST() < node2.getEST())
				return (-1);
			else
				return (0);
		}
	}

	public static class SumRankComparator implements Comparator<WorkflowNode> {
		public int compare(WorkflowNode node1, WorkflowNode node2) {
			if (node1.getSumRank() < node2.getSumRank())
				return (1);
			else if (node1.getSumRank() > node2.getSumRank())
				return (-1);
			else
				return (0);
		}
	}

	public static class ASTComparator implements Comparator<WorkflowNode> {
		public int compare(WorkflowNode node1, WorkflowNode node2) {
			if (node1.getAST() < node2.getAST())
				return (-1);
			else if (node1.getAST() > node2.getAST())
				return (1);
			else
				return (0);
		}
	}

	protected result setInstanceE(WorkflowNode curNode, Instance curInst, result r) {
		if (r == null) {
			r = checkInstanceE(curNode, curInst);
		}
		if (r.freqLevel == 0)
			r.freqLevel = curInst.getFrequencyLevel(r.freq);
		if (r.inplace) {
			return setInstanceInPlace(curNode, curInst, r);
		}

		long start, curStart = curInst.getFinishTime(), curFinish, readStart;
		// checks Latest start Time in Instances
		readStart = curStart;

		int interval = resources.getInterval();
		long finishTime = curInst.getFinishTime();
		long startTime = curInst.getStartTime();
		double curCost = Math.ceil((double) (finishTime - startTime) / (double) interval) * curInst.getType().getCost();
		curStart = (int) GetMyFileTransferTime(curStart, curNode, curInst);

		long compute = Math.round((float) curNode.getInstructionSize() / (curInst.getType().getMIPS() * r.freqLevel));
		compute = (long) utility.configuration.GetUncertaionExecutionTime(compute);
		if (curInst.getFinishTime() == 0) {
			curInst.setStartTime(curStart);
			curStart += configuration.getProvisioningDelay();
			curInst.setFirstTask(curNode.getId());
		}

		curFinish = curStart + compute;
		float Cost = (float) (Math.ceil((double) (curFinish - curInst.getStartTime()) / (double) interval)
				* curInst.getType().getCost() - curCost);

		//

		curNode.setAST((int) curStart);
		curNode.setAFT((int) curFinish);
		curNode.setEST((int) curStart);
		curNode.setEFT((int) curFinish);
		curNode.setSelectedResource(curInst.getId());
		curNode.setStartReading((int) readStart);
		curNode.setScheduled();

		// curInst.addExe(curNode.getId(), (int) curStart, curFinish, readStart, Cost);
//		if (curNode.getId().contains("ID00024"))
//			System.out.print("-"+curFinish);

		curInst.addExe(curNode.getId(), (int) curStart, curFinish, readStart, Cost, (int) r.energy, r.reliability,
				r.freq, r.freqLevel);
		if (curFinish > finishTime) {
			curInst.setFinishTime(curFinish);
			curInst.setLastTask(curNode.getId());
		}

		result res = new result();
		res.freq = r.freq;
		double y = curInst.getType().getFaultRate();
		int transferTime = (int) getTransferTime(curNode, curInst);
		res.transferTime = transferTime;
		res.finishTime = (int) curFinish;
		res.energy = calcEnergy((int) compute, r.freq, curInst);
		res.reliability = calcReliability(0, (int) compute, r.freq, curInst);

		res.cost = (float) (Math.ceil((double) (curFinish - startTime) / (double) interval)
				* curInst.getType().getCost() - curCost);
		res.startTime = (int) curStart;
		res.freqLevel = r.freqLevel;

		return res;

	}

	protected result setInstanceE(WorkflowNode curNode, Instance curInst, result r, WorkflowGraph graph) {

		if (r.freqLevel == 0)
			r.freqLevel = curInst.getFrequencyLevel(r.freq);
		if (r.inplace) {
			return setInstanceInPlace(curNode, curInst, r);
		}

		long start, curStart = curInst.getFinishTime(), curFinish, readStart;
		// checks Latest start Time in Instances
		readStart = curStart;

		int interval = resources.getInterval();
		long finishTime = curInst.getFinishTime();
		long startTime = curInst.getStartTime();
		double curCost = Math.ceil((double) (finishTime - startTime) / (double) interval) * curInst.getType().getCost();
		curStart = (int) GetMyFileTransferTime(curStart, curNode, curInst);

		long compute = Math.round((float) curNode.getInstructionSize() / (curInst.getType().getMIPS() * r.freqLevel));
		compute = utility.configuration.GetUncertaionExecutionTime(compute);

		if (curStart > curInst.getFinishTime() + 1 && curInst.getFinishTime() > 0) {
			curInst.addGap(curInst.getFinishTime(), curStart);
		}

		if (curInst.getFinishTime() == 0) {
			curInst.setStartTime(curStart);
			curStart += configuration.getProvisioningDelay();
			curInst.setFirstTask(curNode.getId());
		}

		curFinish = curStart + compute;

		//

		curNode.setAST((int) curStart);
		curNode.setAFT((int) curFinish);
		curNode.setEST((int) curStart);
		curNode.setEFT((int) curFinish);
		curNode.setSelectedResource(curInst.getId());
		curNode.setStartReading((int) readStart);
		curNode.setScheduled();

		// curInst.addExe(curNode.getId(), (int) curStart, curFinish, readStart, Cost);
//		if (curNode.getId().contains("ID00024"))
//			System.out.print("-"+curFinish);

		if (curFinish > finishTime) {
			curInst.setFinishTime(curFinish);
			curInst.setLastTask(curNode.getId());
		}
		float Cost = (float) (Math.ceil((double) (curFinish - startTime) / (double) interval)
				* curInst.getType().getCost() - curCost);

		curInst.addExe(curNode.getId(), (int) curStart, curFinish, readStart, Cost, (int) r.energy, r.reliability,
				r.freq, r.freqLevel);
		result res = new result();
		res.freq = r.freq;
		double y = curInst.getType().getFaultRate();
		int transferTime = (int) getTransferTime(curNode, curInst);
		res.transferTime = transferTime;
		res.finishTime = (int) curFinish;
		res.energy = calcEnergy((int) compute, r.freq, curInst);
		res.reliability = calcReliability(0, (int) compute, r.freq, curInst);

		res.cost = (float) (Math.ceil((double) (curFinish - startTime) / (double) interval)
				* curInst.getType().getCost() - curCost);
		res.startTime = (int) curStart;
		res.freqLevel = r.freqLevel;

		return res;

	}

	protected result setInstanceE(WorkflowNode curNode, Instance curInst) {

		long start, curStart = curInst.getFinishTime(), curFinish, readStart;
		// checks Latest start Time in Instances
		readStart = curStart;

		int interval = resources.getInterval();
		long finishTime = curInst.getFinishTime();
		long startTime = curInst.getStartTime();
		double curCost = Math.ceil((double) (finishTime - startTime) / (double) interval) * curInst.getType().getCost();
		curStart = (int) GetMyFileTransferTime(curStart, curNode, curInst);

		long compute = Math.round((float) curNode.getInstructionSize() / (curInst.getType().getMIPS() * 1));
		compute = (long) utility.configuration.GetUncertaionExecutionTime(compute);

		if (curInst.getFinishTime() == 0) {

			curInst.setStartTime(curStart);
			curStart += configuration.getProvisioningDelay();
			curInst.setFirstTask(curNode.getId());
		}

		curFinish = curStart + compute;
		float Cost = (float) (Math.ceil((double) (curFinish - startTime) / (double) interval)
				* curInst.getType().getCost() - curCost);

		curNode.setAST((int) curStart);
		curNode.setAFT((int) curFinish);
		curNode.setEST((int) curStart);
		curNode.setEFT((int) curFinish);
		curNode.setSelectedResource(curInst.getId());
		curNode.setStartReading((int) readStart);
		curNode.setScheduled();

		float freq = curInst.getType().getMaxFrequencyOr1();

		result res = new result();
		res.transferTime = (int) getTransferTime(curNode, curInst);

		res.freq = freq;
		res.freqLevel = 1;
		double y = curInst.getType().getFaultRate();
		curFinish = curStart + compute;
		res.finishTime = (int) curFinish;
		res.energy = calcEnergy((int) compute, freq, curInst);
		res.reliability = calcReliability(0, (int) compute, freq, curInst);

		res.cost = (float) (Math.ceil((double) (curFinish - startTime) / (double) interval)
				* curInst.getType().getCost() - curCost);

		res.startTime = (int) curStart;

		// curInst.addExe(curNode.getId(), (int) curStart, curFinish, readStart, Cost);
		curInst.addExe(curNode.getId(), (int) curStart, curFinish, readStart, Cost, (int) res.energy, res.reliability,
				res.freq, res.freqLevel);
		if (curFinish > finishTime) {
			curInst.setFinishTime(curFinish);
			curInst.setLastTask(curNode.getId());
		}
		if (curInst.getExeList() == null) {
			System.out.print("");
		}

		return res;

	}

	protected result setInstanceInPlace(WorkflowNode curNode, Instance curInst, result r) {
		long start, curStart = curInst.getFinishTime(), curFinish, readStart;
		// checks Latest start Time in Instances
		readStart = curStart;
		// System.out.print(" -inplace:" + curNode.getId()+"->I " + curInst.getId()+
		// "-");
		int interval = resources.getInterval();
		long finishTime = curInst.getFinishTime();
		long startTime = curInst.getStartTime();
		double curCost = Math.ceil((double) (finishTime - startTime) / (double) interval) * curInst.getType().getCost();
		float Cost;

		if (curInst.getFinishTime() == 0) {
			curInst.setStartTime(curStart);
			r.startTime += configuration.getProvisioningDelay();
			r.finishTime += configuration.getProvisioningDelay();
			curInst.setFirstTask(curNode.getId());
		}

		curStart = r.startTime;
		long compute = Math.round((float) curNode.getInstructionSize() / (curInst.getType().getMIPS() * r.freqLevel));
		compute = utility.configuration.GetUncertaionExecutionTime(compute);
		
		curFinish = curStart+ compute;
		if (r.finishTime<curFinish) {
			curFinish=r.finishTime;
		}
		Cost = 0;
//		if (finishTime > curFinish)
//			Cost = 0;
//		else
//			Cost = (float) (Math.ceil((double) (curFinish - Math.min(startTime, curStart)) / (double) interval)
//					* curInst.getType().getCost() - curCost);

		if (curStart > curInst.getFinishTime() + 1 && curInst.getFinishTime() > 0) {
			curInst.addGap(curInst.getFinishTime(), curStart);
		}

		execution gap = curInst.getGaplist().get(r.gapid);
		if (r.startTime < gap.getStart() || r.finishTime > gap.getFinish()) {
			System.err.println("error gap");

		}

		if (r.finishTime < gap.getFinish()) {
			curInst.addGap(r.finishTime, gap.getFinish());
		}

		curInst.getGaplist().get(r.gapid).setFinish(r.startTime);

		curNode.setAST((int) curStart);
		curNode.setAFT((int) curFinish);
		curNode.setEST((int) curStart);
		curNode.setEFT((int) curFinish);
		curNode.setSelectedResource(curInst.getId());
		curNode.setStartReading((int) readStart);
		curNode.setScheduled();
		if (utility.configuration.isPrintGapInsertation())
			System.out.print("\n------------<<alloc gap  Node " + curNode.getId() + "=> inst: " + curInst.getId()
					+ "  [ " + curStart + ": " + curFinish + "] ");

		// curInst.addExe(curNode.getId(), (int) curStart, curFinish, readStart, Cost);
		curInst.addExe(curNode.getId(), (int) curStart, curFinish, readStart, Cost, (int) r.energy, r.reliability,
				r.freq, r.freqLevel);
		if (curFinish > finishTime) {
			curInst.setFinishTime(curFinish);
			curInst.setLastTask(curNode.getId());
		}
		result res = new result();
		res.transferTime = (int) getTransferTime(curNode, curInst);
		res.freq = r.freq;
		res.freqLevel = r.freqLevel;
		double y = curInst.getType().getFaultRate();
		curFinish = curStart + Math.round((float) curNode.getInstructionSize() / curInst.getType().getMIPS());
		res.finishTime = r.finishTime;
		res.energy = calcEnergy((int) (r.finishTime - r.startTime), r.freq, curInst);
		res.reliability = calcReliability(0, (int) r.finishTime - r.startTime, r.freq, curInst);
		res.cost = Cost;
		res.startTime = r.startTime;
		return res;

	}

	protected result setInstanceInPlace(WorkflowNode curNode, Instance curInst, result r, WorkflowGraph graph) {
		long start, curStart = curInst.getFinishTime(), curFinish, readStart;
		// checks Latest start Time in Instances
		readStart = curStart;
		// System.out.print(" -inplace:" + curNode.getId()+"->I " + curInst.getId()+
		// "-");
		int interval = resources.getInterval();
		long finishTime = curInst.getFinishTime();
		long startTime = curInst.getStartTime();
		double curCost = Math.ceil((double) (finishTime - startTime) / (double) interval) * curInst.getType().getCost();
		float Cost;

		if (curInst.getFinishTime() == 0) {
			curInst.setStartTime(curStart);
			r.startTime += configuration.getProvisioningDelay();
			r.finishTime += configuration.getProvisioningDelay();
			curInst.setFirstTask(curNode.getId());
		}

		curStart = r.startTime;
		curFinish = r.finishTime;

		if (finishTime > curFinish)
			Cost = 0;
		else
			Cost = (float) (Math.ceil((double) (curFinish - Math.max(startTime, curStart)) / (double) interval)
					* curInst.getType().getCost() - curCost);

		curNode.setAST((int) curStart);
		curNode.setAFT((int) curFinish);
		curNode.setEST((int) curStart);
		curNode.setEFT((int) curFinish);
		curNode.setSelectedResource(curInst.getId());
		curNode.setStartReading((int) readStart);
		curNode.setScheduled();
		if (utility.configuration.isPrintGapInsertation())
			System.out.print("\n------------<<alloc gap  Node " + curNode.getId() + "=> inst: " + curInst.getId()
					+ "  [ " + curStart + ": " + curFinish + "] ");
		if (curInst.getFinishTime() == 0) {
			// curInst.setStartTime(curStart);
			// sets start of the instances to the time that it reads files from storage or
			// parent of a node;
			curInst.setStartTime(curStart);
			curInst.setFirstTask(curNode.getId());
		}
		// curInst.addExe(curNode.getId(), (int) curStart, curFinish, readStart, Cost);
		curInst.addExe(curNode.getId(), (int) curStart, curFinish, readStart, Cost, (int) r.energy, r.reliability,
				r.freq, r.freqLevel);
		if (curFinish > finishTime) {
			curInst.setFinishTime(curFinish);
			curInst.setLastTask(curNode.getId());
		}
		result res = new result();
		res.transferTime = (int) getTransferTime(curNode, curInst, graph);
		res.freq = r.freq;
		res.freqLevel = r.freqLevel;
		double y = curInst.getType().getFaultRate();
		curFinish = curStart + Math.round((float) curNode.getInstructionSize() / curInst.getType().getMIPS());
		res.finishTime = r.finishTime;
		res.energy = calcEnergy((int) (r.finishTime - r.startTime), r.freq, curInst);
		res.reliability = calcReliability(0, (int) r.finishTime - r.startTime, r.freq, curInst);
		res.cost = Cost;
		res.startTime = r.startTime;
		return res;

	}

	public result setInstance(List<WorkflowNode> path, Instance curInst, result r) {

		result res = new result();
		long finishTime = curInst.getFinishTime();
		long startTime = curInst.getStartTime();
		int[] newESTs = new int[path.size()], newLFTs = new int[path.size()], newDTT = new int[path.size()];
		boolean success = true;
		long curTime = 0, curRuntime = 0;
		int firstStart = path.get(0).getEST(), lastFinish = path.get(path.size() - 1).getLFT();

		// check after finish time
		newESTs = computeNewESTs(path, curInst, finishTime, r);
		newLFTs = computeNewLFTs(path, curInst, lastFinish, r);

		// my edit
		if (curInst.getFinishTime() == 0) {
			newESTs[0] += configuration.getProvisioningDelay();
		}

		for (int i = 0; i < path.size() && success; i++) {
			WorkflowNode curNode = path.get(i);
			curRuntime = Math.round((float) curNode.getInstructionSize() / (curInst.getType().getMIPS() * r.freqLevel));
			curRuntime = (long) utility.configuration.GetUncertaionExecutionTime(curRuntime);

			curTime = newESTs[i] + curRuntime;
			if (curTime > newLFTs[i])
				success = false;

			curNode.setEST(newESTs[i]);
			curNode.setEFT((int) curTime);

			curNode.setLFT(newLFTs[i]);
			curNode.setLST((int) (newLFTs[i] - curRuntime));
			if (curInst.getFinishTime() < curNode.getEFT()) {
				curInst.setFinishTime(curNode.getEFT());
				curInst.setLastTask(curNode.getId());
			}
			if (curInst.getStartTime() > curNode.getEST() || curInst.getStartTime() == 0) {
				curInst.setStartTime(curNode.getEST());
				curInst.setFirstTask(curNode.getId());
			}

			// my edit ahmad
			if (success) {
				curNode.setNeedTransferTime(newDTT[i]);

				curNode.setRunTime((int) curRuntime);
				curNode.setSelectedResource(curInst.getId());
				curNode.setScheduled();
				// (String id, long start, long finish, long readStart, float cost,int
				// energy,float freq)
				curInst.addExe(curNode.getId(), curNode.getEST(), curNode.getEFT(), 0, r.cost, (int) r.energy,
						r.reliability, r.freq, r.freqLevel);

				if (curInst.getFinishTime() < curNode.getEFT()) {
					curInst.setFinishTime(curNode.getEFT());
					curInst.setLastTask(curNode.getId());
				}
				if (curInst.getStartTime() > curNode.getEST() || curInst.getStartTime() == 0) {
					curInst.setStartTime(curNode.getEST());
					curInst.setFirstTask(curNode.getId());
				}

				execution ex = getExecutedTask(curNode.getId());
				if (ex != null) {
					ex.setStart(curNode.getEST());
					ex.setFinish((int) curNode.getEFT());
				}

			}
		}
		if (success) {
			if (curInst.getFinishTime() == 0) {
				curInst.setStartTime(path.get(0).getEST());
				curInst.setFirstTask(path.get(0).getId());
			}
			// *************************************************************
			// curInst.setFinishTime(path.get(path.size()-1).getLFT());
//			curInst.setFinishTime(path.get(path.size() - 1).getEFT());
			curInst.setLastTask(path.get(path.size() - 1).getId());

			curInst.getTasks().addAll(curInst.getTasks().size(), path);

			res.freq = r.freq;
			double y = curInst.getType().getFaultRate();

			res.finishTime = (int) curTime;
			res.energy = calcEnergy((int) curRuntime, r.freq, curInst);
			res.reliability = calcReliability(0, (int) curRuntime, r.freq, curInst);
			float newCost = (float) (Math.ceil((double) (curTime - startTime) / (double) resources.getInterval())
					* (curInst.getType().getCost() * 1));
			// *bestFreq
			res.cost = (float) newCost;
			res.startTime = (int) newESTs[0];
			res.freqLevel = r.freqLevel;
			res.transferTime = computePathDataTransfers(path, curInst);
			return res;
		} else
			for (int i = 0; i < path.size(); i++)
				path.get(i).setUnscheduled();

		boolean backCheck = true;
		// check before start time
		if (!backCheck)
			return res;
		newLFTs = computeNewLFTs(path, curInst, startTime);
		newESTs = computeNewESTs(path, curInst, firstStart);
		// my edit ahmad

		success = true;
		for (int i = path.size() - 1; i >= 0 && success; i--) {
			WorkflowNode curNode = path.get(i);
			curRuntime = Math.round((float) curNode.getInstructionSize() / (curInst.getType().getMIPS() * r.freqLevel));
			curRuntime = (long) utility.configuration.GetUncertaionExecutionTime(curRuntime);
			curTime = newLFTs[i] - curRuntime;
			if (curTime < newESTs[i])
				success = false;

			curNode.setLFT(newLFTs[i]);
			curNode.setLST((int) curTime);

			// curNode.setEST(curNode.getLST());
			// curNode.setEFT(curNode.getLFT());
			curNode.setEST(newESTs[i]);
			curNode.setEFT((int) (newESTs[i] + curRuntime));
			if (curInst.getFinishTime() < curNode.getEFT()) {
				curInst.setFinishTime(curNode.getEFT());
				curInst.setLastTask(curNode.getId());
			}
			if (curInst.getStartTime() > curNode.getEST() || curInst.getStartTime() == 0) {
				curInst.setStartTime(curNode.getEST());
				curInst.setFirstTask(curNode.getId());
			}

			execution ex = getExecutedTask(curNode.getId());
			if (ex != null) {
				ex.setStart(curNode.getEST());
				ex.setFinish((int) curNode.getEFT());
			}
			if (curInst.getFinishTime() < curNode.getEFT()) {
				curInst.setFinishTime(curNode.getEFT());
				curInst.setLastTask(curNode.getId());
			}
			if (curInst.getStartTime() > curNode.getEST() || curInst.getStartTime() == 0) {
				curInst.setStartTime(curNode.getEST());
				curInst.setFirstTask(curNode.getId());
			}
			// my edit ahmad
			curNode.setNeedTransferTime(newDTT[i]);

			curNode.setRunTime((int) curRuntime);
			curNode.setSelectedResource(curInst.getId());
			curNode.setScheduled();

			r.freqLevel = curInst.getFrequencyLevel(r.freq);

			curInst.addExe(curNode.getId(), curNode.getEST(), curNode.getEFT(), 0, r.cost, (int) r.energy,
					r.reliability, r.freq, r.freqLevel);

		}
		if (curInst.getFinishTime() == 0) {
			// *************************************************************
			// curInst.setFinishTime(path.get(path.size()-1).getLFT());
			curInst.setFinishTime(path.get(path.size() - 1).getEFT());
			curInst.setLastTask(path.get(path.size() - 1).getId());
			curInst.setStartTime(path.get(0).getEST());
		} else {
			curInst.setStartTime(path.get(0).getEST());
		}
		curInst.setFirstTask(path.get(0).getId());

		curInst.getTasks().addAll(0, path);

		res.freq = r.freq;
		double y = curInst.getType().getFaultRate();
		res.freqLevel = r.freqLevel;
		res.finishTime = (int) curTime;
		res.energy = calcEnergy((int) curRuntime, r.freq, curInst);
		res.reliability = calcReliability(0, (int) curRuntime, r.freq, curInst);
		float newCost = (float) (Math.ceil((double) (curTime - startTime) / (double) resources.getInterval())
				* (curInst.getType().getCost() * 1));
		// *bestFreq
		res.cost = (float) newCost;
		res.transferTime = computePathDataTransfers(path, curInst);
		res.startTime = (int) path.get(0).getEST();

		return res;

	}

	private int[] computeNewESTs(List<Broker.WorkflowNode> path, Instance inst, long startTime) {
		int[] newESTs = new int[path.size()];
		int[] tempESTs = new int[path.size()];
		long start, selfTransfer, max, temp, curTime = startTime;
		temp = startTime;
		for (int i = 0; i < path.size(); i++) {
			max = 0;
			selfTransfer = 0;

			Broker.WorkflowNode curNode = path.get(i);
			temp = GetMyFileTransferTime(temp, curNode, inst);

			for (Link parent : curNode.getParents()) {
				WorkflowNode parentNode = graph.getNodes().get(parent.getId());
				if (parentNode.isScheduled()) {
					start = parentNode.getEFT();

					if (parentNode.getSelectedResource() != inst.getId()) {

						max += Math.round((float) parent.getDataSize() / bandwidth);
						selfTransfer = Math.round((float) curNode.getReadFileSize() / bandwidth);
						if (selfTransfer > max)
							max = selfTransfer;
						start += max;
					} else
						start += Math.round((float) curNode.getReadFileSize() / bandwidth);
				} else {
					if (i > 0 && parentNode.getId().equals(path.get(i - 1).getId()))
						start = curTime + Math.round((float) curNode.getReadFileSize() / bandwidth);
					else
						start = parentNode.getEFT() + Math.round((float) curNode.getReadFileSize() / bandwidth)
								+ Math.round((float) parent.getDataSize() / bandwidth);
				}
				if (start > curTime)
					curTime = start;
			}

			newESTs[i] = (int) curTime;
			// tempESTs[i] = (int) temp;
			curTime += Math.round(
					(float) curNode.getInstructionSize() / (inst.getType().getMIPS() * inst.getFrequencyLevel()));

			// curTime += Math.round((float) curNode.getInstructionSize() //
			// inst.getType().getMIPS());
		}

		return (newESTs);
	}

	public int computePathDataTransfers(List<Broker.WorkflowNode> path, Instance inst) {

		float ts = 0;
		for (int i = 0; i < path.size(); i++) {
			Broker.WorkflowNode curNode = path.get(i);
			for (Link parent : curNode.getParents()) {
				Broker.WorkflowNode parentNode = graph.getNodes().get(parent.getId());
				if (parentNode.isScheduled()) {
					if (parentNode.getSelectedResource() != inst.getId())
						ts += (float) parent.getDataSize() / bandwidth;
				} else {
					if (i > 0 && parentNode.getId().equals(path.get(i - 1).getId()))
						ts += 0;
					else
						ts += (float) parent.getDataSize() / bandwidth;
				}
			}
		}
		return Math.round(ts);
	}

	private int[] computeNewESTs(List<Broker.WorkflowNode> path, Instance inst, long startTime, result r) {
		int[] newESTs = new int[path.size()];
		int[] tempESTs = new int[path.size()];
		long start, selfTransfer, max, temp, curTime = startTime;
		if (inst.getFinishTime() == 0) {
			curTime += configuration.getProvisioningDelay();
		}
		temp = startTime;
		for (int i = 0; i < path.size(); i++) {
			max = 0;
			selfTransfer = 0;

			Broker.WorkflowNode curNode = path.get(i);
			temp = GetMyFileTransferTime(temp, curNode, inst);

			for (Link parent : curNode.getParents()) {
				WorkflowNode parentNode = graph.getNodes().get(parent.getId());
				if (parentNode.isScheduled()) {
					start = parentNode.getEFT();

					if (parentNode.getSelectedResource() != inst.getId()) {

						max += Math.round((float) parent.getDataSize() / bandwidth);
						selfTransfer = Math.round((float) curNode.getReadFileSize() / bandwidth);
						if (selfTransfer > max)
							max = selfTransfer;
						start += max;
					} else
						start += Math.round((float) curNode.getReadFileSize() / bandwidth);
				} else {
					if (i > 0 && parentNode.getId().equals(path.get(i - 1).getId()))
						start = curTime + Math.round((float) curNode.getReadFileSize() / bandwidth);
					else
						start = parentNode.getEFT() + Math.round((float) curNode.getReadFileSize() / bandwidth)
								+ Math.round((float) parent.getDataSize() / bandwidth);
				}
				if (start > curTime)
					curTime = start;
			}

			newESTs[i] = (int) curTime;
			// tempESTs[i] = (int) temp;
			curTime += Math.round((float) curNode.getInstructionSize() / (inst.getType().getMIPS() * r.freqLevel));
			// curTime += Math.round((float) curNode.getInstructionSize() //
			// inst.getType().getMIPS());
		}

		return (newESTs);
	}

	private int[] computeNewLFTs(List<Broker.WorkflowNode> path, Instance inst, long finishTime, result r) {
		int[] newLFTs = new int[path.size()];
		long finish, curTime = finishTime;

		for (int i = path.size() - 1; i >= 0; i--) {
			Broker.WorkflowNode curNode = path.get(i);
			for (Link child : curNode.getChildren()) {
				Broker.WorkflowNode childNode = graph.getNodes().get(child.getId());
				if (childNode.isScheduled()) {
					finish = childNode.getLST();
					if (childNode.getSelectedResource() != inst.getId())
						finish -= Math.round((float) child.getDataSize() / bandwidth);
				} else {
					if (i < path.size() - 1 && childNode.getId().equals(path.get(i + 1).getId()))
						finish = curTime;
					else
						finish = childNode.getLST() - Math.round((float) child.getDataSize() / bandwidth);
				}
				if (finish < curTime)
					curTime = finish;
			}

			newLFTs[i] = (int) curTime;
			curTime -= Math.round((float) curNode.getInstructionSize() / (inst.getType().getMIPS() * r.freqLevel));
		}

		return (newLFTs);
	}

	private int[] computeNewDataTransferTime(List<Broker.WorkflowNode> path, Instance inst, long startTime) {
		int[] newESTs = new int[path.size()];
		int[] tempESTs = new int[path.size()];
		long start, selfTransfer, max, temp, curTime = startTime;
		temp = startTime;
		for (int i = 0; i < path.size(); i++) {
			max = 0;
			selfTransfer = 0;

			Broker.WorkflowNode curNode = path.get(i);
			temp = GetMyFileTransferTime(0, curNode, inst);

			for (Link parent : curNode.getParents()) {
				Broker.WorkflowNode parentNode = graph.getNodes().get(parent.getId());
				if (parentNode.isScheduled()) {
					start = parentNode.getEFT();

					if (parentNode.getSelectedResource() != inst.getId()) {
						// max=super.GetMyFileTransferTime(curStart, curNode, in)
						max += Math.round((float) parent.getDataSize() / bandwidth);
						selfTransfer = Math.round((float) curNode.getReadFileSize() / bandwidth);
						if (selfTransfer > max)
							max = selfTransfer;
						start += max;
					} else
						start += Math.round((float) curNode.getReadFileSize() / bandwidth);
				} else {
					if (i > 0 && parentNode.getId().equals(path.get(i - 1).getId()))
						start = curTime + Math.round((float) curNode.getReadFileSize() / bandwidth);
					else
						start = parentNode.getEFT() + Math.round((float) curNode.getReadFileSize() / bandwidth)
								+ Math.round((float) parent.getDataSize() / bandwidth);
				}
				if (start > curTime)
					curTime = start;
			}

			newESTs[i] = (int) curTime;
			tempESTs[i] = (int) temp;
			temp += Math.round((float) curNode.getInstructionSize() / inst.getType().getMIPS());
			curTime += Math.round(
					(float) curNode.getInstructionSize() / (inst.getType().getMIPS() * inst.getFrequencyLevel()));
		}

		return (tempESTs);
	}

	private int[] computeNewLFTs(List<Broker.WorkflowNode> path, Instance inst, long finishTime) {
		int[] newLFTs = new int[path.size()];
		long finish, curTime = finishTime;

		for (int i = path.size() - 1; i >= 0; i--) {
			Broker.WorkflowNode curNode = path.get(i);
			for (Link child : curNode.getChildren()) {
				Broker.WorkflowNode childNode = graph.getNodes().get(child.getId());
				if (childNode.isScheduled()) {
					finish = childNode.getLST();
					if (childNode.getSelectedResource() != inst.getId())
						finish -= Math.round((float) child.getDataSize() / bandwidth);
				} else {
					if (i < path.size() - 1 && childNode.getId().equals(path.get(i + 1).getId()))
						finish = curTime;
					else
						finish = childNode.getLST() - Math.round((float) child.getDataSize() / bandwidth);
				}
				if (finish < curTime)
					curTime = finish;
			}

			newLFTs[i] = (int) curTime;
			curTime -= Math.round(
					(float) curNode.getInstructionSize() / (inst.getType().getMIPS() * inst.getFrequencyLevel()));
		}

		return (newLFTs);
	}

	protected float getMinFreq(long taskLength, long deadlineLength, Instance inst) {
		float minfreqlevel = (float) Math.min((float) 1.0,
				(float) Math.ceil(((float) taskLength * 10 / (deadlineLength))) / 10);
		float minFreq = inst.getFrequencyByFrequencyLevel(minfreqlevel);

		if (minFreq < inst.getType().getMinFrequency())
			minFreq = inst.getType().getMinFrequency();
		return minFreq;
	}

	protected result checkInstance(WorkflowNode curNode, Instance curInst, float freq) {
		float freqLevel = curInst.getFrequencyLevel(freq);
		long finishTime = curInst.getFinishTime();
		long startTime = curInst.getStartTime();
		int interval = resources.getInterval();
		double curCost = Math.ceil((double) (finishTime - startTime) / (double) interval) * curInst.getType().getCost();
		long curIntervalFinish = startTime
				+ (long) Math.ceil((double) (finishTime - startTime) / (double) interval) * interval;
		int start, curStart = (int) finishTime, curFinish;
		curStart = (int) GetMyFileTransferTime(curStart, curNode, curInst);

		if (finishTime == 0)
			startTime = curStart;

		long computation = Math.round((float) curNode.getInstructionSize() / (curInst.getType().getMIPS() * freqLevel));

		// long energy = Math.round(Math.pow(freq, 2) * computation);
		long energy = calcEnergy((int) computation, freq, curInst); // =Math.round(Math.pow(freq, 2)+
																	// WorkflowBroker.getVoltageByFrequency(freq) *
																	// curInst.getType().getEnergyPerSec() *
																	// computation);

		float reliability = calcReliability(0, (int) computation, freq, curInst); // (float) Math.exp(-1 *
																					// curInst.getType().getFaultRate()
//				* Math.pow(10, (fq * (1 - freq) / (1 - WorkflowBroker.minFrequency))) * (computation / freq));

		if (curInst.getFinishTime() == 0) {
			computation += configuration.getProvisioningDelay();
		}

		result r = new result();
		curFinish = (int) (curStart + computation);
		r.finishTime = curFinish;
		r.energy = energy;
		r.reliability = reliability;
		r.cost = (float) (Math.ceil((double) (curFinish - startTime) / (double) interval) * curInst.getType().getCost()
				- curCost);
		r.freqLevel = freqLevel;
		r.freq = freq;

		return (r);
	}

	protected result checkInstance(WorkflowNode curNode, Instance curInst) {
		if (curInst == null)
			return null;

		float fq = 1;
		long finishTime = curInst.getFinishTime();
		long startTime = curInst.getStartTime();
		int interval = resources.getInterval();
		double curCost = Math.ceil((double) (finishTime - startTime) / (double) interval) * curInst.getType().getCost();
		long curIntervalFinish = startTime
				+ (long) Math.ceil((double) (finishTime - startTime) / (double) interval) * interval;
		int start, curStart = (int) finishTime, curFinish;
		curStart = (int) GetMyFileTransferTime(curStart, curNode, curInst);

		if (finishTime == 0)
			startTime = curStart;
		long computation = Math.round(
				(float) curNode.getInstructionSize() / (curInst.getType().getMIPS() * curInst.getFrequencyLevel()));
		float fluct = calcFluctuation(computation);
		computation = computation + Math.round(fluct);
		// long energy = Math.round(Math.pow(curInst.getFrequencyLevel(), 2) *
		// computation);
		// float reliability = (float) Math.exp(-1 * curInst.getType().getFaultRate()
		// * Math.pow(10, (fq * (1 - curInst.getFrequencyLevel()) / (1 -
		// WorkflowBroker.minFrequency)))
		// * (computation / curInst.getFrequencyLevel()));
		float freq = curInst.getType().getMaxFrequencyOr1();
		double y = curInst.getType().getFaultRate();
		long energy = calcEnergy((int) computation, freq, curInst); // Math

		float reliability = calcReliability(0, (int) computation, freq, curInst);

		result r = new result();
		if (curInst.getFinishTime() == 0) {
			computation += configuration.getProvisioningDelay();
		}

		curFinish = (int) (curStart + computation);
		r.freqLevel = 1;
		r.freq = freq;
		r.finishTime = curFinish;
		r.energy = energy;
		r.reliability = reliability;
		r.cost = (float) (Math.ceil((double) (curFinish - startTime) / (double) interval) * curInst.getType().getCost()
				- curCost);
		r.startTime = curStart;
		return (r);
	}

	// protected float getCostCalc(long finish, long start, int interval, float
	// cost, float frequency)
	// {
	// float cost2 = (float) (Math.ceil((double) (finish - start) / (double)
	// interval) * curInst.getType().getCost()
	// - curCost);
	// }

	public result checkInstanceGapPro(WorkflowNode curNode, Instance curInst, long subDl) {
		long finishTime = curInst.getFinishTime();
		long startTime = curInst.getStartTime();

		int interval = resources.getInterval();

		long start, curStart = finishTime, curFinish;
		curStart = 0;
		curStart = (int) GetMyFileTransferTime(curStart, curNode, curInst);

		long computation = Math.round(
				(float) curNode.getInstructionSize() / (curInst.getType().getMIPS() * curInst.getFrequencyLevel()));
		float fluct = calcFluctuation(computation);
		computation = computation + Math.round(fluct);
		if (curInst.getFinishTime() == 0) {
			computation += configuration.getProvisioningDelay();
		}

		curFinish = (int) (curStart + computation);

		result rg = curInst.canFitGapPro(curStart, subDl, computation);
		if (curInst.getFinishTime() < curStart || rg.finishTime == -1) {
			return checkInstance(curNode, curInst);
		}

		if (rg.finishTime > -1) {

		}
		if (finishTime == 0)
			startTime = curStart;
		float freq = curInst.getType().getMaxFrequencyOr1();
		double y = curInst.getType().getFaultRate();
		long energy = calcEnergy((int) computation, freq, curInst);

		float reliability = calcReliability(0, (int) computation, freq, curInst);

		result r = new result();
		r.startTime = rg.startTime;
		r.finishTime = rg.finishTime;
		r.gapFitpercent = rg.gapFitpercent;
		r.gapid = rg.gapid;
//		r.startTime=(int) curStart;
//		r.finishTime = (int) curFinish;
		r.energy = energy;
		r.reliability = reliability;
//		r.cost = (float) (Math.ceil((double) (curFinish - startTime) / (double) interval) * curInst.getType().getCost()
//				- curCost);
		r.cost = 0;
		r.inplace = true;
		r.freq = freq;
		r.freqLevel = 1;
		return (r);

	}

	public result checkInstanceGap(WorkflowNode curNode, Instance curInst) {
		long finishTime = curInst.getFinishTime();
		long startTime = curInst.getStartTime();
		long estt, lftt = 0;
		int interval = resources.getInterval();
		double curCost = Math.ceil((double) (finishTime - startTime) / (double) interval) * curInst.getType().getCost();
		long curIntervalFinish = startTime
				+ (long) Math.ceil((double) (finishTime - startTime) / (double) interval) * interval;
		long start, curStart = finishTime, curFinish;
		curStart = 0;
		curStart = (int) GetMyFileTransferTime(curStart, curNode, curInst);

		long computation = Math.round(
				(float) curNode.getInstructionSize() / (curInst.getType().getMIPS() * curInst.getFrequencyLevel()));

		if (curInst.getFinishTime() == 0) {
			computation += configuration.getProvisioningDelay();
		}
		curFinish = (int) (curStart + computation);

		int canFit = curInst.canFitGap(curStart, curFinish);
		if (curInst.getFinishTime() < curStart || canFit == -1) {
			return checkInstance(curNode, curInst);
		}

		if (finishTime == 0)
			startTime = curStart;

		double y = curInst.getType().getFaultRate();
		float freq = curInst.getType().getMaxFrequencyOr1();
		long energy = calcEnergy((int) computation, freq, curInst);

		float reliability = calcReliability(0, (int) computation, freq, curInst);

		result r = new result();
		r.startTime = (int) curStart;
		r.gapid = canFit;
		r.finishTime = (int) curFinish;
		r.energy = energy;
		r.reliability = reliability;
//		r.cost = (float) (Math.ceil((double) (curFinish - startTime) / (double) interval) * curInst.getType().getCost()
//				- curCost);
		r.cost = 0;
		r.inplace = true;
		r.freq = freq;
		r.freqLevel = 1;
		return (r);
	}

	public result checkInstanceGapFreq(WorkflowNode curNode, Instance curInst, float freq) {
		float freqLevel = curInst.getFrequencyLevel(freq);

		long finishTime = curInst.getFinishTime();
		long startTime = curInst.getStartTime();
		long estt, lftt = 0;
		int interval = resources.getInterval();
		double curCost = Math.ceil((double) (finishTime - startTime) / (double) interval) * curInst.getType().getCost();
		long curIntervalFinish = startTime
				+ (long) Math.ceil((double) (finishTime - startTime) / (double) interval) * interval;
		long start, curStart = finishTime, curFinish;
		curStart = 0;
		curStart = (int) GetMyFileTransferTime(curStart, curNode, curInst);
		long computation = Math.round((float) curNode.getInstructionSize() / (curInst.getType().getMIPS() * freqLevel));

		if (curInst.getFinishTime() == 0) {
			computation += configuration.getProvisioningDelay();
		}

		curFinish = (int) (curStart + computation);

		int canFit = curInst.canFitGap(curStart, curFinish);
		if (curInst.getFinishTime() < curStart || canFit == -1) {
			return checkInstance(curNode, curInst);
		}

		if (finishTime == 0)
			startTime = curStart;

		double y = curInst.getType().getFaultRate();
		long energy = calcEnergy((int) computation, freq, curInst);

		float reliability = calcReliability(0, (int) computation, freq, curInst);

		result r = new result();
		r.gapid = canFit;
		r.startTime = (int) curStart;
		r.finishTime = (int) curFinish;
		r.energy = energy;
		r.reliability = reliability;

		r.freq = freq;
		r.freqLevel = freqLevel;
		r.cost = 0;
		r.inplace = true;
		return (r);

	}

	public result checkInstanceGapFreqPro(WorkflowNode curNode, Instance curInst, long subDl, float freq) {

		float freqLevel = curInst.getFrequencyLevel(freq);

		long finishTime = curInst.getFinishTime();
		long startTime = curInst.getStartTime();
		long estt, lftt = 0;
		int interval = resources.getInterval();
		double curCost = Math.ceil((double) (finishTime - startTime) / (double) interval) * curInst.getType().getCost();
		long curIntervalFinish = startTime
				+ (long) Math.ceil((double) (finishTime - startTime) / (double) interval) * interval;
		long start, curStart = finishTime, curFinish;
		curStart = 0;
		curStart = (int) GetMyFileTransferTime(curStart, curNode, curInst);

		long computation = Math.round((float) curNode.getInstructionSize() / (curInst.getType().getMIPS() * freqLevel));

		float fluct = calcFluctuation(computation);
		computation = computation + Math.round(fluct);

		if (curInst.getFinishTime() == 0) {
			computation += configuration.getProvisioningDelay();
		}
		curFinish = (int) (curStart + computation);

		result rg = curInst.canFitGapPro(curStart, subDl, computation);
		if (curInst.getFinishTime() < curStart || rg.finishTime == -1) {
			return checkInstanceEFrequency(curNode, curInst, freq);
		}

		if (finishTime == 0)
			startTime = curStart;

		double y = curInst.getType().getFaultRate();
		long energy = calcEnergy((int) computation, freq, curInst);

		float reliability = calcReliability(0, (int) computation, freq, curInst);

		result r = new result();
		r.startTime = rg.startTime;
		r.finishTime = rg.finishTime;
		r.gapFitpercent = rg.gapFitpercent;
		r.gapid = rg.gapid;
//		r.startTime=(int) curStart;
//		r.finishTime = (int) curFinish;
		r.energy = energy;
		r.reliability = reliability;
//		r.cost = (float) (Math.ceil((double) (curFinish - startTime) / (double) interval) * curInst.getType().getCost()
//				- curCost);
		r.cost = 0;
		r.inplace = true;
		r.freq = freq;
		r.freqLevel = freqLevel;
		return (r);

	}

	protected result checkInstanceE(WorkflowNode curNode, Instance curInst) {

		long finishTime = curInst.getFinishTime();
		long startTime = curInst.getStartTime();
		int interval = resources.getInterval();
		double curCost = Math.ceil((double) (finishTime - startTime) / (double) interval) * curInst.getType().getCost();
		long curIntervalFinish = startTime
				+ (long) Math.ceil((double) (finishTime - startTime) / (double) interval) * interval;
		int start, curStart = (int) finishTime, curFinish;

		curStart = (int) GetMyFileTransferTime(curStart, curNode, curInst);
		float freq = curInst.getType().getMaxFrequencyOr1();
		if (finishTime == 0)
			startTime = curStart;
		long computation = Math.round((float) curNode.getInstructionSize() / (curInst.getType().getMIPS()));
		float fluct = calcFluctuation(computation);
		computation = computation;// + Math.round(fluct);
		double y = curInst.getType().getFaultRate();

		long energy = calcEnergy((int) computation, freq, curInst);
		float reliability = calcReliability(0, (int) computation, freq, curInst); // (float) Math
//				.exp(-1 * y * Math.pow(10, (fq * (1 - curInst.getFrequencyLevel()) / (1 - WorkflowBroker.minFrequency)))
//						* (computation / curInst.getFrequencyLevel()));

		if (curInst.getFinishTime() == 0) {
			computation += configuration.getProvisioningDelay();
		}
		result r = new result();
		curFinish = (int) (curStart + computation);
		r.finishTime = curFinish;
		r.energy = energy;
		r.reliability = reliability;
		r.cost = (float) (Math.ceil((double) (curFinish - startTime) / (double) interval) * curInst.getType().getCost()
				- curCost);
		r.startTime = curStart;

		r.freqLevel = 1;
		r.freq = freq;
		return (r);
	}

	protected result checkInstanceEFrequency(WorkflowNode curNode, Instance curInst, float freq) {
		float freqLevel = curInst.getFrequencyLevel(freq);

		long finishTime = curInst.getFinishTime();
		long startTime = curInst.getStartTime();
		int interval = resources.getInterval();
		double curCost = Math.ceil((double) (finishTime - startTime) / (double) interval) * curInst.getType().getCost();
		long curIntervalFinish = startTime
				+ (long) Math.ceil((double) (finishTime - startTime) / (double) interval) * interval;
		int start, curStart = (int) finishTime, curFinish;
		curStart = (int) GetMyFileTransferTime(curStart, curNode, curInst);

		if (finishTime == 0)
			startTime = curStart;
		long computation = Math.round((float) curNode.getInstructionSize() / (curInst.getType().getMIPS() * freqLevel));
		float fluct = calcFluctuation(computation);
		computation = computation + Math.round(fluct);

		double y = curInst.getType().getFaultRate();
		long energy = calcEnergy((int) computation, freq, curInst);
		float reliability = calcReliability(0, (int) computation, freq, curInst);

		if (curInst.getFinishTime() == 0) {
			computation += configuration.getProvisioningDelay();
		}

		result r = new result();
		curFinish = (int) (curStart + computation);
		r.finishTime = curFinish;
		r.startTime = curStart;
		r.energy = energy;
		r.reliability = reliability;
		r.cost = (float) (Math.ceil((double) (curFinish - startTime) / (double) interval) * curInst.getType().getCost()
				- curCost);
		r.freq = freq;
		r.freqLevel = freqLevel;
		return (r);
	}

	protected int getLevelId(float startTime, int listCount) {
		int interval = this.resources.getInterval();
		int rst = ((int) Math.floor((startTime) / interval));
		if (rst == listCount)
			rst--;
		if (rst > listCount)
			rst = listCount - 1;
		return rst;
	}
}
