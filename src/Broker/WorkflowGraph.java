package Broker;

import java.awt.datatransfer.StringSelection;
import java.util.*;
import DAG.*;
import DAG.Adag;
import DAG.FilenameType;
import DAG.LinkageType;
import DAG.Adag.*;
import DAG.Adag.Child.Parent;

public class WorkflowGraph {
	// private Map<String, WorkflowNode> nodes;
	public Map<String, WorkflowNode> nodes;
	private int nodeNum;
	private final String startNodeId = "start";
	private final String endNodeId = "end";

	private final int jobNumPE = 1;
   
	private String Name;
	
	public WorkflowGraph() {
		nodes = new HashMap<String, WorkflowNode>();
		nodeNum = 1;
	}
	
	
	public String GetName() {
		return Name;
	}
	
	


	public boolean convertDagToWorkflowGraph(Adag dag) {
		long inputFilesSize, outputFilesSize, IOsize;
		int runTime;
//		if (utility.configuration.isEnableCCR()) 
//			System.out.printf("\n \n +++++++++++++++++++++++++>CCR: %s", utility.configuration.getCommunication2ComputationRatio());
//		else  
//			System.out.print("\n \n ------------------------->NotCCR---NotCCR--NotCCR  ");
		
		
		
		nodes.clear();
		if (dag == null)
			return (false);
		for (Job job : dag.getJob()) {

			inputFilesSize = outputFilesSize = 0;
			List<File> jobFileSet = new ArrayList<File>();
			for (FilenameType file : job.getUses()) {
				long filesize= Long.valueOf(file.getSize())*utility.configuration.getStandardDataTransfer();
				file.setSize( Long.toString(filesize));
				// @author:ahmad
				FileSets.Insert(file, job);
				
				jobFileSet.add(FileSets.returnAsFile(file));
				// FileSets.InsertByType(file, job);
				// end
				if (file.getLink() == LinkageType.INPUT && Long.valueOf(file.getSize()) > 0)
					inputFilesSize += Long.valueOf(file.getSize())*utility.configuration.getStandardDataTransfer();
				else if (file.getLink() == LinkageType.OUTPUT && Long.valueOf(file.getSize()) > 0)
					outputFilesSize += Long.valueOf(file.getSize())*utility.configuration.getStandardDataTransfer();
			}

			// just for test
			// inputFilesSize = outputFilesSize = 0;

			runTime = Math.round(Float.valueOf(job.getRuntime()));

			if (runTime <= 0)
				runTime = 1;
			WorkflowNode wfNode = new WorkflowNode(job.getId(), job.getName(), inputFilesSize, outputFilesSize,
					runTime,jobFileSet);

			// temporary
			wfNode.setInstructionSize(runTime * utility.configuration.standardMIPS);
			wfNode.setNumPE(jobNumPE);
			if (job.getCores() != null)
				wfNode.setCores(Math.round(Integer.valueOf(job.getCores())));
			else
				wfNode.setCores(1);
			// wfNode.setC
			// wfNode.setLevel(job.getLevel());
			nodes.put(job.getId(), wfNode);
		}

		for (Child child : dag.getChild()) {
			String childId = child.getRef();
			if (!nodes.containsKey(childId)) {
				System.out.println("id= " + childId + " doesn't exist!");
				return (false);
			}
			for (Parent parents : child.getParent()) {
				Job parentCase = (Job) parents.getRef();
				String parentId = parentCase.getId();
				
				
				if (utility.configuration.isEnableCCR()) {
					
					IOsize=Math.round(nodes.get(childId).getRunTime()*utility.configuration.getCommunication2ComputationRatio()* utility.configuration.getBandwidth());
					
				}
				else
					IOsize = computeIOsize(dag, parentId, childId);
				
				nodes.get(childId).setNeedTransferTime(IOsize);
				// just for test
				// IOsize = 0;

				nodes.get(childId).addParent(parentId, IOsize);
				nodes.get(parentId).addChild(childId, IOsize);
			}
		}

		WorkflowNode startNode = new WorkflowNode(startNodeId, startNodeId, 0, 0, 0);
		WorkflowNode endNode = new WorkflowNode(endNodeId, endNodeId, 0, 0, 0);
		startNode.setInstructionSize(0);
		endNode.setInstructionSize(0);
		startNode.setNumPE(0);
		endNode.setNumPE(0);
		for (WorkflowNode node : nodes.values()) {
			if (!node.hasParent()) {
				startNode.addChild(node.getId(), 0);
				node.addParent(startNode.getId(), 0);
			}
			if (!node.hasChild()) {
				node.addChild(endNode.getId(), 0);
				endNode.addParent(node.getId(), 0);
			}
		}
		nodes.put(startNodeId, startNode);
		nodes.put(endNodeId, endNode);
		nodeNum = nodes.size();
		distanceFromBottom(nodes.get(startNodeId), 0);
		distanceFromTop(nodes.get(endNodeId), 0);
		if (WorkflowBroker.unifyRunTime) unifyRunTimes();
		unifyRunTimes();

		// Set<String, WorkflowNode> str= nodes.entrySet();

		Set<Map.Entry<String, WorkflowNode>> nodeList = nodes.entrySet();
		for (Map.Entry<String, WorkflowNode> nd : nodeList) {
			WorkflowNode wn = nd.getValue();
			String Nid = wn.getId();
			
			if (!nodes.containsKey(Nid)) {
				System.out.println("id= " + Nid + " doesn't exist!");
				return (false);
			}
			long IO = 0;
			for (Link parents : wn.getParents()) {
				IO += parents.dataSize;
			}
			long input = nodes.get(nd.getKey()).getInputFileSize();
			if (input - IO > 0)
				nodes.get(nd.getKey()).setReadFileSize(input - IO);
			else
				nodes.get(nd.getKey()).setReadFileSize(0);
		}
		// for (int i = 0; i < nodes.size(); i++) {
		//
		// WorkflowNode wn = nodes.get(str.get(i));
		// if (wn == null)
		// continue;
		// String Nid = wn.getId();
		// if (!nodes.containsKey(Nid)) {
		// System.out.println("id= " + Nid + " doesn't exist!");
		// return (false);
		// }
		// long IO = 0;
		// for (Link parents : wn.getParents()) {
		// IO += parents.dataSize;
		// }
		// nodes.get(Nid).setReadFileSize(nodes.get(Nid).getInputFileSize() - IO);
		// }
		return (true);
	}
	public String GetNameFromWorkflow(String name) {
		
		if (name.indexOf("/")!=-1){
		name=name.substring(name.indexOf("/")+1 );
		}
		return name.substring(0, name.indexOf("."));
		
	}

