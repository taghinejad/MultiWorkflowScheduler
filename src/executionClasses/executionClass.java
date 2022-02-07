package executionClasses;

import java.util.List;

import Broker.Log;
import Broker.ScheduleType;
import Broker.WorkflowBroker;
import Broker.WorkflowPolicy;
import Broker.result;

import algorithms.NoConstrained.HEFTAlgorithm;

import algorithms.multiworkflow.CUSF;
import algorithms.multiworkflow.EUSF;
import algorithms.multiworkflow.NOSF;
import charts.instanceChart;
import utility.SimulationResults;

public class executionClass {
	static WorkflowBroker wb;
	// static int deadline = 0;
	// static int bandwidth = 200000000;
	public static String ids = "";
	static int FactestTime = 1, FastestCPTime = 0, CheapestTime = 1;
	public static int HeftTime = 1, pcpTime = 1;
	static int CheapestCpTime;
	static int CheapestIndTime;
	static float FastestCost, FastestCPCost = 0, CheapestCost, CheapestCpCost, CheapestIndCost, HeftCost, PCPcost;
	public static int minPCPtime = 0;
	public static result OverallResults;


	public static void scheduleWorkflowInitial(String WfFile, int res, int interval, int bandwidth) {
		scheduleWorkflowFastest(WfFile, res, interval, bandwidth);
		RunCheapestCP(WfFile, res, interval, bandwidth);
		RunCheapestPolicy(WfFile, res, interval, bandwidth);
		RunFastestCP(WfFile, res, interval, bandwidth);
		
		RunHeft(WfFile, false, res, interval, bandwidth);

	}

	public static void scheduleWorkflowFastest(String WfFile, int res, int interval, int bandwidth) {
		int startTime = 0;
		try {
			wb = new WorkflowBroker(WfFile, ScheduleType.MY_FAST, interval, bandwidth, res);
		} catch (Exception e) {
			System.out.println(" Error in creating workflow broker!!!" + e.getLocalizedMessage());
		}
		// CH is Cost MH is time fastest Run
		FastestCost = wb.schedule(startTime, 0);
		FactestTime = wb.graph.getNodes().get(wb.graph.getEndId()).getAST();
		// setting Deadline
		// deadline = MH * 10;
		System.out.println(" Fastest Cost=" + FastestCost + "  Time=" + FactestTime + " ResourceDetails[Mips"
				+ wb.resources.getMaxMIPS() + " CostPerInterval:" + wb.resources.getMaxCost() + "]");

	}

	public static void RunCheapestPolicy(String WfFile, int res, int interval, int bandwidth) {
		// deadline = dl;
		// System.out.println("\n Cheapest Deadline=" + deadline);
		int startTime = 0;
		float cost;
		try {
			wb = new WorkflowBroker(WfFile, ScheduleType.Cheapest, interval, bandwidth, res);
		} catch (Exception e) {
			System.out.println(" Error in creating workflow broker!!!" + e.getLocalizedMessage());
		}
		CheapestCost = wb.schedule(startTime, 0);
		CheapestTime = wb.graph.getNodes().get(wb.graph.getEndId()).getAST();
		// System.out.println("Cheapest: cost="+CC+" time="+MC);
		System.out.println("Cheapest: cost=" + CheapestCost + " time=" + CheapestTime + " ResourceDetails[Mips: "
				+ wb.resources.getMinMIPS() + " CostPerInterval:" + wb.resources.getMinCost() + "]");

	}


	public static void RunCheapestCP(String WfFile, int res, int interval, int bandwidth) {
		// deadline = dl;
		// System.out.println("\n Cheapest Deadline=" + deadline);
		int startTime = 0;
		float cost;
		try {
			wb = new WorkflowBroker(WfFile, ScheduleType.CheapestCP, interval, bandwidth, res);
		} catch (Exception e) {
			System.out.println(" Error in creating workflow broker!!!" + e.getLocalizedMessage());
		}

		CheapestCpCost = wb.schedule(startTime, 0);
		CheapestCpTime = wb.graph.getNodes().get(wb.graph.getEndId()).getAST();
		// System.out.println("Cheapest: cost="+CC+" time="+MC);
		System.out.println("CheapestCp: cost=" + CheapestCpCost + " time=" + CheapestCpTime + " ResourceDetails[Mips: "
				+ wb.resources.getMinMIPS() + " CostPerInterval:" + wb.resources.getMinCost() + "]");

	}

