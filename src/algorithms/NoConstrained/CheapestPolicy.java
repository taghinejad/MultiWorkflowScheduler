package algorithms.NoConstrained;

import java.util.List;
import java.util.Map;

import Broker.Link;
import Broker.ResourceSet;
import Broker.WorkflowGraph;
import Broker.WorkflowNode;
import Broker.WorkflowPolicy;

public class CheapestPolicy extends WorkflowPolicy {
	public CheapestPolicy(WorkflowGraph g, ResourceSet rs, long bw) {
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
	 float minMIPS = resources.getMinMIPS();
	 float minCost = resources.getMinCost(), totalCost;
	 long totalTime = 0;
	
	 for (WorkflowNode curNode : graph.getNodes().values())
	 totalTime += Math.round((float)curNode.getInstructionSize()/minMIPS);
	
	 totalCost =
	 (float)(Math.ceil((double)totalTime/(double)resources.getInterval()) *
	 minCost);
	 graph.getNodes().get(graph.getEndId()).setAST((int)totalTime);
	 return (totalCost);
	 }

	@Override
	public float schedule(List<Long> startTime, List<Long> deadline, float cost) {
		// TODO Auto-generated method stub
		return 0;
	}
	 
//		public float schedule(int startTime, int deadline) {
//		float cost;
//		WorkflowNode parentNode, childNode;
//		setRuntimes();
//		computeESTandEFT2(startTime);
//		computeLSTandLFT(0);
//		initializeStartEndNodes(startTime, 0);
//		Map<String, WorkflowNode> nodes = graph.getNodes();
//		int thisTime, maxTime = 0;
//		float minMIPS = resources.getMinMIPS();
//		float minCost = resources.getMinCost(), totalCost;
//		long totalTime = 0;
//float eachcost=0;
//		for (WorkflowNode curNode : graph.getNodes().values()) {
//
//			totalTime += Math.round((float) curNode.getInstructionSize() / minMIPS);
//			eachcost+=(float)Math.ceil((((float) curNode.getInstructionSize() / minMIPS))/ (double) resources.getInterval())* minCost; 
//		}
//		totalCost = (float) (Math.ceil((double) totalTime / (double) resources.getInterval()) * minCost);
//		graph.getNodes().get(graph.getEndId()).setAST((int) totalTime);
////		return (totalCost);
//		return eachcost;
//	}
}
