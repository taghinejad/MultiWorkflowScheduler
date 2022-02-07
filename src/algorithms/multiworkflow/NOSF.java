//Liu J, Ren J, Dai W, et al. Online Multi-Workflow Scheduling under Uncertain Task Execution Time in IaaS Clouds. IEEE Trans Cloud Comput. 2019;PP(c):1. doi:10.1109/TCC.2019.2906300

//This workflow can run on un
package algorithms.multiworkflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

import Broker.Instance;
import Broker.execution;
import Broker.Link;
import Broker.ResourceSet;
import Broker.WorkflowGraph;
import Broker.WorkflowNode;
import Broker.WorkflowPolicy;
import Broker.result;
import Broker.WorkflowPolicy.EarliestStartTime;
import utility.SimulationResults;

public class NOSF extends WorkflowPolicy {
	boolean backCheck = true;
	static int count = 0;
	public int NodeSizes;
	public static result OverallResults;

	public NOSF(WorkflowGraph[] g, ResourceSet rs, long bw) {
		super(g, rs, bw);
		for (WorkflowGraph workflowGraph : graphs) {
			NodeSizes += workflowGraph.nodes.size();
		}
	}

	List<Long> TotalDeadline;

	public float schedule(List<Long> startTime, List<Long> deadline, float cost) {
		currentAlgrotihm = "NOSF multi-workflows";
		OverallResults = new result();
		OverallResults.algorithmName = currentAlgrotihm;
		this.TotalDeadline = deadline;
		long alStart = 0, algEnd = 0, algEnd2 = 0, algEnd3 = 0, algEnd4 = 0, algEnd5 = 0;
		alStart = System.nanoTime();

		for (int i = 0; i < graphs.length; i++) {

			setRuntimesStochastic(graphs[i]);
			this.graph = graphs[i];

			computeESTandEFT2(graphs[i], startTime.get(i).intValue());
			computeLSTandLFT(graphs[i], deadline.get(i).intValue());
			initializeStartEndNodes(graphs[i], startTime.get(i), deadline.get(i));

			distributeDeadline(graph);

			planning(graph);

			setEndNodeEST(graph);

		}

		for (WorkflowGraph dag : graphs) {
			setEndNodeEST(dag);
		}
		cost = super.computeFinalCost();
		OverallResults.totalEnergy = super.computeFinalEnergy();

		System.out.print("\n NOSF ");

		OverallResults.cost = cost;
		int finishTime = 0;
		// to do find the finish time based on
		OverallResults.finishTimes = new ArrayList<Integer>();
		OverallResults.deadlines = this.TotalDeadline;
		OverallResults.starts = startTime;

		for (WorkflowGraph workflowGraph : graphs) {

			finishTime = workflowGraph.getNodes().get(workflowGraph.getEndId()).getEST();
			if (OverallResults.finishTime < finishTime) {
				OverallResults.finishTime = finishTime;
			}
			OverallResults.finishTimes.add(finishTime);

		}
		algEnd2 = System.nanoTime() - alStart;
		System.out.println(" \n nosf altimer: planning:" + algEnd2 / 1000000000);

		return (cost);

	}

	public float schedule(int startTime, int deadline) {
		currentAlgrotihm = "NOSF";
		float cost;

		setRuntimes();
		computeESTandEFT(startTime);
		computeLSTandLFT(deadline);
		initializeStartEndNodes(startTime, deadline);

		distributeDeadline(graph);
		planning(graph);

		setEndNodeEST();
		cost = super.computeFinalCost();
		return (cost);
	}

	private void distributeDeadline(WorkflowGraph graph) {
		assignParents(graph.getNodes().get(graph.getEndId()));
		for (WorkflowNode node : graph.getNodes().values())
			node.setUnscheduled();
	}

	private void assignParents(WorkflowNode curNode) {
		List<WorkflowNode> criticalPath;

		criticalPath = findPartialCriticalPath(curNode);
		if (criticalPath.isEmpty())
			return;

		assignPath(criticalPath);
		for (int i = 0; i < criticalPath.size(); i++) {
			updateChildrenEST(criticalPath.get(i));
			updateParentsLFT(criticalPath.get(i));
		}
		for (int i = 0; i < criticalPath.size(); i++)
			assignParents(criticalPath.get(i));

		assignParents(curNode);
	}