	public boolean convertDagToWorkflowGraph(Adag dag,String name) {
		Random rn=new Random();
		name=GetNameFromWorkflow(name)+"-w"+rn.nextInt(100)+"+";
		this.Name=name;
		long inputFilesSize, outputFilesSize, IOsize;
		int runTime;
//		if (utility.configuration.isEnableCCR()) 
//			System.out.printf("\n \n +++++++++++++++++++++++++>CCR: %s", utility.configuration.getCommunication2ComputationRatio());
		
//		if (!utility.configuration.isEnableCCR()) 
//			System.out.print("\n \n ------------------------->NotCCR---NotCCR--NotCCR  ");
		
		
		
		nodes.clear();
		if (dag == null)
			return (false);
		for (Job job : dag.getJob()) {

			inputFilesSize = outputFilesSize = 0;
			List<File> jobFileSet = new ArrayList<File>();
			for (FilenameType file : job.getUses()) {
				long filesize= Long.valueOf(file.getSize())*utility.configuration.getStandardDataTransfer();
				file.setSize( Long.toString(filesize));
				// @author:ahmad
				FileSets.Insert(file, job);
				
				jobFileSet.add(FileSets.returnAsFile(file));
				// FileSets.InsertByType(file, job);
				// end
				if (file.getLink() == LinkageType.INPUT && Long.valueOf(file.getSize()) > 0)
					inputFilesSize += Long.valueOf(file.getSize())*utility.configuration.getStandardDataTransfer();
				else if (file.getLink() == LinkageType.OUTPUT && Long.valueOf(file.getSize()) > 0)
					outputFilesSize += Long.valueOf(file.getSize())*utility.configuration.getStandardDataTransfer();
			}

			// just for test
			// inputFilesSize = outputFilesSize = 0;

			runTime = Math.round(Float.valueOf(job.getRuntime()));

			if (runTime <= 0)
				runTime = 1;
			WorkflowNode wfNode = new WorkflowNode(name+job.getId(), job.getName(), inputFilesSize, outputFilesSize,
					runTime,jobFileSet);

			// temporary
			wfNode.setInstructionSize(runTime * utility.configuration.standardMIPS);
			wfNode.setNumPE(jobNumPE);
			if (job.getCores() != null)
				wfNode.setCores(Math.round(Integer.valueOf(job.getCores())));
			else
				wfNode.setCores(1);
			// wfNode.setC
			// wfNode.setLevel(job.getLevel());
			nodes.put(name+job.getId(), wfNode);
		}

		for (Child child : dag.getChild()) {
			String childId = name+child.getRef();
			if (!nodes.containsKey(childId)) {
				System.out.println("id= " + childId + " doesn't exist!");
				return (false);
			}
			for (Parent parents : child.getParent()) {
				Job parentCase = (Job) parents.getRef();
				String parentId = name+parentCase.getId();
				
				
				if (utility.configuration.isEnableCCR()) {
					
					IOsize=Math.round(nodes.get(childId).getRunTime()*utility.configuration.getCommunication2ComputationRatio()* utility.configuration.getBandwidth());
					
				}
				else
					IOsize = computeIOsize(dag, parentId, childId);
				
				nodes.get(childId).setNeedTransferTime(IOsize);
				// just for test
				// IOsize = 0;

				nodes.get(childId).addParent(parentId, IOsize);
				nodes.get(parentId).addChild(childId, IOsize);
			}
		}

		WorkflowNode startNode = new WorkflowNode(startNodeId, startNodeId, 0, 0, 0);
		WorkflowNode endNode = new WorkflowNode(endNodeId, endNodeId, 0, 0, 0);
		startNode.setInstructionSize(0);
		endNode.setInstructionSize(0);
		startNode.setNumPE(0);
		endNode.setNumPE(0);
		for (WorkflowNode node : nodes.values()) {
			if (!node.hasParent()) {
				startNode.addChild(node.getId(), 0);
				node.addParent(startNode.getId(), 0);
			}
			if (!node.hasChild()) {
				node.addChild(endNode.getId(), 0);
				endNode.addParent(node.getId(), 0);
			}
		}
		nodes.put(startNodeId, startNode);
		nodes.put(endNodeId, endNode);
		nodeNum = nodes.size();
		distanceFromBottom(nodes.get(startNodeId), 0);
		distanceFromTop(nodes.get(endNodeId), 0);
		if (WorkflowBroker.unifyRunTime) unifyRunTimes();
		unifyRunTimes();

		// Set<String, WorkflowNode> str= nodes.entrySet();

		Set<Map.Entry<String, WorkflowNode>> nodeList = nodes.entrySet();
		for (Map.Entry<String, WorkflowNode> nd : nodeList) {
			WorkflowNode wn = nd.getValue();
			String Nid = wn.getId();
			
			if (!nodes.containsKey(Nid)) {
				System.out.println("id= " + Nid + " doesn't exist!");
				return (false);
			}
			long IO = 0;
			for (Link parents : wn.getParents()) {
				IO += parents.dataSize;
			}
			long input = nodes.get(nd.getKey()).getInputFileSize();
			if (input - IO > 0)
				nodes.get(nd.getKey()).setReadFileSize(input - IO);
			else
				nodes.get(nd.getKey()).setReadFileSize(0);
		}
		// for (int i = 0; i < nodes.size(); i++) {
		//
		// WorkflowNode wn = nodes.get(str.get(i));
		// if (wn == null)
		// continue;
		// String Nid = wn.getId();
		// if (!nodes.containsKey(Nid)) {
		// System.out.println("id= " + Nid + " doesn't exist!");
		// return (false);
		// }
		// long IO = 0;
		// for (Link parents : wn.getParents()) {
		// IO += parents.dataSize;
		// }
		// nodes.get(Nid).setReadFileSize(nodes.get(Nid).getInputFileSize() - IO);
		// }
		return (true);
	}

	
	
