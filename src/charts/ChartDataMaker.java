package charts;

import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.naming.spi.DirectoryManager;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.CombinedDomainCategoryPlot;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import utility.AlgorithmResult;
import utility.ResultDB;
import utility.Utility;
import utility.*;

public class ChartDataMaker extends ApplicationFrame {
	
	// rangevaluePrint is used to declare if it is needed to be printed time ratio quality ratio and cost ratio in different ranges.
	public  static boolean rangeValuePrint = true;
	public  static boolean NormalizedRangeValue = true;
	
	//if success Quality is true, then for time>deadline or cost>Budget adds Quality= TimeRate+CostRate+1 /2
	public static boolean successQuality=false;
	// public static CategoryDataset createDatasetCost(int index) {
	// final DefaultCategoryDataset ds = new DefaultCategoryDataset();
	// ResultDB rs = Utility.dbs.get(index);
	// for (AlgorithmResult as : rs.algorithms) {
	// if ((as.cost < (rs.getCostConstrained() * 3)) && (as.makespan <
	// (rs.getTimeConstrained() * 3)))
	// ds.addValue(as.cost, as.name, "Cost");
	// }
	// return ds;
	// }


	private ChartDataMaker(String title) {
		super(title);
		// TODO Auto-generated constructor stub
	}

	public static void SaveBarCostTimeCharts(Boolean Cost, Boolean Time) {
		// results of charts are saved on resultCharts folder
		String FolderName = FolderMaker();
		if (Time)
			saveChartsTime(FolderName + "/");
		if (Cost)
			saveChartsCost(FolderName + "/");
	}

	public static void printCostTimeBarCharts(Boolean Cost, Boolean Time) {
		// results of charts are saved on resultCharts folder
		if (Time)
			printTimesBarChart();
		if (Cost)
			printCostsBarChart();
	}

	private static void printCostsBarChart() {
		float count = 0;
		float plus = ((float) (100) / utility.Utility.dbs.size()) / 100;
		for (ResultDB rs : utility.Utility.dbs) {
			if (rs.algorithms.size() < 1)
				continue;
			String str = "Cost CHART x" + rs.getXcost() + " Wf:" + rs.getWorkflow() + " CostC: " + rs.getCostConstrained()
					+ " TimeC: " + rs.getTimeConstrained() + "  int: " + rs.getInterval() + "  BW:" + rs.getBandwidth();
			ChartMakerCost chart = new ChartMakerCost(str, str, rs);

			chart.pack();
			RefineryUtilities.positionFrameOnScreen(chart, count, 0.0);
			// RefineryUtilities.positionFrameRandomly(chart);
			chart.setVisible(true);
			count += plus;
		}
	}

	private static void printTimesBarChart() {
		float count = 0;
		float plus = ((float) (100) / utility.Utility.dbs.size()) / 100;
		for (ResultDB rs : utility.Utility.dbs) {
			if (rs.algorithms.size() < 1)
				continue;
			String str = "TIME CHART x" + rs.getXcost() + " Wf:" + rs.getWorkflow() + " CostC: " + rs.getCostConstrained()
					+ " TimeC: " + rs.getTimeConstrained() + "  int: " + rs.getInterval() + "  BW:" + rs.getBandwidth();

			ChartMakerTime chart = new ChartMakerTime(str, str, rs);
			chart.pack();
			RefineryUtilities.positionFrameOnScreen(chart, count, 0.6);
			// RefineryUtilities.positionFrameRandomly(chart);
			// RefineryUtilities.centerFrameOnScreen( chart );
			chart.setVisible(true);

			count += plus;
		}
	}

	private static String FolderMaker() {

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss");
		Date date = new Date();
		String dateStr = formatter.format(date);
		String FolderName = "resultCharts/" + dateStr;
		File nf = new File(FolderName);
		nf.mkdir();

		for (ResultDB rs : utility.Utility.dbs) {

			String wf = rs.getWorkflow().substring(rs.getWorkflow().indexOf("/") + 1, rs.getWorkflow().indexOf("."));
			File ff = new File(FolderName + "/" + wf);
			ff.mkdir();
		}

		return nf.getPath();
	}

