package algorithms.NoConstrained;

import java.util.ArrayList;
import java.util.List;

import Broker.Instance;
import Broker.Link;
import Broker.ResourceSet;
import Broker.WorkflowGraph;
import Broker.WorkflowNode;
import Broker.WorkflowPolicy;
import Broker.result;

public class CheapestCP extends WorkflowPolicy {
	boolean backCheck = true;

	public CheapestCP(WorkflowGraph g, ResourceSet rs, long bw) {
		super(g, rs, bw);

	}


	private result assignParents(WorkflowNode curNode) {
		List<WorkflowNode> criticalPath;

		criticalPath = findPartialCriticalPath(curNode);
		Instance inst=new Instance(1, resources.getMinResource());
		return mycheckInstance(criticalPath, inst);
		
	}
	public float schedule(int[] startTime, int []deadline, float cost) {
		return 0;
	}
	
	private result mycheckInstance(List<Broker.WorkflowNode> path, Instance curInst) {
		long finishTime = curInst.getFinishTime();
		long startTime = curInst.getStartTime();
		int interval = resources.getInterval();
		double curCost = Math.ceil((double) (finishTime - startTime) / (double) interval) * curInst.getType().getCost();
		long curIntervalFinish = startTime
				+ (long) Math.ceil((double) (finishTime - startTime) / (double) interval) * interval;
		long max,selftranfer;
		Broker.WorkflowNode curNode = path.get(0);
		for (int i = 0; i < path.size(); i++) {
			max = 0;
			curNode = path.get(i);
			finishTime += GetMyFileTransferTime(0, curNode, curInst);
			finishTime += Math.round((float) curNode.getInstructionSize() / curInst.getType().getMIPS());
		//	curTime += Math.round((float) curNode.getInstructionSize() / inst.getType().getMIPS());
		}
		result r = new result();
		r.finishTime = (int) finishTime;
		// if ((finishTime != 0 && curStart > curIntervalFinish) || curFinish >
		// curNode.getLFT()) // difference with PCPD2
		// r.cost = Float.MAX_VALUE;
		// else
		r.cost = (float) (Math.ceil((double) (finishTime - startTime) / (double) interval) * curInst.getType().getCost()
				- curCost);

		return (r);
	}
	

	protected WorkflowNode findCriticalParent(Broker.WorkflowNode child) {
		Broker.WorkflowNode criticalPar = null;
		int criticalParStart = -1, curStart;

		for (Link parentLink : child.getParents()) {
			Broker.WorkflowNode parentNode = graph.getNodes().get(parentLink.getId());
			if (parentNode.isScheduled())
				continue;

			curStart = parentNode.getEFT() + Math.round((float) parentLink.getDataSize() / bandwidth);
			if (curStart > criticalParStart) {
				criticalParStart = curStart;
				criticalPar = parentNode;
			}
		}
		// if (criticalPar != null)
		// if (graph.nodes.get(child.getId()).getCriticalParent() == null)
		// graph.nodes.get(child.getId()).setCriticalParent(criticalPar);
		return (criticalPar);
	}

	protected WorkflowNode SetCriticalParent(Broker.WorkflowNode child) {
		Broker.WorkflowNode criticalPar = null;
		int criticalParStart = -1, curStart;

		for (Link parentLink : child.getParents()) {
			Broker.WorkflowNode parentNode = graph.getNodes().get(parentLink.getId());
			if (parentNode.isScheduled())
				continue;

			curStart = parentNode.getEFT() + Math.round((float) parentLink.getDataSize() / bandwidth);
			if (curStart > criticalParStart) {
				criticalParStart = curStart;
				criticalPar = parentNode;
			}
		}
		if (criticalPar != null)
			if (graph.nodes.get(child.getId()).getCriticalParent() == null)
				graph.nodes.get(child.getId()).setCriticalParent(criticalPar);
		return (criticalPar);
	}

	protected List<WorkflowNode> findPartialCriticalPath(Broker.WorkflowNode curNode) {
		List<WorkflowNode> criticalPath = new ArrayList<WorkflowNode>();

		do {
			SetCriticalParent(curNode);
			curNode = findCriticalParent(curNode);
			if (curNode != null) {
				criticalPath.add(0, curNode);

			}
		} while (curNode != null);
		return (criticalPath);
	}


	@Override
	public float schedule(int startTime, int deadline, float cost) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float schedule(int startTime, int deadline) {
		float cost;

		setRuntimes();
		computeESTandEFT2(startTime);
		computeLSTandLFT(0);
		initializeStartEndNodes(startTime, 0);
		result rs=assignParents(graph.getNodes().get(graph.getEndId()));
		graph.getNodes().get(graph.getEndId()).setAST((int)rs.finishTime);
		return rs.cost;
	}


	@Override
	public float schedule(List<Long> startTime, List<Long> deadline, float cost) {
		// TODO Auto-generated method stub
		return 0;
	}

}
