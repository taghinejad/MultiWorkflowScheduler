package algorithms.NoConstrained;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import Broker.Link;
import Broker.Resource;
import Broker.ResourceSet;
import Broker.WorkflowGraph;
import Broker.WorkflowNode;
import Broker.WorkflowPolicy;
import algorithms.*;
import utility.SimulationResults;

public class MyFast extends WorkflowPolicy {
	public MyFast(WorkflowGraph g, ResourceSet rs, long bw) {
		super(g, rs, bw);
	}
	public float schedule(int startTime, int deadline, float cost) {
		// TODO Auto-generated method stub
		return 0;
	}
	public float schedule(int[] startTime, int []deadline, float cost) {
		return 0;
	}
	
	public float schedule(int startTime, int deadline) {
		Queue<String> candidateNodes = new LinkedList<String>();
		Map<String, WorkflowNode> nodes = graph.getNodes();
		WorkflowNode curNode, parentNode, childNode;
		long data = 0, maxData = 0;
		setRuntimes();
		int currentResourceRunTime = 0;
		curNode = nodes.get(graph.getStartId());
		curNode.setAST(startTime);
		curNode.setAFT(startTime);
		curNode.setScheduled();
		for (Link child : curNode.getChildren())
			candidateNodes.add(child.getId());

		while (!candidateNodes.isEmpty()) {
			int thisTime, maxTime;
			curNode = nodes.get(candidateNodes.remove());
			maxTime = -1;
			// it does not consider resources runtime and just think that resource can run  it
			for (Link parent : curNode.getParents()) {
				parentNode = nodes.get(parent.getId());
				thisTime = parentNode.getAFT(); // + Math.round((float) parent.getDataSize() / bandwidth);
				data = Math.round((float) parent.getDataSize() / bandwidth);
				if (thisTime > maxTime)
					maxTime = thisTime;

				if (data > maxData)
					maxData = data;

			}
			//calculate transferTime WorkflowPolicy.CalculatedTransferTime(WorkflowNode wn, Instance in)
			//maxTime++
			// maxTime+=currentResourceRunTime;

			if (maxTime < currentResourceRunTime)
				maxTime = currentResourceRunTime;
			curNode.setAST(maxTime);
			curNode.setAFT(maxTime + curNode.getRunTime());
			currentResourceRunTime += curNode.getRunTime();
			if (curNode.getId() != graph.getStartId() && curNode.getId() != graph.getEndId()) {
				SimulationResults.computation += (curNode.getInstructionSize() / resources.getMaxMIPS());
				// SimulationResults.computation += curNode.getRunTime();
				SimulationResults.datatransfer += data;
				SimulationResults.runTime += (curNode.getAFT() - curNode.getAST());
				SimulationResults.Tasks += 1;
				SimulationResults.ComputationRate2data = (double) SimulationResults.computation
						/ SimulationResults.datatransfer;
				SimulationResults.DataRate2Comp = SimulationResults.datatransfer
						/ (double) SimulationResults.computation;
			}
			super.setEndNodeAST();

			curNode.setScheduled();
			curNode.setSelectedResource(0);

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
		Resource rs = resources.getMaxResource();
		long last = graph.getNodes().get(graph.getEndId()).getAST();
		long start = graph.getNodes().get(graph.getStartId()).getAST();
		float cost = (float) Math.ceil((last - start) / (float) resources.getInterval()) * rs.getCost();
		// (float) Math.ceil((cur.getFinishTime() - cur.getStartTime()) / (double)
		// interval)

		// return(0);
		// ahmad
		return cost;
	}
	@Override
	public float schedule(List<Long> startTime, List<Long> deadline, float cost) {
		// TODO Auto-generated method stub
		return 0;
	}

}