	private static String XYFolderMaker(String title) {

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss");
		Date date = new Date();
		String dateStr = title + formatter.format(date);
		String FolderName = "resultCharts/" + dateStr;
		File nf = new File(FolderName);
		nf.mkdir();
		return nf.getPath();
	}

	private static void saveChartsCost(String FolderName) {
		for (ResultDB rs : utility.Utility.dbs) {
			String str = "COST CHART x" + rs.getXcost() + " Wf:" + rs.getWorkflow() + " CostC: " + rs.getCostConstrained()
					+ " TimeC: " + rs.getTimeConstrained() + "  int: " + rs.getInterval() + "  BW:" + rs.getBandwidth();
			JFreeChart barChart = ChartFactory.createBarChart(str, "Algorithms", "COST",
					ChartMakerCost.createDatasetCost(rs), PlotOrientation.VERTICAL, true, true, false);

			Random rn = new Random();
			String wf = rs.getWorkflow().substring(rs.getWorkflow().indexOf("/") + 1, rs.getWorkflow().indexOf("."));

			String fn = FolderName + "/" + wf + "/" + "X" + rs.getXcost() + " Cost_" + wf + "_" + "I-" + rs.getInterval();

			try {
				SaveChartJPG(barChart, fn);
			} catch (IOException e) {
				// TODO: handle exception
				System.out.print(e.toString());
			}
		}
	}

	private static void saveChartsTime(String FolderName) {
		for (ResultDB rs : utility.Utility.dbs) {
			String str = "TIME CHART  x" + rs.getXcost() + " Wf:" + rs.getWorkflow() + " CostC: " + rs.getCostConstrained()
					+ " TimeC: " + rs.getTimeConstrained() + "  int: " + rs.getInterval() + "  BW:" + rs.getBandwidth();
			JFreeChart barChart = ChartFactory.createBarChart(str, "Algorithms", "TIME",
					ChartMakerTime.createDatasetTime(rs), PlotOrientation.VERTICAL, true, true, false);
			// String fn="TimeChart:"+rs.getWorkflow()+"T:"+rs.getTimeConstrained()+"
			// C:"+rs.getCostConstrained()+"x"+rs.getXcost();
			Random rn = new Random();
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss");
			Date date = new Date();

			String dateStr = formatter.format(date);

			String wf = rs.getWorkflow().substring(rs.getWorkflow().indexOf("/") + 1, rs.getWorkflow().indexOf("."));

			String fn = FolderName + "/" + wf + "/" + "X" + rs.getXcost() + " Time_" + wf + "_" + "I-" + rs.getInterval();
			try {
				SaveChartJPG(barChart, fn);
			} catch (IOException e) {
				// TODO: handle exception
				System.out.print(e.toString());
			}

		}
	}

	private static void SaveChartJPG(JFreeChart barChart, String fileName) throws IOException {
		int width = 1024; /* Width of the image */
		int height = 800; /* Height of the image */

		File BarChart = new File(fileName + ".jpg");

		ChartUtilities.saveChartAsJPEG(BarChart, barChart, width, height);
	}