	private void assignPath(List<WorkflowNode> path) {
		int last = path.size() - 1;
		int pathEST = path.get(0).getEST();
		int pathEFT = path.get(last).getEFT();
		int PSD = path.get(last).getLFT() - pathEST;

		for (int i = 0; i <= last; i++) {
			WorkflowNode curNode = path.get(i);
			int subDeadline = pathEST
					+ (int) Math.floor((float) (curNode.getEFT() - pathEST) / (float) (pathEFT - pathEST) * PSD);

			curNode.setDeadline(subDeadline);
			curNode.setScheduled();

			if (i > 0) {
				int newEST = Math.round(path.get(i - 1).getDeadline())
						+ Math.round((float) getDataSize(path.get(i - 1), curNode) / bandwidth);
				if (newEST > curNode.getEST())
					curNode.setEST(newEST);
			}
		}
	}

	protected void updateChildrenEST(WorkflowNode parentNode) {
		for (Link child : parentNode.getChildren()) {
			WorkflowNode childNode = graph.getNodes().get(child.getId());
			int newEST;

			if (!childNode.isScheduled()) {
				if (parentNode.isScheduled())
					newEST = Math.round(parentNode.getDeadline()) + Math.round((float) child.getDataSize() / bandwidth);
				else
					newEST = parentNode.getEFT() + Math.round((float) child.getDataSize() / bandwidth);

				if (childNode.getEST() < newEST) {
					childNode.setEST(newEST);
					childNode.setEFT(newEST + childNode.getRunTime());
					updateChildrenEST(childNode);
				}
			}
		}
	}

	protected void updateParentsLFT(WorkflowNode childNode) {
		for (Link parent : childNode.getParents()) {
			WorkflowNode parentNode = graph.getNodes().get(parent.getId());
			int newLFT;

			if (!parentNode.isScheduled()) {
				if (childNode.isScheduled())
					newLFT = childNode.getEST() - Math.round((float) parent.getDataSize() / bandwidth);
				else
					newLFT = childNode.getLST() - Math.round((float) parent.getDataSize() / bandwidth);

				if (parentNode.getLFT() > newLFT) {
					parentNode.setLFT(newLFT);
					parentNode.setLST(newLFT - parentNode.getRunTime());
					updateParentsLFT(parentNode);
				}
			}
		}
	}

	protected WorkflowNode findCriticalParent(WorkflowNode child) {
		WorkflowNode criticalPar = null;
		int criticalParStart = -1, curStart;

		for (Link parentLink : child.getParents()) {
			WorkflowNode parentNode = graph.getNodes().get(parentLink.getId());
			if (parentNode.isScheduled())
				continue;

			curStart = parentNode.getEFT() + Math.round((float) parentLink.getDataSize() / bandwidth);
			if (curStart > criticalParStart) {
				criticalParStart = curStart;
				criticalPar = parentNode;
			}
		}
		return (criticalPar);
	}

	protected List<WorkflowNode> findPartialCriticalPath(WorkflowNode curNode) {
		List<WorkflowNode> criticalPath = new ArrayList<WorkflowNode>();

		do {
			curNode = findCriticalParent(curNode);
			if (curNode != null)
				criticalPath.add(0, curNode);
		} while (curNode != null);
		return (criticalPath);
	}