	public static void RunFastestCP(String WfFile, int res, int interval, int bandwidth) {
		// deadline = dl;
		// System.out.println("\n Cheapest Deadline=" + deadline);
		int startTime = 0;
		float cost;
		try {
			wb = new WorkflowBroker(WfFile, ScheduleType.FastestCP, interval, bandwidth, res);
		} catch (Exception e) {
			System.out.println("Error in creating workflow broker!!!" + e.getLocalizedMessage());
		}

		FastestCPCost = wb.schedule(startTime, 0);
		FastestCPTime = wb.graph.getNodes().get(wb.graph.getEndId()).getAST();
		HeftTime = FastestCPTime;
		// System.out.println("Cheapest: cost="+CC+" time="+MC);
//		System.out.println(" FastestCP: cost=" + FastestCPCost + " time=" + FastestCPTime + " ResourceDetails[Mips: "
//				+ wb.resources.getMinMIPS() + " CostPerInterval:" + wb.resources.getMinCost() + "]");

	}


	public static void RunHeft(String WfFile, Boolean instancePrint, int res, int interval, int bandwidth) {
		System.out.println("-HEFT Algorithm ");

		int startTime = 0, finishTime, MH, MC;
		long realStartTime = 0, realFinishTime = 0;
		float cost, CC;
		try {
			wb = new WorkflowBroker(WfFile, ScheduleType.HEFT, interval, bandwidth, res);
		} catch (Exception e) {
		}
		// realStartTime = System.currentTimeMillis();
		cost = wb.schedule(startTime, 0);
		realFinishTime = System.currentTimeMillis();
		realFinishTime -= realStartTime;
		finishTime = wb.graph.getNodes().get(wb.graph.getEndId()).getEST();

		HeftTime = finishTime;
		HeftCost = cost;
		System.out.println(" HEFT: cost=" + cost + "$ time=" + finishTime + " NC=" + cost / CheapestCost + " Instances="
				+ wb.policy.instances.getSize() );

		
		if (instancePrint && (cost > 0)) {
			// Log.printInstancesFull(wb);
			instanceChart.PrintInstanceGantt(wb, WfFile + " HEFT cost=" + cost + " time=" + finishTime);
		}

//		utility.Utility.saveAlgorithmResult(ids, "HEFT", finishTime, cost, 0, 0, wb.policy.instances, 0, 0, 0,
//				HEFTAlgorithm.OverallResults.energy, HEFTAlgorithm.OverallResults.getTotalEnergyKillo(),
//				HEFTAlgorithm.OverallResults.reliability);

		System.out.println("");
	}

	public static void RunHeft(List <String> WfFiles, List<Long> starttimes, List<Long> dl, float costConstrain, Boolean instancePrint,
			int res, int interval, int bandwidth) {
		System.out.printf("\n HEFT Dynamic Deadline= ");
		for (long i : dl) {
			System.out.print(i + ",");
		}
		System.out.print("\n ");
		int finishTime, MH, MC;
		long realStartTime = 0, realFinishTime = 0;
		float cost, CC;
		try {
			wb = new WorkflowBroker(WfFiles, ScheduleType.HEFT, interval, bandwidth, res);
		} catch (Exception e) {
		}
		cost = wb.schedule(starttimes, dl, costConstrain);
		realFinishTime = System.currentTimeMillis();
		realFinishTime -= realStartTime;
		finishTime = HEFTAlgorithm.OverallResults.finishTime;
		long cB = (long) (costConstrain - cost);
		int util = Log.getUtilization(wb);
		System.err.print(" -HEFT Dynamic algorithm: Cost=" + cost + " time=" + finishTime + " Instances="
				+ wb.policy.instances.getSize() + " Energy=" + HEFTAlgorithm.OverallResults.getEnergyKillo() + " time="
				+ HEFTAlgorithm.OverallResults.finishTime 
				+ " UT:" + util + " Cost=" + HEFTAlgorithm.OverallResults.cost );
		

		if (instancePrint && (cost > 0)) {
			Log.printInstancesFull(wb);
			instanceChart.PrintInstanceGantt(wb, WfFiles + " HEFTAlgorithm cost=" + cost + " time=" + finishTime
					+   " Deadline=" + dl);
		}
		float NC = (float) cost / CheapestCost;
		float NT = (float) finishTime / HeftTime;
		System.out.println("");
	}



