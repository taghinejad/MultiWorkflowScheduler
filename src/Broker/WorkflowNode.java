package Broker;

import java.util.*;

import DAG.LinkageType;

public class WorkflowNode {
	private String id, name;
	// readFileSize is the Size of File that its read from storage not from its
	// parents.
	private long inputFileSize, outputFileSize, readFileSize;
	private int runTime, instructionSize; // in MI/
	private int numPE;
	private List<Link> parents, children;
	private WorkflowNode CriticalParent;
	private int levelTop;
	private int levelBottem;
	// these will be filled in scheduling time
	private boolean scheduled;
	private int EST, EFT, AST, AFT, LFT, LST, MET, StartReading;
	private int upRank, downRank, SumRank;
	private float subDeadline;
	private int selectedResource;
	private int cores;
	private List<File> FileSet;
	private long NeedTransferTime;
	private int meanRunTime;
	private int slackTime = 0;
	public int graphid = -1;

	public WorkflowNode(String nodeId) {
		id = new String(nodeId);
		name = new String();
		inputFileSize = outputFileSize = 0;
		runTime = instructionSize = 0;
		parents = new ArrayList<Link>();
		children = new ArrayList<Link>();
		scheduled = false;
	}

	public WorkflowNode(String nodeId, String nodeName, long inSize, long outSize, int rt) {
		id = new String(nodeId);
		name = new String(nodeName);
		inputFileSize = inSize;
		outputFileSize = outSize;
		runTime = rt;
		parents = new ArrayList<Link>();
		children = new ArrayList<Link>();
		scheduled = false;
	}

	public WorkflowNode(String nodeId, String nodeName, long inSize, long outSize, long readFileSize, int rt) {
		id = new String(nodeId);
		name = new String(nodeName);
		inputFileSize = inSize;
		outputFileSize = outSize;
		runTime = rt;
		parents = new ArrayList<Link>();
		children = new ArrayList<Link>();
		scheduled = false;
		this.readFileSize = readFileSize;
	}

	public WorkflowNode(String nodeId, String nodeName, long inSize, long outSize, int rt, List<File> Files) {
		id = new String(nodeId);
		name = new String(nodeName);
		inputFileSize = inSize;
		outputFileSize = outSize;
		runTime = rt;
		parents = new ArrayList<Link>();
		children = new ArrayList<Link>();
		scheduled = false;
		this.FileSet = Files;
	}

	public WorkflowNode(String nodeId, String nodeName, long inSize, long outSize, long readFileSize, int rt,
			List<File> Files) {
		id = new String(nodeId);
		name = new String(nodeName);
		inputFileSize = inSize;
		outputFileSize = outSize;
		runTime = rt;
		parents = new ArrayList<Link>();
		children = new ArrayList<Link>();
		scheduled = false;
		this.FileSet = Files;
		this.readFileSize = readFileSize;
	}

	public WorkflowNode(String id, String name, long inputFileSize, long outputFileSize, long readFileSize, int runTime,
			int instructionSize, int numPE, List<Link> parents, List<Link> children, WorkflowNode criticalParent,
			int levelTop, int levelBottem, boolean scheduled, int eST, int eFT, int aST, int aFT, int lFT, int lST,
			int mET, int startReading, int upRank, int downRank, int sumRank, float subDeadline, int selectedResource,
			int cores, List<File> fileSet, long needTransferTime, int meanRunTime, int slackTime) {
		super();
		this.id = id;
		this.name = name;
		this.inputFileSize = inputFileSize;
		this.outputFileSize = outputFileSize;
		this.readFileSize = readFileSize;
		this.runTime = runTime;
		this.instructionSize = instructionSize;
		this.numPE = numPE;
		this.parents = parents;
		this.children = children;
		CriticalParent = criticalParent;
		this.levelTop = levelTop;
		this.levelBottem = levelBottem;
		this.scheduled = scheduled;
		EST = eST;
		EFT = eFT;
		AST = aST;
		AFT = aFT;
		LFT = lFT;
		LST = lST;
		MET = mET;
		StartReading = startReading;
		this.upRank = upRank;
		this.downRank = downRank;
		SumRank = sumRank;
		this.subDeadline = subDeadline;
		this.selectedResource = selectedResource;
		this.cores = cores;
		FileSet = fileSet;
		NeedTransferTime = needTransferTime;
		this.meanRunTime = meanRunTime;
		this.slackTime = slackTime;
	}

	public WorkflowNode(WorkflowNode node) {
		id = new String(node.id);
		name = new String(node.name);
		inputFileSize = node.inputFileSize;
		outputFileSize = node.outputFileSize;
		runTime = node.runTime;
		instructionSize = node.instructionSize;
		scheduled = node.scheduled;
		EST = node.EST;
		EFT = node.EFT;
		LST = node.LST;
		AST = node.AST;
		LFT = node.LFT;
		MET = node.MET;
		upRank = node.upRank;
		subDeadline = node.subDeadline;
		selectedResource = node.selectedResource;
		parents = new ArrayList<Link>();
		children = new ArrayList<Link>();

		for (Link link : node.parents)
			parents.add(new Link(link.getId(), link.getDataSize()));
		for (Link link : node.children)
			children.add(new Link(link.getId(), link.getDataSize()));
	}

	public int getStartReading() {
		return StartReading;
	}

	public int getSlackTime() {
		return slackTime;
	}

	public void setSlackTime(int slackTime) {
		this.slackTime = slackTime;
	}

	public void setStartReading(int sTARTREADTIME) {
		StartReading = sTARTREADTIME;
	}

	public int getDownRank() {
		return downRank;
	}

	public void setDownRank(int downRank) {
		this.downRank = downRank;
	}

