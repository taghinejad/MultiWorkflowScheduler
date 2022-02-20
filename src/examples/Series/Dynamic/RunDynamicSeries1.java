package examples.Series.Dynamic;

import java.util.ArrayList;

import java.util.List;

import Broker.result;

import algorithms.multiworkflow.CUSF;
import algorithms.multiworkflow.EUSF;

import executionClasses.executionEnergySenario.Algorithm;

public class RunDynamicSeries1 {

	public static void main(String arg[]) throws InterruptedException {
		for (int i = 0; i < 1; i++) {
			DynamicSeriesClasses.SRS = new ArrayList<List<result>>();

			// Runs a Series of dynamic multi workflow algorithms based on arrival time of poisson.

			utility.configuration.setPrintGapAlert(false);
			utility.configuration.setPrintGapInsertation(false);
			EUSF.GAPfit = true;
			CUSF.GAPfit = true;

			double unCertainDeviation = 0.2, HeftCoefficient = 2, arrival = 1;
			int workloadnum = 100, wfTasksize = 3;
			Boolean instprint = false;

			Algorithm[] algs = { Algorithm.CUSF };
			// Algorithm.ECTDalgorithm, Algorithm.NOSF,Algorithm.ERES_Neha,Algorithm.CTD

			DynamicSeriesClasses.RunProDynamicPossionWorkload(algs, unCertainDeviation, HeftCoefficient, workloadnum,
					wfTasksize, arrival, instprint, (float) 1.5);

			DynamicSeriesClasses.PrintSummeries(false);
		}

	}

}