	public static void printXYCharts() {
		// prints cost and time of each workflow on deffrent environment
		float count = 0;
		float plus = ((float) (100) / utility.Utility.dbs.size()) / 100;
		List<Integer> intervals = new ArrayList<Integer>();
		List<String> wflows = new ArrayList<String>();
		List<ResultDB> ldb = new ArrayList<ResultDB>();

		for (ResultDB rs : utility.Utility.dbs) {
			if (!intervals.contains(rs.getInterval())) {
				intervals.add(rs.getInterval());
			}
			if (!wflows.contains(rs.getWorkflow().trim()))
				wflows.add(rs.getWorkflow());
		}
		int curInt;
		String curWf;

		float loc = 0;

		float locplus = ((float) (100) / (intervals.size() + wflows.size())) / 100;

		for (int i = 0; i < intervals.size(); i++) {
			for (int j = 0; j < wflows.size(); j++) {
				ldb.clear();
				for (ResultDB rs : utility.Utility.dbs) {
					curInt = intervals.get(i);
					curWf = wflows.get(j);
					if (rs.getInterval() == curInt && curWf.contains(rs.getWorkflow()))
						ldb.add(rs);
				}

				ChartMakerTime chartT = new ChartMakerTime(
						"Time interval: " + ldb.get(0).getInterval() + " wf:" + ldb.get(0).getWorkflow(),
						"Time interval: " + ldb.get(0).getInterval() + " wf:" + ldb.get(0).getWorkflow(), ldb);
				ChartMakerCost chartC = new ChartMakerCost(
						"Cost interval: " + ldb.get(0).getInterval() + " wf:" + ldb.get(0).getWorkflow(),
						"Cost interval: " + ldb.get(0).getInterval() + " wf:" + ldb.get(0).getWorkflow(), ldb);

				chartC.pack();
				RefineryUtilities.positionFrameOnScreen(chartC, loc, 0.0);

				chartC.setVisible(true);

				chartT.pack();

				RefineryUtilities.positionFrameOnScreen(chartT, loc, 0.6);
				chartT.setVisible(true);

				loc += locplus;
			}
		}

	}

	public static void printTimeCostRatioCharts() {
		// prints cost and time of each workflow on deffrent environment
		float count = 0;
		float plus = ((float) (100) / utility.Utility.dbs.size()) / 100;
		List<Integer> intervals = new ArrayList<Integer>();
		List<String> wflows = new ArrayList<String>();
		List<ResultDB> ldb = new ArrayList<ResultDB>();
		List<Float> BudgetFactors = new ArrayList<Float>();
		List<Float> DeadlineFactors = new ArrayList<Float>();
		for (ResultDB rs : utility.Utility.dbs) {
			if (!intervals.contains(rs.getInterval())) {
				intervals.add(rs.getInterval());
			}
			if (!wflows.contains(rs.getWorkflow().trim()))
				wflows.add(rs.getWorkflow());
			if (!BudgetFactors.contains(rs.getXcost()))
				BudgetFactors.add(rs.getXcost());
			if (!DeadlineFactors.contains(rs.getXtime()))
				DeadlineFactors.add(rs.getXtime());
		}
		int curInt;
		float curF = 0;
		String curWf;

		float loc = 0;

		float locplus = ((float) (100) / (intervals.size() + wflows.size())) / 100;

		for (int i = 0; i < intervals.size(); i++) {
			for (int j = 0; j < wflows.size(); j++) {

				ldb.clear();
				for (ResultDB rs : utility.Utility.dbs) {
					curInt = intervals.get(i);
					curWf = wflows.get(j);

					if (rs.getInterval() == curInt && curWf.contains(rs.getWorkflow()))
						ldb.add(rs);
				}

				ChartMakerTime chartT = new ChartMakerTime(
						"Time interval: " + ldb.get(0).getInterval() + " wf:" + ldb.get(0).getWorkflow(),
						"Time interval: " + ldb.get(0).getInterval() + " wf:" + ldb.get(0).getWorkflow(), ldb,
						BudgetFactors, DeadlineFactors);
				ChartMakerCost chartC = new ChartMakerCost(
						"Cost interval: " + ldb.get(0).getInterval() + " wf:" + ldb.get(0).getWorkflow(),
						"Cost interval: " + ldb.get(0).getInterval() + " wf:" + ldb.get(0).getWorkflow(), ldb,
						BudgetFactors, DeadlineFactors);

				chartC.pack();
				RefineryUtilities.positionFrameOnScreen(chartC, loc, 0.0);

				chartC.setVisible(true);

				chartT.pack();

				RefineryUtilities.positionFrameOnScreen(chartT, loc, 0.6);
				chartT.setVisible(true);

				loc += locplus;

			}
		}

	}

