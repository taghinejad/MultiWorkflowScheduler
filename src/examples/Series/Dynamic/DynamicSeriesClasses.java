package examples.Series.Dynamic;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.Set;

import Broker.WorkflowPolicy;
import Broker.result;
import algorithms.multiworkflow.CUSF;
import algorithms.multiworkflow.EUSF;
import algorithms.multiworkflow.NOSF;
import executionClasses.executionClass;
import executionClasses.executionEnergySenario.Algorithm;
import utility.Utility;
import utility.configuration;

public class DynamicSeriesClasses {
	public static List<List<result>> SRS;
	public static List<String> workloads;

	public static int wfreturn(int wfsize, int i) {
		return i % wfsize + 1;
	}

	public static void RunProDynamicPossionWorkload(Algorithm[] algs, double unCertainDeviation, double HeftCoefficient,
			int workloadnum, int wfsize, double arrival, Boolean instprint, float fluctpercent) {

		int bandwidth = 100, res = 2021, interval = 3600;
		bandwidth *= 1000000;
		WorkflowPolicy.fluctpercent = fluctpercent;
		System.out.println("deviation: " + unCertainDeviation + " heftCoeff:" + HeftCoefficient + " arrival:" + arrival
				+ " workload:" + workloadnum + " wfSize:" + wfsize + " bw: " + " res:" + res + " interval: "
				+ interval);
		// 2021 Online Multi-Workflow Scheduling

		Integer wfSize = wfsize;

		List<String> wfs = new ArrayList<String>();

		int i = 0;
		Boolean staticworkflow = false;
		Random rn = new Random();
		System.out.println("starting configuring workflows: " + workloadnum);
		int w, s = 0;
		int j = 0;
		if (staticworkflow) {
			while (i < workloadnum) {

				wfSize = wfreturn(wfsize, j);
				wfs.add(Utility.returnDaxMontage(wfSize));
				i++;

				wfs.add(Utility.returnDaxEpigenomics(wfSize));
				i++;

				wfs.add(Utility.returnDaxSipht(wfSize));
				i++;

				wfs.add(Utility.returnDaxInspiral(wfSize));
				i++;

				wfs.add(Utility.returnDaxCyberShake(wfSize));
				i++;

				j++;
			}
		} else {
			while (i < workloadnum) {
				w = rn.nextInt(5);
				s = rn.nextInt(wfsize) + 1;
				switch (w) {
				case 0:
					wfs.add(Utility.returnDaxMontage(s));
					i++;
					break;
				case 1:
					wfs.add(Utility.returnDaxEpigenomics(s));
					i++;
					break;
				case 2:
					wfs.add(Utility.returnDaxSipht(s));
					i++;
					break;
				case 3:
					wfs.add(Utility.returnDaxInspiral(s));
					i++;
					break;
				case 4:
					wfs.add(Utility.returnDaxCyberShake(s));
					i++;
					break;
				}

			}
		}

		workloads = wfs;
		System.out.println("starting running algorithms");
		ProDynamicPossion(algs, wfs, res, interval, bandwidth, (float) unCertainDeviation, (float) HeftCoefficient,
				arrival, instprint);
	}

	public static void ProDynamic(Algorithm[] algs, List<String> wfs, int res, int interval, int bandwidth,
			float unCertainDeviation, float heftCoefficient, Boolean instprint) {
		List<Long> deadline = new ArrayList<Long>();
		List<Long> starts = new ArrayList<Long>();
		if (unCertainDeviation > 0) {
			configuration.EnableUncertainTaskExecution((float) 0.2);
		}
		long heftTime;
		for (String wf : wfs) {
			executionClass.RunHeft(wf, false, res, interval, bandwidth);
			heftTime = algorithms.NoConstrained.HEFTAlgorithm.OverallResults.finishTime;
			deadline.add((long) Math.round(heftTime * heftCoefficient));
			starts.add((long) 0);

		}
		printStartUpConfig(algs, wfs, res, interval, bandwidth, unCertainDeviation, heftCoefficient, starts, deadline);
		runAlgorithmsDynamic(wfs, starts, deadline, 1, bandwidth, res, interval, algs, instprint);

	}

