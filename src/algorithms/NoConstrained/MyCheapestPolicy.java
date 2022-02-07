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

public class MyCheapestPolicy extends WorkflowPolicy {
	public MyCheapestPolicy(WorkflowGraph g, ResourceSet rs, long bw) {
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
		//in this policy we sum each task running cost independently. 
		float cost;
		initializeStartEndNodes(startTime, 0);
	
		int thisTime, maxTime = 0;
		float minMIPS = resources.getMinMIPS();
		float minCost = resources.getMinCost(), totalCost;
		long totalTime = 0;
		float eachcost = 0;
		for (WorkflowNode curNode : graph.getNodes().values()) {

			totalTime += Math.round((float) curNode.getInstructionSize() / minMIPS);
			eachcost += (float) Math
					.ceil((((float) curNode.getInstructionSize() / minMIPS)) / (double) resources.getInterval())
					* minCost;
		}
		totalCost = (float) (Math.ceil((double) totalTime / (double) resources.getInterval()) * minCost);
		graph.getNodes().get(graph.getEndId()).setAST((int) totalTime);
		// return (totalCost);
		return eachcost;
	}

	@Override
	public float schedule(List<Long> startTime, List<Long> deadline, float cost) {
		// TODO Auto-generated method stub
		return 0;
	}

}