	public static void printQosRatioCharts() {
		// prints cost and time of each workflow on deffrent environment
		float count = 0;
		float plus = ((float) (100) / utility.Utility.dbs.size()) / 100;
		List<Integer> intervals = new ArrayList<Integer>();
		List<String> wflows = new ArrayList<String>();
		List<ResultDB> ldb = new ArrayList<ResultDB>();
		List<Float> BudgetFactors = new ArrayList<Float>();
		List<Float> DeadlineFactors = new ArrayList<Float>();
		for (ResultDB rs : utility.Utility.dbs) {
			if (!intervals.contains(rs.getInterval())) {
				intervals.add(rs.getInterval());
			}
			if (!wflows.contains(rs.getWorkflow().trim()))
				wflows.add(rs.getWorkflow());
			if (!BudgetFactors.contains(rs.getXcost()))
				BudgetFactors.add(rs.getXcost());
			if (!DeadlineFactors.contains(rs.getXtime()))
				DeadlineFactors.add(rs.getXtime());
		}
		int curInt;
		float curF = 0;
		String curWf;

		float loc = 0;

		float locplus = ((float) (100) / (intervals.size() + wflows.size())) / 100;

		for (int i = 0; i < intervals.size(); i++) {
			for (int j = 0; j < wflows.size(); j++) {

				ldb.clear();
				for (ResultDB rs : utility.Utility.dbs) {
					curInt = intervals.get(i);
					curWf = wflows.get(j);

					if (rs.getInterval() == curInt && curWf.contains(rs.getWorkflow()))
						ldb.add(rs);
				}

				ChartMakerTime chartT = new ChartMakerTime(
						"QosRatio interval: " + ldb.get(0).getInterval() + " wf:" + ldb.get(0).getWorkflow(),
						"QosRatio interval: " + ldb.get(0).getInterval() + " wf:" + ldb.get(0).getWorkflow(), ldb,
						BudgetFactors, DeadlineFactors, true);
				ChartMakerCost chartC = new ChartMakerCost(
						"QosRatio interval: " + ldb.get(0).getInterval() + " wf:" + ldb.get(0).getWorkflow(),
						"QosRatio interval: " + ldb.get(0).getInterval() + " wf:" + ldb.get(0).getWorkflow(), ldb,
						BudgetFactors, DeadlineFactors, true);
				utility.Excel.writeExcel.writeExcelBasedBudgetFactorBoxChart(ldb, BudgetFactors, DeadlineFactors);
				utility.Excel.writeExcel.writeExcelBasedDeadlineFactorBoxChart(ldb, BudgetFactors, DeadlineFactors);

				chartC.pack();
				RefineryUtilities.positionFrameOnScreen(chartC, loc, 0.0);

				chartC.setVisible(true);

				chartT.pack();

				RefineryUtilities.positionFrameOnScreen(chartT, loc, 0.6);
				chartT.setVisible(true);

				loc += locplus;

			}
		}

	}