	public static void ProDynamicPossion(Algorithm[] algs, List<String> wfs, int res, int interval, int bandwidth,
			float unCertainDeviation, float heftCoefficient, double arrival, Boolean instprint) {
		List<Long> deadline = new ArrayList<Long>();
		List<Long> starts = new ArrayList<Long>();
		if (unCertainDeviation > 0) {
			configuration.EnableUncertainTaskExecution((float) 0.2);
		}
		bandwidth/=8;
		long dl, st;
		long heftTime;

		for (int i = 0; i < wfs.size(); i++) {

			executionClass.RunFastestCP(wfs.get(i), res, interval, bandwidth);
			heftTime = executionClass.HeftTime;

			// follows a poisson distribution
			st = (long) (Math.ceil(i * (1 / arrival)) * 60);
			// st=configuration.GetPoissonRandom(100);
			deadline.add(st + (long) Math.round(heftTime * heftCoefficient));
			starts.add((long) st);
		}

		printStartUpConfig(algs, wfs, res, interval, bandwidth, unCertainDeviation, heftCoefficient, starts, deadline);
		runAlgorithmsDynamic(wfs, starts, deadline, 1, bandwidth, res, interval, algs, instprint);

	}

	private static void printStartUpConfig(Algorithm[] algs, List<String> wfs, int res, int interval, int bandwidth,
			float unCertainDeviation, float heftCoefficient, List<Long> starts, List<Long> deadline) {
		System.out.println(
				"||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
		System.out.println("resource: " + res + " interval:" + interval + " unceratin:" + unCertainDeviation
				+ " heftCoefficient: " + heftCoefficient);
		System.out.print(" Algorithms: ");
		for (Algorithm al : algs) {
			System.out.print(al.toString() + " ,");
		}
		System.out.println();
		for (int i = 0; i < wfs.size(); i++) {
			if (i == 5)
				break;
			System.out.println(
					"Workflows(" + i + ") " + wfs.get(i) + " 	 ST:" + starts.get(i) + "   DL:" + deadline.get(i));
		}
		System.out.println("and other " + (wfs.size() - 5) + " \n"
				+ "||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");

	}

	public static void ProDynamic(Algorithm[] algs, List<String> wfs, int res, int interval, int bandwidth,
			float unCertainDeviation, float heftCoefficient, long[] deadlines, long[] startss, Boolean instprint) {
		List<Long> deadline = new ArrayList<Long>();
		List<Long> starts = new ArrayList<Long>();
		for (int i = 0; i < startss.length; i++) {
			deadline.add(deadlines[i]);
			starts.add(startss[i]);
		}

		if (unCertainDeviation > 0) {
			configuration.EnableUncertainTaskExecution((float) 0.2);
		}
		printStartUpConfig(algs, wfs, res, interval, bandwidth, unCertainDeviation, heftCoefficient, starts, deadline);
		runAlgorithmsDynamic(wfs, starts, deadline, 1, bandwidth, res, interval, algs, instprint);

	}

	public static void runAlgorithmsDynamic(List<String> wfs, List<Long> starts, List<Long> deadlines, float cost,
			int bandwidth, int res, int interval, Algorithm[] algs, Boolean instprint) {

		List<result> SR = new ArrayList<result>();
		for (Algorithm algorithm : algs) {
			if (algorithm == algorithm.EUSF) {
				executionClass.RunEUSF_Ahmad_Dynamic(wfs, starts, deadlines, cost, instprint, res, interval, bandwidth);
				SR = new ArrayList<result>();
				SR.add(EUSF.OverallResults);
				SRS.add(SR);
			} else if (algorithm == algorithm.CUSF) {
				executionClass.RunCUSF_Ahmad_Dynamic(wfs, starts, deadlines, cost, instprint, res, interval, bandwidth);
				SR = new ArrayList<result>();
				SR.add(CUSF.OverallResults);
				SRS.add(SR);
			} else if (algorithm == algorithm.NOSF) {
				executionClass.RunNOSF(wfs, starts, deadlines, cost, instprint, res, interval, bandwidth);
				SR = new ArrayList<result>();
				SR.add(NOSF.OverallResults);
				SRS.add(SR);
			}
		}

	}

	public static void PrintSummeries(Boolean perworkflow) {
		System.out.println("Summeries::");
		int missedDeadline = 0;
		int Total = 0, count = 0;
		float AvgEnergy = 0, AvgCost = 0, avgTimeRatio = 0;
		float succRate;

		Hashtable<String, List<result>> Sumup = new Hashtable<>();

		for (List<result> res : SRS) {
			missedDeadline = 0;
			Total = 0;
			AvgEnergy = 0;
			AvgCost = 0;
			avgTimeRatio = 0;
			for (result result : res) {

				for (int i = 0; i < workloads.size(); i++) {

					Total++;
					if (perworkflow)
						if (result.deadlines.get(i) < result.finishTimes.get(i)) {
							System.err.println(result.algorithmName + "  DL: " + result.deadlines.get(i) + "     Ft:"
									+ result.finishTimes.get(i) + " st:" + result.starts.get(i) + "  "
									+ workloads.get(i));
							missedDeadline++;
						} else {
							System.out.println(result.algorithmName + "  DL: " + result.deadlines.get(i) + "     Ft:"
									+ result.finishTimes.get(i) + " st:" + result.starts.get(i) + "  "
									+ workloads.get(i));
						}

					avgTimeRatio += (float) result.finishTimes.get(i) / result.deadlines.get(i);
				}
				AvgEnergy += result.getTotalEnergyKillo();
				AvgCost += result.cost;
				if (perworkflow)
					System.out.println(result.algorithmName + " =>    #Energy:" + result.getTotalEnergyKillo()
							+ " # Cost: " + result.cost);

				try {
					Sumup.get(result.algorithmName).add(result);
				} catch (Exception e) {

					Sumup.put(result.algorithmName, new ArrayList<result>() {
					});
					Sumup.get(result.algorithmName).add(result);
				}

			}

			succRate = (float) (Total - missedDeadline) * 100 / Total;
			avgTimeRatio = avgTimeRatio / Total;

		}

		// Print sumup
		Set<String> keys = Sumup.keySet();
		Enumeration names;
		names = Sumup.keys();
		String str;
		System.err.println("------------------------------------------------------");
		System.err.println("------------------------------------------------------");
		System.out.println("To Sumup:" + " wf num: " + Total);
		while (names.hasMoreElements()) {

			str = (String) names.nextElement();
			missedDeadline = 0;
			Total = 0;
			AvgEnergy = 0;
			AvgCost = 0;
			avgTimeRatio = 0;
			Total = 0;
			count = 0;
			for (result rs : Sumup.get(str)) {
				AvgCost += rs.cost;
				AvgEnergy += rs.getTotalEnergyKillo();
				for (int i = 0; i < workloads.size(); i++) {
					Total++;
					if (rs.deadlines.get(i) < rs.finishTimes.get(i))
						missedDeadline++;
					avgTimeRatio += (float) rs.finishTimes.get(i) / rs.deadlines.get(i);
				}

			}

			succRate = (float) (Total - missedDeadline) * 100 / Total;
			avgTimeRatio = (float) avgTimeRatio / Total;
			System.out.println(str + "=>SuccRate:" + succRate + "%    missedDeadline:" + missedDeadline + " total:"
					+ workloads.size() + " energy:" + AvgEnergy + " cost:" + AvgCost + " AvgEnergy:"
					+ Math.round((float) AvgEnergy / Sumup.get(str).size()) + " AvgCost:"
					+ Math.round((float) AvgCost / Sumup.get(str).size()));
		}
	}

}