	public int getSumRank() {
		return SumRank;
	}

	public void setSumRank(int sumRank) {
		SumRank = sumRank;
	}

	public int getCores() {
		return cores;
	}

	public void setCores(int cores) {
		this.cores = cores;
	}

	public long getReadFileSize() {
		return readFileSize;
	}

	public void setReadFileSize(long readFileSize) {
		this.readFileSize = readFileSize;
	}

	public List<File> getFileSet() {
		return FileSet;
	}

	public List<File> getInputFileSet() {
		List<File> fs = new ArrayList<File>();
		for (File f : FileSet) {
			if (f.link == LinkageType.INPUT) {
				fs.add(f);
			}
		}
		return fs;
	}

	public List<File> getOutputFileSet() {
		List<File> fs = new ArrayList<File>();
		for (File f : FileSet) {
			if (f.link == LinkageType.OUTPUT) {
				fs.add(f);
			}
		}
		return fs;
	}

	public void setFileSet(List<File> fileSet) {
		FileSet = fileSet;
	}

	/**
	 * @return the levelTop
	 */
	public int getLevelTop() {
		return levelTop;
	}

	/**
	 * @param levelTop the levelTop to set
	 */
	public void setLevelTop(int levelTop) {
		this.levelTop = levelTop;
	}

	/**
	 * @return the levelBottem
	 */
	public int getLevelBottem() {
		return levelBottem;
	}

	public WorkflowNode getCriticalParent() {
		return CriticalParent;
	}

	public void setCriticalParent(WorkflowNode criticalParent) {
		CriticalParent = criticalParent;
	}

	/**
	 * @param levelBottem the levelBottem to set
	 */
	public void setLevelBottem(int levelBottem) {
		this.levelBottem = levelBottem;
	}

	public long getNeedTransferTime() {
		return NeedTransferTime;
	}

	public void setNeedTransferTime(long needTransferTime) {
		NeedTransferTime = needTransferTime;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String newName) {
		name = newName;
	}

	public long getInputFileSize() {
		return (inputFileSize);
	}

	public long getOutputFileSize() {
		return (outputFileSize);
	}

	public int getRunTime() {
		return (runTime);
	}

	public int getRunTimeWithData(long bandwidth) {
		int readTime = (int) Math.round(((double) getNeedTransferTime()) / bandwidth);
		return (runTime + readTime);
	}

	public int getReadTime(long bandwidth) {
		int readTime = (int) Math.round(((double) readFileSize) / bandwidth);
		return (readTime);
	}

	public void setInputFileSize(long newSize) {
		if (newSize >= 0)
			inputFileSize = newSize;
	}

	public int getNumPE() {
		return (numPE);
	}

	public void setNumPE(int n) {
		numPE = n;
	}

	public void setOutputFileSize(long newSize) {
		if (newSize >= 0)
			outputFileSize = newSize;
	}

	public void setRunTime(int newRunTime) {
		if (newRunTime >= 0)
			runTime = newRunTime;
	}

	public void addParent(String parentId, long size) {
		parents.add(new Link(parentId, size));
	}

	public void addChild(String childId, long size) {
		children.add(new Link(childId, size));
	}

	public ArrayList<Link> getParents() {
		return (ArrayList<Link>) parents;
	}

	public ArrayList<Link> getChildren() {
		return (ArrayList<Link>) children;
	}

	public String getChildrenIDString() {
		String s = "";

		for (Link link : children) {

			s += " , " + link.id;
		}
		return s;
	}

	public boolean hasChild() {
		if (children.size() == 0)
			return (false);
		else
			return (true);
	}

	public boolean hasParent() {
		if (parents.size() == 0)
			return (false);
		else
			return (true);
	}

	public boolean isScheduled() {
		return (scheduled);
	}

	public void setScheduled() {
		scheduled = true;
	}

	public void setUnscheduled() {
		scheduled = false;
	}

	public int getAST() {
		return (AST);
	}

	public void setAST(int time) {
		AST = time;
	}

	public int getAFT() {
		return (AFT);
	}

	public void setAFT(int time) {
		AFT = time;
	}

	public int getLFT() {
		return (LFT);
	}

	public void setLFT(int time) {
		LFT = time;
	}

	public int getEST() {
		return (EST);
	}

	public void setEST(int time) {
		EST = time;
	}

	public int getEFT() {
		return (EFT);
	}

	public void setEFT(int time) {
		EFT = time;
	}

	public int getLST() {
		return (LST);
	}

	public void setLST(int time) {
		LST = time;
	}

	public int getSelectedResource() {
		return (selectedResource);
	}

	public void setSelectedResource(int resIndex) {
		selectedResource = resIndex;
	}

	public int getMET() {
		return (MET);
	}

	public void setMET(int time) {
		MET = time;
	}

	public int getInstructionSize() {
		return (instructionSize);
	}

	public void setInstructionSize(int size) {
		instructionSize = size;
	}

	public int getUpRank() {
		return (upRank);
	}

	public void setUpRank(int ur) {
		upRank = ur;
	}

	public float getDeadline() {
		return subDeadline;
	}

	public void setDeadline(float minTime) {
		subDeadline = minTime;
	}

	public long getChildDataSize(String childId) {
		for (Link child : children) {
			if (child.getId().equals(childId))
				return (child.getDataSize());
		}
		return (0);
	}

	public long getParentDataSize(String parentId) {
		for (Link parent : parents) {
			if (parent.getId().equals(parentId))
				return (parent.getDataSize());
		}
		return (0);
	}

	public int getMeanRunTime() {
		return meanRunTime;
	}

	public void setMeanRunTime(int meanRunTime) {
		this.meanRunTime = meanRunTime;
	}

}