	public static void printSuccessRateCharts() {
		// prints cost and time of each workflow on deffrent environment
		float count = 0;
		float plus = ((float) (100) / utility.Utility.dbs.size()) / 100;
		List<Integer> intervals = new ArrayList<Integer>();
		List<String> wflows = new ArrayList<String>();
		List<ResultDB> ldb = new ArrayList<ResultDB>();
		List<Float> BudgetFactors = new ArrayList<Float>();
		List<Float> DeadlineFactors = new ArrayList<Float>();
		for (ResultDB rs : utility.Utility.dbs) {
			if (!intervals.contains(rs.getInterval())) {
				intervals.add(rs.getInterval());
			}
			if (!wflows.contains(rs.getWorkflow().trim()))
				wflows.add(rs.getWorkflow());
			if (!BudgetFactors.contains(rs.getXcost()))
				BudgetFactors.add(rs.getXcost());
			if (!DeadlineFactors.contains(rs.getXtime()))
				DeadlineFactors.add(rs.getXtime());
		}
		int curInt;
		float curF = 0;
		String curWf;

		float loc = 0;

		float locplus = ((float) (100) / (intervals.size() + wflows.size())) / 100;

		for (int i = 0; i < intervals.size(); i++) {
			for (int j = 0; j < wflows.size(); j++) {

				ldb.clear();
				for (ResultDB rs : utility.Utility.dbs) {
					curInt = intervals.get(i);
					curWf = wflows.get(j);

					if (rs.getInterval() == curInt && curWf.contains(rs.getWorkflow()))
						ldb.add(rs);
				}

				ChartMakerTime chartT = new ChartMakerTime(
						"SR interval: " + ldb.get(0).getInterval() + " wf:" + ldb.get(0).getWorkflow(),
						"SR interval: " + ldb.get(0).getInterval() + " wf:" + ldb.get(0).getWorkflow(), ldb,
						BudgetFactors, DeadlineFactors, true,true);
				ChartMakerCost chartC = new ChartMakerCost(
						"SR interval: " + ldb.get(0).getInterval() + " wf:" + ldb.get(0).getWorkflow(),
						"SR interval: " + ldb.get(0).getInterval() + " wf:" + ldb.get(0).getWorkflow(), ldb,
						BudgetFactors, DeadlineFactors, true,true);

				chartC.pack();
				RefineryUtilities.positionFrameOnScreen(chartC, loc, 0.0);

				chartC.setVisible(true);

				chartT.pack();

				RefineryUtilities.positionFrameOnScreen(chartT, loc, 0.6);
				chartT.setVisible(true);

				loc += locplus;

			}
		}

	}


	public static void printUtilizationRatioCharts() {
		// prints cost and time of each workflow on deffrent environment
		float count = 0;
		float plus = ((float) (100) / utility.Utility.dbs.size()) / 100;
		List<Integer> intervals = new ArrayList<Integer>();
		List<String> wflows = new ArrayList<String>();
		List<ResultDB> ldb = new ArrayList<ResultDB>();
		List<Float> BudgetFactors = new ArrayList<Float>();
		List<Float> DeadlineFactors = new ArrayList<Float>();
		for (ResultDB rs : utility.Utility.dbs) {
			if (!intervals.contains(rs.getInterval())) {
				intervals.add(rs.getInterval());
			}
			if (!wflows.contains(rs.getWorkflow().trim()))
				wflows.add(rs.getWorkflow());
			if (!BudgetFactors.contains(rs.getXcost()))
				BudgetFactors.add(rs.getXcost());
			if (!DeadlineFactors.contains(rs.getXtime()))
				DeadlineFactors.add(rs.getXtime());
		}
		int curInt;
		float curF = 0;
		String curWf;

		float loc = 0;

		float locplus = ((float) (100) / (intervals.size() + wflows.size())) / 100;

		for (int i = 0; i < intervals.size(); i++) {
			for (int j = 0; j < wflows.size(); j++) {

				ldb.clear();
				for (ResultDB rs : utility.Utility.dbs) {
					curInt = intervals.get(i);
					curWf = wflows.get(j);

					if (rs.getInterval() == curInt && curWf.contains(rs.getWorkflow()))
						ldb.add(rs);
				}

				ChartMakerGeneral chartUtilization = new ChartMakerGeneral(
						"Utilization Ratio" + " Time interval: " + ldb.get(0).getInterval() + " wf:"
								+ ldb.get(0).getWorkflow(),
						"Utilization Ratio" + " Time interval: " + ldb.get(0).getInterval() + " wf:"
								+ ldb.get(0).getWorkflow(),
						ldb, BudgetFactors, DeadlineFactors, true);

				chartUtilization.pack();

				RefineryUtilities.positionFrameOnScreen(chartUtilization, loc, 0.6);
				chartUtilization.setVisible(true);

				loc += locplus;

			}
		}

	}