	private void planning(WorkflowGraph graph) {
		PriorityQueue<WorkflowNode> queue = new PriorityQueue<WorkflowNode>(graph.nodes.size(),
				new WorkflowPolicy.EarliestStartTime());
		result r;
		List<result> rlist = new ArrayList<result>();

		int bestFinish = Integer.MAX_VALUE;

		computeUpRank();
		for (WorkflowNode node : graph.nodes.values())
			if (!node.getId().equals(graph.getStartId()) && !node.getId().equals(graph.getEndId()))
				queue.add(node);

		while (!queue.isEmpty()) {
			WorkflowNode curNode = queue.remove();

			int bestInst = -1;
			float bestCost = Float.MAX_VALUE;
String ss="";
			for (int curInst = 0; curInst < instances.getSize(); curInst++) {
				r = checkInstance2(curNode, instances.getInstance(curInst));
				if (r.cost < bestCost) {
					bestCost = r.cost;
					bestFinish = r.finishTime;
					bestInst = curInst;
					r.instId = curInst;
					rlist.add(r);
				}

				else if (bestCost < Float.MAX_VALUE && r.cost == bestCost && r.finishTime < bestFinish) {
					bestFinish = r.finishTime;
					bestInst = curInst;
				}
			}
			int minUsage = Integer.MAX_VALUE*-1;
			if (rlist.size() > 0) {
				for (result res : rlist) {
					if (res.usage > minUsage) {
						bestInst = res.instId;
					}
				}
			}
			if (bestInst == -1)
				for (int curRes = resources.getSize() - 1; curRes >= 0; curRes--) { // because the cheapest one is the
																					// last
					Instance inst = new Instance(instances.getSize(), resources.getResource(curRes));
					r = checkInstance2(curNode, inst);
					if (r.cost < Float.MAX_VALUE) {
						instances.addInstance(inst);
						bestInst = inst.getId();
						ss="resourses";
						break;
						
					}
				}
			rlist.clear();
			if (bestInst == -1) {
				bestInst = getFastestInstanceIndex2(curNode);
				ss="fasterst:"+ bestInst;
			}

			result rassigned;
			if (bestInst < 50000) {
				if (instances.getInstance(bestInst).getType()==null){
					System.out.println();
				}
				setInstanceE(curNode, instances.getInstance(bestInst));
			} else {
				bestInst -= 50000;
				Instance inst = new Instance(instances.getSize(), resources.getResource(bestInst));
				instances.addInstance(inst);
				if (inst.getType()==null){
					System.out.println();
				}
				setInstanceE(curNode, inst);
			}

		}
	}

	private result checkInstance2(WorkflowNode curNode, Instance curInst) {
		long finishTime = curInst.getFinishTime();
		long startTime = curInst.getStartTime();
		int interval = resources.getInterval();
		double curCost = Math.ceil((double) (finishTime - startTime) / (double) interval) * curInst.getType().getCost();
		long curIntervalFinish = startTime
				+ (long) Math.ceil((double) (finishTime - startTime) / (double) interval) * interval;
		int start, curStart = (int) finishTime, curFinish = 0;

		for (Link parent : curNode.getParents()) {
			WorkflowNode parentNode = graph.getNodes().get(parent.getId());

			start = parentNode.getEFT();
			if (parentNode.getSelectedResource() != curInst.getId())
				start += Math.round((float) parent.getDataSize() / bandwidth);
			if (start > curStart)
				curStart = start;
		}

		if (finishTime == 0)
			startTime = curStart;

		result r = new result();
		r.usage = (int) (curIntervalFinish - finishTime);
		float ex = executionPredictionBasedOnNOSF((float) curNode.getInstructionSize() / curInst.getType().getMIPS());
		curFinish = curStart + Math.round(ex);
		r.finishTime = curFinish;
		if ((finishTime != 0 && curStart > curIntervalFinish) || curFinish > curNode.getDeadline())
			r.cost = Float.MAX_VALUE;
		else
			r.cost = (float) (Math.ceil((double) (curFinish - startTime) / (double) interval)
					* curInst.getType().getCost() - curCost);

		return (r);
	}

	private float executionPredictionBasedOnNOSF(double runningTime) {
			return (float) ( (runningTime + fluctpercent*calcFluctuation(runningTime)));
		//return (float) ((float) runningTime+Math.sqrt(calcFluctuation(runningTime)));
	}



	private void setInstance(WorkflowNode curNode, Instance curInst) {
		int start, curStart = (int) curInst.getFinishTime(), curFinish;

		for (Link parent : curNode.getParents()) {
			WorkflowNode parentNode = graph.getNodes().get(parent.getId());

			start = parentNode.getEFT();
			if (parentNode.getSelectedResource() != curInst.getId())
				start += Math.round((float) parent.getDataSize() / bandwidth);
			if (start > curStart)
				curStart = start;
		}

		curFinish = curStart + Math.round((float) curNode.getInstructionSize() / curInst.getType().getMIPS());
		curNode.setEST(curStart);
		curNode.setEFT(curFinish);
		curNode.setSelectedResource(curInst.getId());
		curNode.setScheduled();

		if (curInst.getFinishTime() == 0) {
			curInst.setStartTime(curStart);
			curInst.setFirstTask(curNode.getId());
		}
		curInst.setFinishTime(curFinish);
		curInst.setLastTask(curNode.getId());
	}

	@Override
	public float schedule(int startTime, int deadline, float cost) {
		// TODO Auto-generated method stub
		return 0;
	}

}
