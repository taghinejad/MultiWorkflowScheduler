package algorithms.multiworkflow;
// My CUSF algorithm. 

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import Broker.Instance;
import Broker.Link;
import Broker.ResourceSet;
import Broker.WorkflowBroker;
import Broker.WorkflowGraph;
import Broker.WorkflowNode;
import Broker.WorkflowPolicy;
import Broker.result;

import utility.PriorityNodeList;

public class CUSF extends WorkflowPolicy {
	boolean backCheck = true;
	boolean justRemainingBudget = false;
	public int NodeSizes;
	public static boolean DVFSenabled = false;
	public static boolean GAPfit = true;
	public static boolean FullFrequency = false;
	public static double averageRunTime = 0;
	public static double edges = 0;
	// DVFS : Dynamic Voltage Frequency Scaling
	// fq is Frequncy Impact factor.
	float fq = 1;
	public static result OverallResults;
	float[] BudgetLevel;
	private float CostImpact = (float) 1, EnergyImpact = (float) 0, TimeImpact = (float) 0;
	public Map<String, List<levelNodes>> levelDag;

	public CUSF(WorkflowGraph[] g, ResourceSet rs, long bw) {
		super(g, rs, bw);
		levelDag = new HashMap<String, List<levelNodes>>();

		for (WorkflowGraph workflowGraph : graphs) {
			NodeSizes += workflowGraph.nodes.size();
		}
	}

	List<levelNodes> lvlList = new ArrayList<levelNodes>();
	float alphaDeadline = 0;
	double DeadlineFactor = 0;
	List<Long> TotalDeadline;
	List<Long> TotalStartTimes;
	float TotalCost;
	float remaingCost = 0;
	int redistributionCount = 0;
	long alStart = 0, algEnd = 0, algEnd2 = 0, algEnd3 = 0, algEnd4 = 0, algEnd5 = 0;

	public float schedule(int startTime, int deadline, float cost) {
		return 0;
	}

	public float schedule(List<Long> startTimes, List<Long> deadlines, float cost) {
		// TODO Auto-generated method stub
		currentAlgrotihm = "CUSF multi-workflows";

		OverallResults = new result();
		OverallResults.algorithmName = currentAlgrotihm;
		this.TotalDeadline = deadlines;
		this.TotalStartTimes = startTimes;
		this.TotalCost = cost;
		this.remaingCost = cost;

		alStart = System.nanoTime();

		for (int i = 0; i < graphs.length; i++) {
			setRuntimesStochastic2(graphs[i]);
			this.graph = graphs[i];
			computeESTandEFT2(graphs[i], startTimes.get(i).intValue());
			computeLSTandLFT(graphs[i], deadlines.get(i).intValue());
			initializeStartEndNodes(graphs[i], startTimes.get(i), deadlines.get(i));
			calculateLevelLists(graphs[i], cost);
			computeECT_SubDeadlineFluct(graphs[i], i);

		}
		algEnd = System.nanoTime() - alStart;

		Boolean r = planning();

		if (!r)
			return -1;
		for (WorkflowGraph dag : graphs) {
			setEndNodeEST(dag);
		}
		cost = super.computeFinalCost();
		OverallResults.totalEnergy = super.computeFinalEnergy();
		String strDVFS = "nonDVFS";
		if (CUSF.DVFSenabled)
			strDVFS = "-DVFS";

		if (CUSF.GAPfit)
			strDVFS += "-Gap";
		else
			strDVFS += "-NotGap";
		if (CUSF.FullFrequency)
			strDVFS += "-Full";

		System.out.print("\n CUSF " + strDVFS + " rdis:" + redistributionCount);

		OverallResults.cost = cost;

		OverallResults.finishTimes = new ArrayList<Integer>();
		OverallResults.deadlines = this.TotalDeadline;
		OverallResults.starts = startTimes;
		int finishTime = 0;
		// to do find the finish time based on
		for (WorkflowGraph workflowGraph : graphs) {

			finishTime = workflowGraph.getNodes().get(workflowGraph.getEndId()).getEST();
			if (OverallResults.finishTime < finishTime) {
				OverallResults.finishTime = finishTime;
			}
			OverallResults.finishTimes.add(finishTime);

		}
		algEnd4 = System.nanoTime() - alStart - algEnd3;
		algEnd5 = System.nanoTime() - alStart;
		System.out.println(" \n altimer: precompile:" + algEnd / 1000000000 + " leveling:" + algEnd2 / 1000000000
				+ " dl dis:" + algEnd3 / 1000000000 + " planning:" + algEnd4 / 1000000000 + " after:"
				+ algEnd5 / 1000000000);
		return (cost);

	}

	public float schedule(int[] startTimes, int[] deadlines, float cost) {
		return 0;
	}


	public float schedule(int startTime, int deadline) {
		return 0;
	}