	public static void saveCostTimeXYCharts() {
		float count = 0;
		float plus = ((float) (100) / utility.Utility.dbs.size()) / 100;
		List<Integer> intervals = new ArrayList<Integer>();
		List<String> wflows = new ArrayList<String>();
		List<ResultDB> ldb = new ArrayList<ResultDB>();

		for (ResultDB rs : utility.Utility.dbs) {
			if (!intervals.contains(rs.getInterval())) {
				intervals.add(rs.getInterval());
			}
			if (!wflows.contains(rs.getWorkflow().trim()))
				wflows.add(rs.getWorkflow());
		}
		int curInt = 0;
		String curWf = "";

		float loc = 0;

		float locplus = ((float) (100) / (intervals.size() + wflows.size())) / 100;
		String FolderName = XYFolderMaker("XYchart");

		for (int i = 0; i < intervals.size(); i++) {
			for (int j = 0; j < wflows.size(); j++) {
				ldb.clear();
				for (ResultDB rs : utility.Utility.dbs) {
					curInt = intervals.get(i);
					curWf = wflows.get(j);
					if (rs.getInterval() == curInt && curWf.contains(rs.getWorkflow()))
						ldb.add(rs);
				}
				String wf = curWf.substring(curWf.indexOf("/") + 1, curWf.indexOf("."));
				JFreeChart chartT = ChartMakerTime.getlineChart(
						"Time interval: " + ldb.get(0).getInterval() + " wf:" + wf,
						"Time interval: " + ldb.get(0).getInterval() + " wf:" + wf, ldb);
				JFreeChart chartC = ChartMakerCost.getlineChart(
						"Cost interval: " + ldb.get(0).getInterval() + " wf:" + wf,
						"Cost interval: " + ldb.get(0).getInterval() + " wf:" + wf, ldb);

				try {
					SaveChartJPG(chartT, FolderName + "/Time" + wf + "_" + "I-" + curInt);
					SaveChartJPG(chartC, FolderName + "/Cost" + wf + "_" + "I-" + curInt);
				} catch (IOException e) {
					// TODO: handle exception
					System.out.print(e.toString());
				}

			}
		}

	}

	public static void printXYChartsDouble() {
		float count = 0;
		float plus = ((float) (100) / utility.Utility.dbs.size()) / 100;
		List<Integer> intervals = new ArrayList<Integer>();
		List<String> wflows = new ArrayList<String>();
		List<ResultDB> ldb = new ArrayList<ResultDB>();

		for (ResultDB rs : utility.Utility.dbs) {
			if (!intervals.contains(rs.getInterval())) {
				intervals.add(rs.getInterval());
			}
			if (!wflows.contains(rs.getWorkflow().trim()))
				wflows.add(rs.getWorkflow());
		}
		int curInt;
		String curWf;
		for (int i = 0; i < intervals.size(); i++) {
			for (int j = 0; j < wflows.size(); j++) {
				ldb.clear();
				for (ResultDB rs : utility.Utility.dbs) {
					curInt = intervals.get(i);
					curWf = wflows.get(j);
					if (rs.getInterval() == curInt && curWf.contains(rs.getWorkflow()))
						ldb.add(rs);
				}
				ChartDataMaker cdm = new ChartDataMaker("");
				cdm.printDoubleChart("Time interval: " + ldb.get(0).getInterval() + " wf:" + ldb.get(0).getWorkflow(),
						"Time interval: " + ldb.get(0).getInterval() + " wf:" + ldb.get(0).getWorkflow(), ldb);

				ChartMakerTime chartT = new ChartMakerTime(
						"Time interval: " + ldb.get(0).getInterval() + " wf:" + ldb.get(0).getWorkflow(),
						"Time interval: " + ldb.get(0).getInterval() + " wf:" + ldb.get(0).getWorkflow(), ldb);
				ChartMakerCost chartC = new ChartMakerCost(
						"Cost interval: " + ldb.get(0).getInterval() + " wf:" + ldb.get(0).getWorkflow(),
						"Cost interval: " + ldb.get(0).getInterval() + " wf:" + ldb.get(0).getWorkflow(), ldb);

				cdm.pack();
				RefineryUtilities.positionFrameRandomly(cdm);
				cdm.setVisible(true);
				// chartC.pack();
				// RefineryUtilities.positionFrameRandomly(chartC);
				// chartC.setVisible(true);
				//
				// chartT.pack();
				// RefineryUtilities.positionFrameRandomly(chartT);
				// chartT.setVisible(true);
			}
		}

	}

