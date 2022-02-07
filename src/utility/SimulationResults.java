package utility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import Broker.WorkflowNode;

public class SimulationResults {
	public static List<WorkflowNode> criticalPath;
	public static ArrayList<List<WorkflowNode>> criticalPaths= new ArrayList<List<WorkflowNode>>();
	public static long computation,datatransfer, runTime,Tasks=0;
	
	//ComputationRatePerData
	public static double ComputationRate2data=0;
	//DataRatePerComp
	public static double DataRate2Comp=0;
//	ArrayList<ArrayList<String>> listOLists;


}