	public void unifyRunTimes() {
		HashMap<String, counter> jobTypes = new HashMap<String, counter>();

		// Creates a list called jobType for each DAG like "montage_25"
		// every item in list showes jobType which it points to its meanInstructionSet
		// and Maximum InstructionSet of its tasks
		for (WorkflowNode node : nodes.values()) {
			String curJob = node.getName();
			if (jobTypes.containsKey(curJob))
				jobTypes.get(curJob).add(node.getInstructionSize());
			else {
				counter c = new counter(node.getInstructionSize());
				jobTypes.put(curJob, c);
			}
		}

		for (counter c : jobTypes.values())
			c.computeMean();

		for (WorkflowNode node : nodes.values()) {
			String curJob = node.getName();
			node.setInstructionSize(jobTypes.get(curJob).getMean());
		}
	}

	private long computeIOsize(Adag dag, String parentId, String childId) {
		long size = 0;

		for (Job parentJob : dag.getJob()) {
			if (parentJob.getId().equals(parentId)) {
				for (Job childJob : dag.getJob()) {
					if (childJob.getId().equals(childId)) {
						for (FilenameType outFile : parentJob.getUses()) {
							if (outFile.getLink() == LinkageType.OUTPUT) {
								for (FilenameType inFile : childJob.getUses()) {
									if (inFile.getLink() == LinkageType.INPUT
											&& inFile.getFile().equals(outFile.getFile())) {
										double curSize = Long.valueOf(inFile.getSize())*utility.configuration.getStandardDataTransfer();
										if (curSize > 0)
											size += curSize;
									}
								}
							}
						}
					}
				}
			}
		}
		return (size);
	}