	public static void RunBDHEFT(String WfFile, int dl, float costConstrain, Boolean instancePrint,
			Boolean graphChartPrint, Boolean CriticalPathPrint, Boolean ResourceUsedPrint, int res, int interval,
			int bandwidth) {

		// System.out.println("-BDHEFT Schedule Deadline=" + deadline + " Cost
		// Constrained:"+ costConstrained);
		// System.out.println(" *******BDHEFT COST constrain:" + costConstrain + "
		// Deadline=" + dl);
		int startTime = 0, finishTime;

		float cost, CC;
		try {
			wb = new WorkflowBroker(WfFile, ScheduleType.BDHEFT, interval, bandwidth, res);
		} catch (Exception e) {
		}
		// realStartTime = System.currentTimeMillis();
		cost = wb.schedule(startTime, dl, costConstrain);
		int util = Log.getUtilization(wb);
		finishTime = wb.graph.getNodes().get(wb.graph.getEndId()).getEST();
		if ((costConstrain < cost) || (dl < finishTime))
			System.err.print(" BDHEFT: cost=" + cost + " time=" + finishTime + " NC=" + cost / CheapestCost
					+ " Instances=" + wb.policy.instances.getSize() + " UT:" + util);
		else
			System.out.print(" BDHEFT: cost=" + cost + " time=" + finishTime + " NC=" + cost / CheapestCost
					+ " Instances=" + wb.policy.instances.getSize() + " UT:" + util);
		
		if (dl < finishTime)
			System.out.print(" *{Failed Time}");
		// System.out.println("System Time=" + realFinishTime + " \n");
		// Log.printInstances(wb.policy.instances, wb.graph);

		if (instancePrint && (cost > 0)) {
			// Log.printInstancesFull(wb);
			instanceChart.PrintInstanceGantt(wb, WfFile + " BDHEFT cost=" + cost + " time=" + finishTime
					+ " COST constrain:" + costConstrain + " Deadline=" + dl);
		}
		if (graphChartPrint)
			Log.printWorkflowChartDetailed(wb, true, true, false, false);
		if (CriticalPathPrint)
			Log.printCritialPathes(wb);
		if (ResourceUsedPrint)
			Log.printResourcesUsed(wb);

		System.out.println("");
		float NC = (float) cost / CheapestCost;
		float NT = (float) finishTime / HeftTime;
		SimulationResults.criticalPaths.clear();
		String ids = dl + "-" + costConstrain + "-" + interval + "-" + WfFile;
		utility.Utility.saveAlgorithmResult(ids, "BDHEFT", finishTime, cost, NC, NT, wb.policy.instances, dl,
				costConstrain, util);
	}

	public static void RunMyBDHEFT(String WfFile, int dl, float costConstrain, Boolean instancePrint,
			Boolean graphChartPrint, Boolean CriticalPathPrint, Boolean ResourceUsedPrint, int res, int interval,
			int bandwidth) {

		// System.out.println("-BDHEFT Schedule Deadline=" + deadline + " Cost
		// Constrained:"+ costConstrained);
		// System.out.println(" *******My BDHEFT COST constrain:" + costConstrain + "
		// Deadline=" + dl);
		int startTime = 0, finishTime;

		float cost, CC;
		try {
			wb = new WorkflowBroker(WfFile, ScheduleType.MYBDHEFT, interval, bandwidth, res);
		} catch (Exception e) {
		}
		// realStartTime = System.currentTimeMillis();
		cost = wb.schedule(startTime, dl, costConstrain);
		int util = Log.getUtilization(wb);
		finishTime = wb.graph.getNodes().get(wb.graph.getEndId()).getEST();
		if ((costConstrain < cost) || (dl < finishTime))
			System.err.print(" My BDHEFT: cost=" + cost + " time=" + finishTime + " NC=" + cost / CheapestCost
					+ " Instances=" + wb.policy.instances.getSize() + " UT:" + util);
		else
			System.out.print(" My BDHEFT: cost=" + cost + " time=" + finishTime + " NC=" + cost / CheapestCost
					+ " Instances=" + wb.policy.instances.getSize() + " UT:" + util);

		
		if (dl < finishTime)
			System.out.print(" *{Failed Time}");
		// System.out.println("System Time=" + realFinishTime + " \n");
		// Log.printInstances(wb.policy.instances, wb.graph);

		if (instancePrint && (cost > 0)) {
			// Log.printInstancesFull(wb);
			instanceChart.PrintInstanceGantt(wb, WfFile + " MY BDHEFT cost=" + cost + " time=" + finishTime
					+ " COST constrain:" + costConstrain + " Deadline=" + dl);
		}
		if (graphChartPrint)
			Log.printWorkflowChartDetailed(wb, true, true, false, false);
		if (CriticalPathPrint)
			Log.printCritialPathes(wb);
		if (ResourceUsedPrint)
			Log.printResourcesUsed(wb);
		float NC = (float) cost / CheapestCost;
		float NT = (float) finishTime / HeftTime;
		System.out.println("");
		SimulationResults.criticalPaths.clear();
		String ids = dl + "-" + costConstrain + "-" + interval + "-" + WfFile;
		utility.Utility.saveAlgorithmResult(ids, "MyBDHEFT", finishTime, cost, NC, NT, wb.policy.instances, dl,
				costConstrain, util);

	}

