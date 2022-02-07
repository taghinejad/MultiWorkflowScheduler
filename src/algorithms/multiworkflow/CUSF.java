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
	public static boolean FullFrequency = true;

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
		this.TotalStartTimes=startTimes;
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
			//distributeDeadline(graphs[i]);
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
		float alpha,alph = 0;
		long sum = 0;
		for (int i = 0; i < lvlList.size(); i++) {
			for (WorkflowNode wn : lvlList.get(i).getLvlNodes()) {
				// fluct = Math.round(calcFluctuation(wn.getRunTime()));
				sum += wn.getRunTimeWithData(bandwidth) + fluct;
//				sum += wn.getRunTime();
			}
		}
		alpha = (float) TotalDeadline.get(index) / sum;
		alph = ((float) TotalDeadline.get(index)-TotalStartTimes.get(index)) / sum;
		float[] DR = new float[lvlList.size()];
		float[] DRr = new float[lvlList.size()];
		long levelWeight = 0;
		DRr[0]=TotalStartTimes.get(index);
		for (int i = 0; i < lvlList.size(); i++) {
			levelWeight = 0;
			for (WorkflowNode wn : lvlList.get(i).getLvlNodes()) {
				// fluct = Math.round(calcFluctuation(wn.getRunTime()));
				levelWeight += wn.getRunTimeWithData(bandwidth) + fluct;
				// levelWeight += wn.getRunTime();
			}
			DR[i] = levelWeight * alpha;
			DRr[i] = levelWeight * alph;
			if (i > 0) {
				DR[i] += DR[i - 1];
				DRr[i] += DRr[i - 1];
			}
			else
				DRr[i]=TotalStartTimes.get(index)+DRr[i];
		}
		for (int i = 0; i < lvlList.size(); i++) {
			lvlList.get(i).setSubDeadline(Math.round(DRr[i]));
		}

		long minDl, subDl = 0, maxsubDl;

		
		//sets sub deadline for each task. 
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
private levelNodes getLevelNode(int levelID) {
		for (int i = 0; i < lvlList.size(); i++) {
			{
				if (lvlList.get(i).getLevelid() == levelID)
					return lvlList.get(i);
			}
		}
		return null;
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
		lvlList.get(0).setSubBudget(cost);

		levelDag.put(graph.GetName(), lvlList);

	}

	private float getConsumedCost() {
		return super.computeFinalCost();
	}

	private CTTFclass CTTF(long subDl, int maxTime, int minTime, int curTime, float maxBudget, float curBudget,
			float minBudget, long minEnergy, long maxEnergy, long energy, Boolean Isinstance) {
		if (Isinstance) {
			return CTTFInst(subDl,maxTime, minTime, curTime, maxBudget, curBudget, minBudget,
					minEnergy, maxEnergy, energy);
		}
		CTTFclass cttf = new CTTFclass();

		cttf.Time = (float) (subDl - curTime) / (subDl - minTime);
		cttf.Energy = ((float) (maxEnergy - energy) / (maxEnergy - minEnergy));
		cttf.Cost = ((float) (maxBudget - curBudget) / (maxBudget - minBudget));
		
		
		//cttf.Energy=(float) 1/ energy;
		cttf.CTTF = ((CostImpact * cttf.Cost) + (TimeImpact * cttf.Time) + (EnergyImpact * cttf.Energy)) / 3;
		// cttf.CTTF = (cttf.Time +cttf.Energy+ cttf.Cost);
		// cttf.CTTF = (CostImpact * cttf.Cost) / (TimeImpact * cttf.Time) +
		// (EnergyImpact * cttf.Energy);

		return cttf;
	}

	private CTTFclass CTTFInst(long subDl, int maxTime, int minTime, int curTime, float maxBudget, float curBudget,
			float minBudget, long minEnergy, long maxEnergy, long energy) {
		CTTFclass cttf = new CTTFclass();
		cttf.Time = (float) (subDl - curTime) / (subDl - minTime);
		cttf.Energy = ((float) (maxEnergy - energy) / (maxEnergy - minEnergy));
	//	cttf.Cost = ((float) (maxBudget - curBudget) / (maxBudget - minBudget));

		cttf.CTTF = (float) ((EnergyImpact * cttf.Energy) + ((TimeImpact * cttf.Time)))/2;

		return cttf;
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

		rFast = checkInstanceE(curNode, getFastestInstance(curNode));
		rCheap = checkInstanceE(curNode, getCheapInstanceNoDeadline(curNode));
		rExpensive = checkInstanceE(curNode, getExpensiveInstance(curNode));
		long minimalEn = getMinEnergy(curNode).energy;
		long maximalEn = getMaxEnergy(curNode).energy;

		subDl = (long) curNode.getDeadline();
		// subDl = (long) curNode.getLFT();
		CTTFclass CTTFf;

		for (int curInst = 0; curInst < instances.getSize(); curInst++) {

			if (GAPfit)
				r = super.checkInstanceGapPro(curNode, instances.getInstance(curInst), subDl);
			else
				r = checkInstance(curNode, instances.getInstance(curInst));
			// check divider error not to be zero
			if (r.finishTime > curNode.getDeadline())
				continue;
			CTTFf = CTTF(subDl, rCheap.finishTime, rFast.finishTime, r.finishTime, rExpensive.cost, r.cost, rCheap.cost,
					minimalEn, maximalEn, r.energy, true);
			CTTF = CTTFf.CTTF + r.gapFitpercent;

			// fluct *= 0;

			if (BestCTTF < CTTF && r.finishTime <= subDl) {
				minEnergy = r.energy;
				bestInst = curInst;
				bestR = r;
				BestCTTF = CTTF;

				;
				if (DVFSenabled) {
					if (!FullFrequency) {
						taskComp = Math.round((float) curNode.getInstructionSize()
								/ (instances.getInstance(curInst).getType().getMIPS()));

						minFreq = getMinFreq(Math.round(taskComp), subDl - r.startTime, instances.getInstance(curInst));
//					
						if (GAPfit)
							r = checkInstanceGapFreqPro(curNode, instances.getInstance(curInst), subDl, minFreq);
						else
							r = checkInstanceEFrequency(curNode, instances.getInstance(curInst), minFreq);
						CTTFf = CTTF(subDl, rCheap.finishTime, rFast.finishTime, r.finishTime, rExpensive.cost, r.cost,
								rCheap.cost, minimalEn, maximalEn, r.energy, true);
						CTTF = CTTFf.CTTF + r.gapFitpercent;

						if (BestCTTF < CTTF && r.finishTime <= curNode.getDeadline()) {

							minEnergy = r.energy;
							bestInst = curInst;
							bestR = r;
							BestCTTF = CTTF;
						}
					} else {
						float freqChange = instances.getInstance(curInst).getType().getChangeFrequency();
						float maxFreq = instances.getInstance(curInst).getType().getMaxFrequencyOr1();
						float miniFreq = instances.getInstance(curInst).getType().getMinFrequency();
						for (float freq = maxFreq
								- freqChange; freq >= miniFreq; freq -= freqChange) {

							if (GAPfit)
								r = checkInstanceGapFreqPro(curNode, instances.getInstance(curInst), subDl, freq);
							else
								r = checkInstanceEFrequency(curNode, instances.getInstance(curInst), freq);

							if (r.finishTime > curNode.getDeadline())
								break;

							CTTFf = CTTF(subDl, rCheap.finishTime, rFast.finishTime, r.finishTime, rExpensive.cost,
									r.cost, rCheap.cost, minimalEn, maximalEn, r.energy, true);
							CTTF = CTTFf.CTTF + r.gapFitpercent;

							if (BestCTTF < CTTF && r.finishTime <= curNode.getDeadline()) {

								minEnergy = r.energy;
								bestInst = curInst;
								bestR = r;
								BestCTTF = CTTF;
								;
							}

						}

					}

				}

			}
		}
if (bestInst==-1)
		for (int curRes = 0; curRes < resources.getSize(); curRes++) { // because the cheapest one is the last
			Instance inst = new Instance(instances.getSize(), resources.getResource(curRes));
			r = checkInstance(curNode, inst);
			long finish = (long) (r.finishTime);
			if (finish > curNode.getDeadline())
				break;

			CTTFf = CTTF(subDl, rCheap.finishTime, rFast.finishTime, r.finishTime, rFast.cost, r.cost, rCheap.cost,
					minimalEn, maximalEn, r.energy, false);

			CTTF = CTTFf.CTTF + r.gapFitpercent;

			if (BestCTTF < CTTF && finish <= curNode.getDeadline()) {

				minEnergy = r.energy;
				bestInst = 10000 + curRes;
				;
				bestR = r;
				BestCTTF = CTTF;

//				if (DVFSenabled) {
//					if (!FullFrequency) {
//						taskComp = Math.round((float) curNode.getInstructionSize() / (inst.getType().getMIPS()));
//						// minFreq = (float) Math.ceil(((float) taskComp * 10 / (subDl - r.startTime)))
//						// / 10;
//						minFreq = getMinFreq(taskComp, subDl - r.startTime, inst);
//
//						r = checkInstanceEFrequency(curNode, inst, minFreq);
//						CTTFf = CTTF(subDl, rCheap.finishTime, rFast.finishTime, r.finishTime, rExpensive.cost, r.cost,
//								rCheap.cost, minimalEn, maximalEn, r.energy, false);
//
//						CTTF = CTTFf.CTTF + r.gapFitpercent;
//						if (BestCTTF < CTTF && r.finishTime <= curNode.getDeadline()) {
//
//							minEnergy = r.energy;
//							bestInst = 10000 + curRes;
//							bestR = r;
//							BestCTTF = CTTF;
//						}
//
//					} else {
//						float freqChange = inst.getType().getChangeFrequency();
//						float maxFreq = inst.getType().getMaxFrequencyOr1();
//						minFreq = inst.getType().getMinFrequency();
//						
//						for (float freq = maxFreq-freqChange; freq >= minFreq; freq -= freqChange) {
//
//							r = checkInstanceEFrequency(curNode, inst, freq);
//							if (r.finishTime > curNode.getDeadline())
//								break;
//							
//							CTTFf = CTTF(subDl, rCheap.finishTime, rFast.finishTime, r.finishTime, rExpensive.cost,
//									r.cost, rCheap.cost, minimalEn, maximalEn, r.energy, false);
//
//							CTTF = CTTFf.CTTF + r.gapFitpercent;
//							if (BestCTTF < CTTF && r.finishTime <= curNode.getDeadline()) {
//
//								minEnergy = r.energy;
//								bestInst = 10000 + curRes;
//								bestR = r;
//								BestCTTF = CTTF;
//							}
//						}
//
//					}
//
//				}
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

	private void distributeDeadline(WorkflowGraph graph) {
		assignParents(graph.getNodes().get(graph.getEndId()));
		for (WorkflowNode node : graph.getNodes().values())
			node.setUnscheduled();
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
	private Boolean planning() {

		result r;
		int bestFinish = Integer.MAX_VALUE;
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

	private class CTTFclass {
		public float Time;
		public float Cost;
		public float Energy;
		public float CTTF;
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
