
package executionClasses;

import utility.ConsoleColors;

import utility.ResultDB;
import utility.Utility;
import examples.*;

import executionClasses.executionEnergySenario.SenarioBase;
import executionClasses.executionEnergySenario.Tsize;
import executionClasses.executionEnergySenario.Workflow;

import java.util.ArrayList;
import java.util.List;

import javax.swing.text.StyledEditorKit.BoldAction;

import Broker.File;
import Broker.Log;
import Broker.ScheduleType;
import Broker.WorkflowBroker;
import Broker.WorkflowGraph;
import Broker.WorkflowBroker.ResourceProvision;


import algorithms.NoConstrained.CheapestCP;
import algorithms.NoConstrained.CheapestPolicy;
import algorithms.NoConstrained.FastestCP;
import algorithms.NoConstrained.FastestPolicy;
import algorithms.NoConstrained.HEFTAlgorithm;
import algorithms.NoConstrained.MyCheapestPolicy;
import algorithms.NoConstrained.MyFast;


public class executionEnergySenario {

	public static boolean MIPSandDATAchanged = false;
	public static boolean instancePrint = false;

	public enum SenarioBase {
		PCP, costDeadlineRange, Mine2, DeadlineBase
	}

	public enum CCRstatus {
		Enabled, Disabled
	}

	public static Algorithm[] algorithmsRun;

	public enum Algorithm {
		REEW, IC_PCP, CbCR, HEFT, BDHEFT, NOSF,CTD,EUSF,CUSF;

		public static Boolean Contains(String val) {
			for (Algorithm algs : values()) {
				if (algs.name() == val)
					return true;
			}
			return false;
		}
	}

	public enum Workflow {
		ALL, Montage, Sipht, Epigenomics, Inspiral, Cybershake
	}

	public enum Tsize {
		VerySmall, Small, Mediom, Large

	}

	// public enum ResourceProvision {
	// List, EC2, EC2v2, Sample1, EC2ArabNejad, EC2020
	// }

	public static List<String> BindWorkflows(Workflow workflows, Tsize taskSize) {
		int taskSeries = 1;
		if (taskSize == Tsize.VerySmall)
			taskSeries = 1;
		else if (taskSize == Tsize.Small)
			taskSeries = 2;
		else if (taskSize == Tsize.Mediom)
			taskSeries = 3;
		else if (taskSize == Tsize.Large)
			taskSeries = 4;

		List<String> wfs = new ArrayList();
		if (workflows == Workflow.Montage)
			wfs.add(Utility.returnDaxMontage(taskSeries));
		else if (workflows == Workflow.Sipht)
			wfs.add(Utility.returnDaxSipht(taskSeries));
		else if (workflows == Workflow.Epigenomics)
			wfs.add(Utility.returnDaxEpigenomics(taskSeries));
		else if (workflows == Workflow.Cybershake)
			wfs.add(Utility.returnDaxCyberShake(taskSeries));
		else if (workflows == Workflow.Inspiral)
			wfs.add(Utility.returnDaxInspiral(taskSeries));
		else {
			wfs.add(Utility.returnDaxMontage(taskSeries));
			wfs.add(Utility.returnDaxSipht(taskSeries));
			wfs.add(Utility.returnDaxCyberShake(taskSeries));
			wfs.add(Utility.returnDaxEpigenomics(taskSeries));
			wfs.add(Utility.returnDaxInspiral(taskSeries));
		}
		return wfs;
	}

	



	public static void runAlgorithmsDynamic(List <String> wfs, List<Long> starts, List<Long> deadlines, float cost, int bandwidth,
			int res, int interval, Algorithm[] algs) {

		for (Algorithm algorithm : algs) {
			if (algorithm == algorithm.EUSF)
				executionClass.RunEUSF_Ahmad_Dynamic(wfs, starts, deadlines, cost, instancePrint, res, interval,
						bandwidth);
			if (algorithm == algorithm.CUSF)
				executionClass.RunCUSF_Ahmad_Dynamic(wfs, starts, deadlines, cost, instancePrint, res, interval,
						bandwidth);
			else if (algorithm == algorithm.NOSF)
				executionClass.RunNOSF(wfs, starts, deadlines, cost, instancePrint, res, interval, bandwidth);
			
			
		}

	}


}