	// energy and reliability of the algorithms.
	
	
	
	public static void RunEUSF_Ahmad_Dynamic(List <String> WfFiles, List<Long> starttimes, List<Long> dl,
			float costConstrain, Boolean instancePrint, int res, int interval, int bandwidth) {

		System.out.printf(" \n Dynamic EUSF   Deadline= ");
		for (Long i : dl) {
			System.out.print(i + ",");
			if (i==5) {
				System.out.print("and other" + (dl.size()-i));
				break;
			}
		}
		System.out.print("\n ");
		int finishTime, MH, MC;
		long realStartTime = 0, realFinishTime = 0;
		float cost, CC;
		try {
			wb = new WorkflowBroker(WfFiles, ScheduleType.EUSF, interval, bandwidth, res);
		} catch (Exception e) {
		}

		cost = wb.schedule(starttimes, dl, costConstrain);
		realFinishTime = System.currentTimeMillis();
		realFinishTime -= realStartTime;
		finishTime = EUSF.OverallResults.finishTime;

		long cB = (long) (costConstrain - cost);

		String strDVFS = "nonDVFS";
		if (EUSF.DVFSenabled)
			strDVFS = "-DVFS";

		if (EUSF.GAPfit)
			strDVFS += "-Gap";
		else
			strDVFS += "-NotGap";
		if (EUSF.FullFrequency)
			strDVFS += "-Full";
		float ut=Log.CalcInstancesFull(wb);
		EUSF.OverallResults.utilization=ut;
		EUSF.OverallResults.instanceSize=wb.policy.instances.getSize();
		System.err.print(" Ahmad -EUSF algorithm:  time=" + finishTime +" ut:" + ut + " Instances="
				+ wb.policy.instances.getSize() + " Energy=" + EUSF.OverallResults.getEnergyKillo() + " time="
				+ EUSF.OverallResults.finishTime 
				  + " Cost=" + EUSF.OverallResults.cost );

		System.out.println("Summery:");



		

		if (instancePrint && (cost > 0)) {
			Log.printInstancesFull(wb);
			instanceChart.PrintInstanceGantt(wb, wb.policy.currentAlgrotihm+ "ut: "+ut+ " " +WfFiles + strDVFS + " cost=" + cost + " time=" + finishTime
					+ " COST constrain:" + costConstrain + " Deadline=" + dl);
		}
		float NC = (float) cost / CheapestCost;
		float NT = (float) finishTime / HeftTime;
		System.out.println("");


	}
	public static void RunCUSF_Ahmad_Dynamic(List <String> WfFiles, List<Long> starttimes, List<Long> dl,
			float costConstrain, Boolean instancePrint, int res, int interval, int bandwidth) {

		System.out.printf(" \n Dynamic CUSF  Deadline= ");
		for (Long i : dl) {
			System.out.print(i + ",");
			if (i==5) {
				System.out.print("and other" + (dl.size()-i));
				break;
			}
		}
		System.out.print("\n ");
		int finishTime, MH, MC;
		long realStartTime = 0, realFinishTime = 0;
		float cost, CC;
		try {
			wb = new WorkflowBroker(WfFiles, ScheduleType.CUSF, interval, bandwidth, res);
		} catch (Exception e) {
		}

		cost = wb.schedule(starttimes, dl, costConstrain);
		realFinishTime = System.currentTimeMillis();
		realFinishTime -= realStartTime;
		finishTime = CUSF.OverallResults.finishTime;
//		long tB = (long) (dl - finishTime);
		long cB = (long) (costConstrain - cost);
		//int util = Log.getUtilization(wb);

		String strDVFS = "nonDVFS";
		if (CUSF.DVFSenabled)
			strDVFS = "-DVFS";

		if (CUSF.GAPfit)
			strDVFS += "-Gap";
		else
			strDVFS += "-NotGap";
		if (CUSF.FullFrequency)
			strDVFS += "-Full";
		float ut=Log.CalcInstancesFull(wb);
		CUSF.OverallResults.utilization=ut;
		CUSF.OverallResults.instanceSize=wb.policy.instances.getSize();
		System.err.print(" Ahmad -CUSF algorithm:  time=" + finishTime +" ut:" + ut + " Instances="
				+ wb.policy.instances.getSize() + " Energy=" + CUSF.OverallResults.getEnergyKillo() + " time="
				+ CUSF.OverallResults.finishTime 
				  + " Cost=" + CUSF.OverallResults.cost );

		System.out.println("Summery:");



		

		if (instancePrint && (cost > 0)) {
			Log.printInstancesFull(wb);
			instanceChart.PrintInstanceGantt(wb, wb.policy.currentAlgrotihm+ "ut: "+ut+ " " +WfFiles + strDVFS + " cost=" + cost + " time=" + finishTime
					+ " COST constrain:" + costConstrain + " Deadline=" + dl);
		}
		float NC = (float) cost / CheapestCost;
		float NT = (float) finishTime / HeftTime;
		System.out.println("");


	}