	private void printDoubleChart(String applicationTitle, String chartTitle, List<ResultDB> rss) {

		final JFreeChart chart = createCombinedChart(chartTitle, rss);
		final ChartPanel panel = new ChartPanel(chart, true, true, true, false, true);
		panel.setPreferredSize(new java.awt.Dimension(500, 270));
		setContentPane(panel);

	}

	private JFreeChart createCombinedChart(String chartTitle, List<ResultDB> rss) {

		final CategoryDataset dataset1 = ChartMakerCost.createXYCategoryDatasetCost(rss);
		final NumberAxis rangeAxis1 = new NumberAxis("Cost");
		rangeAxis1.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		final LineAndShapeRenderer renderer1 = new LineAndShapeRenderer();
		renderer1.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator());
		final CategoryPlot subplot1 = new CategoryPlot(dataset1, null, rangeAxis1, renderer1);
		subplot1.setDomainGridlinesVisible(true);

		final CategoryDataset dataset2 = ChartMakerTime.createXYCategoryDatasetTime(rss);
		final NumberAxis rangeAxis2 = new NumberAxis("Time");
		rangeAxis2.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		final LineAndShapeRenderer renderer2 = new LineAndShapeRenderer();
		renderer2.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator());
		final CategoryPlot subplot2 = new CategoryPlot(dataset2, null, rangeAxis2, renderer2);
		subplot2.setDomainGridlinesVisible(true);

		final CategoryAxis domainAxis = new CategoryAxis("X");
		final CombinedDomainCategoryPlot plot = new CombinedDomainCategoryPlot(domainAxis);
		plot.add(subplot1, 2);
		plot.add(subplot2, 1);

		// return a new chart containing the overlaid plot...
		return new JFreeChart(chartTitle, JFreeChart.DEFAULT_TITLE_FONT, plot, true);

	}

	private JFreeChart createCombinedChart2(String chartTitle, List<ResultDB> rss) {

		// create subplot 1...
		final XYDataset data1 = ChartMakerCost.createXYDatasetCost(rss);
		final XYItemRenderer renderer1 = new StandardXYItemRenderer();
		final NumberAxis rangeAxis1 = new NumberAxis("Range 1");
		final XYPlot subplot1 = new XYPlot(data1, null, rangeAxis1, renderer1);
		subplot1.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);

		final XYTextAnnotation annotation = new XYTextAnnotation("Hello!", 50.0, 10000.0);
		annotation.setFont(new Font("SansSerif", Font.PLAIN, 9));
		annotation.setRotationAngle(Math.PI / 4.0);
		subplot1.addAnnotation(annotation);

		// create subplot 2...
		final XYDataset data2 = ChartMakerTime.createXYDatasetTime(rss);
		final XYItemRenderer renderer2 = new StandardXYItemRenderer();
		final NumberAxis rangeAxis2 = new NumberAxis("Range 2");
		rangeAxis2.setAutoRangeIncludesZero(false);
		final XYPlot subplot2 = new XYPlot(data2, null, rangeAxis2, renderer2);
		subplot2.setRangeAxisLocation(AxisLocation.TOP_OR_LEFT);

		// parent plot...
		final CombinedDomainXYPlot plot = new CombinedDomainXYPlot(new NumberAxis("Domain"));
		plot.setGap(10.0);

		// add the subplots...
		plot.add(subplot1, 1);
		plot.add(subplot2, 1);
		plot.setOrientation(PlotOrientation.VERTICAL);

		// return a new chart containing the overlaid plot...
		return new JFreeChart(chartTitle, JFreeChart.DEFAULT_TITLE_FONT, plot, true);

	}
}