	public Map<String, WorkflowNode> getNodes() {
		return (nodes);
	}

	public void setNodes(Map<String, WorkflowNode> newNodes) {
		nodes = newNodes;
	}

	public String getStartId() {
		return (startNodeId);
	}

	public String getEndId() {
		return (endNodeId);
	}

	public int getNodeNum() {
		return (nodeNum);
	}

	public int distanceFromTop(WorkflowNode node, int ds) {
		if (!node.hasParent()) {

			nodes.get(node.getId()).setLevelTop(ds);
			return 0;
		} else {
			int max = ds, cur = ds;
			for (Link link : node.getParents()) {
				WorkflowNode nd = nodes.get(link.getId());
				cur = 1 + distanceFromTop(nd, ds);
				if (max < cur)
					max = cur;
			}
			nodes.get(node.getId()).setLevelTop(max);
			return max;
		}
	}

	public int distanceFromBottom(WorkflowNode node, int ds) {
		if (!node.hasChild()) {
			nodes.get(node.getId()).setLevelBottem(ds);
			return 0;
		} else {
			int max = ds, cur = ds;
			for (Link link : node.getChildren()) {
				WorkflowNode nd = nodes.get(link.getId());
				cur = 1 + distanceFromBottom(nd, ds);
				if (max < cur)
					max = cur;
			}
			nodes.get(node.getId()).setLevelBottem(max);
			return max;
		}
	}

	// this is a type how keeps mean Instruction size and Maximum InstSize for all
	// nodes of one type workflow
	private class counter {
		private long meanInstSize;
		private int maxInstSize;
		private int no;

		public counter() {
			maxInstSize = -1;
			meanInstSize = 0;
			no = 0;
		}

		public counter(int instSize) {
			maxInstSize = instSize;
			meanInstSize = instSize;
			no = 1;
		}

		public void add(int instSize) {
			meanInstSize += instSize;
			if (instSize > maxInstSize)
				maxInstSize = instSize;
			no++;
		}

		public int computeMean() {
			meanInstSize /= no;
			return ((int) meanInstSize);
		}

		public int getMean() {
			return ((int) meanInstSize);
		}

		public int getMax() {
			return (maxInstSize);
		}
	}

}
