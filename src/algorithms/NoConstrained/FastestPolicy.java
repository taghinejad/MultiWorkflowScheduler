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

public class FastestPolicy extends WorkflowPolicy {
	public FastestPolicy(WorkflowGraph g, ResourceSet rs, long bw) {
		super(g,rs,bw);
	}
	public float schedule(int startTime, int deadline, float cost) {
		// TODO Auto-generated method stub
		return 0;
	}
	public float schedule(int[] startTime, int []deadline, float cost) {
		return 0;
	}
	
	public float schedule(int startTime, int deadline) {
		Queue<String> candidateNodes = new LinkedList<String>() ;
		Map<String, WorkflowNode> nodes = graph.getNodes() ;
		WorkflowNode curNode,parentNode, childNode;
		
		setRuntimes();

		curNode = nodes.get(graph.getStartId()) ;
		curNode.setAST(startTime) ;
		curNode.setAFT(startTime) ;
		curNode.setScheduled() ;
		for (Link child : curNode.getChildren()) 
			candidateNodes.add(child.getId()) ;
		
		while (!candidateNodes.isEmpty()) {
			int thisTime, maxTime ;
			curNode = nodes.get(candidateNodes.remove()) ;
			maxTime = -1;
			for (Link parent : curNode.getParents()) {
				parentNode = nodes.get(parent.getId()) ;
				thisTime = parentNode.getAFT() + Math.round((float)parent.getDataSize() / bandwidth);  
				if (thisTime > maxTime)
					maxTime = thisTime ;
			}
			curNode.setAST(maxTime); 
			curNode.setAFT(maxTime+curNode.getRunTime());
			curNode.setScheduled() ;
			curNode.setSelectedResource(0) ;
			
			for (Link child: curNode.getChildren()) {
				boolean isCandidate = true;
				childNode = nodes.get(child.getId()) ;
				for (Link parent: childNode.getParents()) 
					if (!nodes.get(parent.getId()).isScheduled())
						isCandidate = false;
				if (isCandidate)
					candidateNodes.add(child.getId()) ;
			}
		}
		Resource rs=resources.getMaxResource();
		long last=graph.getNodes().get(graph.getEndId()).getAST();
		long start=graph.getNodes().get(graph.getStartId()).getAST();
		float cost = (float) Math.ceil((last - start)/(float)resources.getInterval() )* rs.getCost();
		// (float) Math.ceil((cur.getFinishTime() - cur.getStartTime()) / (double) interval)
	
		super.setEndNodeAST();
		//return(0);
		//ahmad
		return cost;
	}
	@Override
	public float schedule(List<Long> startTime, List<Long> deadline, float cost) {
		// TODO Auto-generated method stub
		return 0;
	}
}