	// inserts nodes to level;
	public void insertLevel(int level, WorkflowNode wn) {

		for (int i = 0; i < lvlList.size(); i++) {
			if (level == lvlList.get(i).levelid) {
				lvlList.get(i).lvlNodes.add(wn);
				return;
			}
		}
		levelNodes ln = new levelNodes(level, wn);

		lvlList.add(ln);

	}

	protected void computeECT_SubDeadlineFluct(WorkflowGraph graph, int index) {
		Map<String, WorkflowNode> nodes = graph.getNodes();
		int fluct = 0;
		float alpha, alph = 0;
		long sum = 0,run=0;
		edges=0;
		long datasize=0;
		for (int i = 0; i < lvlList.size(); i++) {
			for (WorkflowNode wn : lvlList.get(i).getLvlNodes()) {
				// fluct = Math.round(calcFluctuation(wn.getRunTime()));
				sum += wn.getRunTimeWithData(bandwidth) + fluct;
				run+=wn.getRunTime();
				datasize+=wn.getTransferSize();
			}
		}
		
		int nodeSize = graph.getNodeNum() - 2;
//		System.out.println(String.format("Graph name:%s nodeSize:%d edgeSize:%d avgRunTime:%f avgTransferSize:%f depth:%d", graph.GetName(),
//				nodeSize,graph.getEdgeNumber(), (float) run / nodeSize, (float) datasize/1000000 / nodeSize,lvlList.size())) ;

		
		alpha = (float) TotalDeadline.get(index) / sum;
		alph = ((float) TotalDeadline.get(index) - TotalStartTimes.get(index)) / sum;
		float[] DR = new float[lvlList.size()];
		float[] DRr = new float[lvlList.size()];
		long levelWeight = 0;
		DRr[0] = TotalStartTimes.get(index);
		for (int i = 0; i < lvlList.size(); i++) {
			levelWeight = 0;
			for (WorkflowNode wn : lvlList.get(i).getLvlNodes()) {
				levelWeight += wn.getRunTimeWithData(bandwidth) + fluct;
			}
			DR[i] = levelWeight * alpha;
			DRr[i] = levelWeight * alph;
			if (i > 0) {
				DR[i] += DR[i - 1];
				DRr[i] += DRr[i - 1];
			} else
				DRr[i] = TotalStartTimes.get(index) + DRr[i];
		}
		for (int i = 0; i < lvlList.size(); i++) {
			lvlList.get(i).setSubDeadline(Math.round(DRr[i]));
		}

		long minDl, subDl = 0, maxsubDl;

		// sets sub deadline for each task.
		for (int i = 0; i < lvlList.size(); i++) {
			maxsubDl = 0;
			for (WorkflowNode curNode : lvlList.get(i).getLvlNodes()) {
				// sum += wn.getRunTimeWithData(bandwidth);
				subDl = 0;
				minDl = 0;
				fluct = Math.round(calcFluctuation(curNode.getRunTime()));
				if (i > 0) {
					minDl = lvlList.get(i - 1).getSubDeadline();
				}

				minDl = Math.max(curNode.getEST(), minDl) + curNode.getRunTime() + fluct;
				subDl = lvlList.get(i).getSubDeadline();
				// this is deffirent

				subDl = Math.max(subDl, minDl);
				subDl = Math.min(subDl, curNode.getLFT());
				curNode.setDeadline(subDl);
				if (subDl > maxsubDl) {
					maxsubDl = subDl;
				}

				lvlList.get(i).setSubDeadline(maxsubDl);
			}
		}

	}
	private void calculateLevelLists(WorkflowGraph graph, float cost) {
		lvlList.clear();
		PriorityQueue<WorkflowNode> queue = new PriorityQueue<WorkflowNode>(graph.nodes.size(),
				new WorkflowPolicy.DownLevelComparator());
		result r;
		int bestFinish = Integer.MAX_VALUE;

		computeDownRank(graph);
		// addes nodes to graph
		for (WorkflowNode node : graph.nodes.values())
			if (!node.getId().equals(graph.getStartId()) && !node.getId().equals(graph.getEndId()))
				queue.add(node);

		while (!queue.isEmpty()) {
			WorkflowNode curNode = queue.remove();
			insertLevel(curNode.getLevelBottem(), curNode);
		}
		

		levelDag.put(graph.GetName(), lvlList);

	}