	public static void RunNOSF(List <String> WfFiles, List<Long> starttimes, List<Long> dl, float costConstrain,
			Boolean instancePrint, int res, int interval, int bandwidth) {
		System.out.printf("\n NOSF Dynamic  Deadline= ");
		for (Long i : dl) {
			System.out.print(i + ",");
			if (i==5) {
				System.out.print("and other" + (dl.size()-i));
				break;
			}
		}
		System.out.print("\n ");
		int finishTime, MH, MC;
		long realStartTime = 0, realFinishTime = 0;
		float cost, CC;
		try {
			wb = new WorkflowBroker(WfFiles, ScheduleType.NOSF, interval, bandwidth, res);
		} catch (Exception e) {
		}
		cost = wb.schedule(starttimes, dl, costConstrain);
		realFinishTime = System.currentTimeMillis();
		realFinishTime -= realStartTime;
		finishTime = NOSF.OverallResults.finishTime;
		long cB = (long) (costConstrain - cost);
		int util = Log.getUtilization(wb);
		float ut=Log.CalcInstancesFull(wb);
		NOSF.OverallResults.utilization=ut;
		NOSF.OverallResults.instanceSize=wb.policy.instances.getSize();
		System.err.print(" -NOSF algorithm: time=" + finishTime + " ut:" + ut + " Instances="
				+ wb.policy.instances.getSize() + " Energy=" + NOSF.OverallResults.getEnergyKillo() + " time="
				+ NOSF.OverallResults.finishTime  + " UT:" + util
				+ " Cost=" + NOSF.OverallResults.cost );

		System.err.print(" -NOSF algorithm: " +  " time=" + finishTime + " Instances="
				+ wb.policy.instances.getSize() + " Energy=" + NOSF.OverallResults.getEnergyKillo() + " time="
				+ NOSF.OverallResults.finishTime  + " UT:" + util
				+ " Cost=" + NOSF.OverallResults.cost );

		System.out.println("Summery:");

//		for (int i = 0; i < WfFiles.length; i++) {
//			System.out.println("NOSF: " + WfFiles[i] + " dl: " + NOSF.OverallResults.deadlines.get(i) + " ft:"
//					+ NOSF.OverallResults.finishTimes.get(i));
//		}
		
		
		
		if (instancePrint && (cost > 0)) {
			ut=Log.printInstancesFull(wb);
			instanceChart.PrintInstanceGantt(wb, wb.policy.currentAlgrotihm+ " ut: "+util+" v2:"+ut+ " "+ WfFiles + "  cost=" + cost + " time=" + finishTime
					 + " Deadline=" + dl);
		}
		float NC = (float) cost / CheapestCost;
		float NT = (float) finishTime / HeftTime;
		System.out.println("");
	}



}