	private float CalculateCTTF(WorkflowNode curNode) {

		result r, rFast, rExpensive, rCheap, bestR = null;
		int bestInst = -1;
		// float bestCost = Float.MAX_VALUE;
		int bestFinish = Integer.MAX_VALUE;
		long minEnergy = Integer.MAX_VALUE;
		float Time, ENERGY;
		long subDl;
		float CTTF, BestCTTF = Integer.MAX_VALUE * -1;
		float minFreq = 1;

		long taskComp = 0;

		subDl = (long) curNode.getDeadline();

		for (int curInst = 0; curInst < instances.getSize(); curInst++) {

			if (GAPfit)
				r = super.checkInstanceGapPro(curNode, instances.getInstance(curInst), subDl);
			else
				r = checkInstance(curNode, instances.getInstance(curInst));
			// check divider error not to be zero
			if (r.finishTime > curNode.getDeadline())
				continue;

			CTTF = 1 / (1 + r.cost);
			if (r.gapFitpercent > 0) {
				// CTTF += 10+1 / (curNode.getDeadline() - r.finishTime);
				CTTF += 1;
			}
			// fluct *= 0;

			if (BestCTTF <= CTTF && r.finishTime <= subDl) {
				if (BestCTTF == CTTF && r.finishTime >= bestR.finishTime) {
					continue;
				}
				minEnergy = r.energy;
				bestInst = curInst;
				bestR = r;
				BestCTTF = CTTF;
			}

		}
		if (bestInst == -1)

			for (int curRes = 0; curRes < resources.getSize(); curRes++) { // because the cheapest one is the last
				Instance inst = new Instance(instances.getSize(), resources.getResource(curRes));
				r = checkInstance(curNode, inst);
				long finish = (long) (r.finishTime);
				if (finish > curNode.getDeadline())
					break;

				CTTF = 1 / (1 + r.cost);

				if (BestCTTF < CTTF && finish <= curNode.getDeadline()) {

					minEnergy = r.energy;
					bestInst = 10000 + curRes;
					;
					bestR = r;
					BestCTTF = CTTF;

				}

			}

		Boolean boolInstNotFound = false;
		if (bestInst == -1) {
			bestInst = getFastestInstanceIndex(curNode);
			boolInstNotFound = true;

		}

		result rassigned;
		if (bestInst < 10000) {
			if (bestR == null) {
				bestR = checkInstance(curNode, instances.getInstance(bestInst));
			}
			rassigned = setInstanceE(curNode, instances.getInstance(bestInst), bestR, graph);
		} else {
			bestInst -= 10000;
			Instance inst = new Instance(instances.getSize(), resources.getResource(bestInst));
			instances.addInstance(inst);
			if (bestR == null) {
				bestR = checkInstance(curNode, inst);
			}
			rassigned = setInstanceE(curNode, inst, bestR, graph);
		}
//		if (curNode.getDeadline() < rassigned.finishTime) {
//			System.out.println("id: " + curNode.getId() + "  dl: " + curNode.getDeadline() + " resourceFinish: "
//					+ rassigned.finishTime);
//		}
		OverallResults.transferTime += rassigned.transferTime;
		OverallResults.reliability *= rassigned.reliability;
		OverallResults.energy += rassigned.energy;
		OverallResults.cost += rassigned.cost;
		return rassigned.cost;
	}

	private Boolean planning() {

		result r;

		PriorityQueue<WorkflowNode> queue = new PriorityQueue<WorkflowNode>(NodeSizes,
				new WorkflowPolicy.subDeadlineAscending());

		for (int i = 0; i < graphs.length; i++) {
			for (WorkflowNode node : graphs[i].nodes.values()) {
				node.graphid = i;
				if (!node.getId().equals(graphs[i].getStartId()) && !node.getId().equals(graphs[i].getEndId()))
					queue.add(node);
			}
		}

		float cost = 0;

		while (!queue.isEmpty()) {
			WorkflowNode cNode;
			cNode = queue.remove();

			cost = CalculateCTTF(cNode);

			if (cost == Float.MAX_VALUE)
				return false;
		}
		algEnd3 = System.nanoTime() - alStart;
		return true;

	}

	private class levelNodes {
		int levelid;
		List<WorkflowNode> lvlNodes = new ArrayList<>();
		private float subBudget;
		private long subDeadline;

		public levelNodes(int levelid, WorkflowNode lvlNodes) {
			super();
			this.levelid = levelid;
			this.lvlNodes.add(lvlNodes);
			this.subBudget = 0;
			this.subDeadline = 0;
		}

		public int getNodeLevelId(String nodeID) {
			for (int i = 0; i < lvlList.size(); i++) {
				for (WorkflowNode wn : lvlList.get(i).getLvlNodes()) {
					if (wn.getId().contains(nodeID))
						return i;
				}
			}
			return -1;
		}

		public int getLevelid() {
			return levelid;
		}

		public void setLevelid(int levelid) {
			this.levelid = levelid;
		}

		public List<WorkflowNode> getLvlNodes() {
			return lvlNodes;
		}

		public void setLvlNodes(List<WorkflowNode> lvlNodes) {
			this.lvlNodes = lvlNodes;
		}

		public float getSubBudget() {
			if (!justRemainingBudget)
				return subBudget;
			return remaingCost;
		}

		public void setSubBudget(float subBudget) {
			this.subBudget = subBudget;
		}

		public long getSubDeadline() {
			return subDeadline;
		}

		public void setSubDeadline(long subDeadline) {
			this.subDeadline = subDeadline;
		}

	}

}
