package charts;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.BoxAndWhiskerToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import utility.AlgorithmResult;
import utility.ResultDB;

public class ChartMakerCost extends ApplicationFrame {
	// public ChartMakerCost(String applicationTitle, String chartTitle) {
	// super(applicationTitle);
	// JFreeChart barChart = ChartFactory.createBarChart(chartTitle, "Algorithms",
	// "COST",
	// ChartDataMaker.createDatasetCost(0), PlotOrientation.VERTICAL, true, true,
	// false);
	// ChartPanel chartPanel = new ChartPanel(barChart);
	// chartPanel.setPreferredSize(new java.awt.Dimension(560, 367));
	// setContentPane(chartPanel);
	//
	// }
	public ChartMakerCost(String applicationTitle, String chartTitle, ResultDB resultdb) {
		super(applicationTitle);
		JFreeChart barChart = ChartFactory.createBarChart(chartTitle, "Algorithms", "COST", createDatasetCost(resultdb),
				PlotOrientation.VERTICAL, true, true, false);
		ChartPanel chartPanel = new ChartPanel(barChart);
		chartPanel.setPreferredSize(new java.awt.Dimension(560, 367));
		setContentPane(chartPanel);

	}

	public ChartMakerCost(String applicationTitle, String chartTitle, List<ResultDB> rss) {
		super(applicationTitle);
		JFreeChart xylineChart = ChartFactory.createXYLineChart(chartTitle,
				"interval: " + rss.get(0).getInterval() + " wf:" + rss.get(0).getWorkflow(), "Cost",
				createXYDatasetCost(rss), PlotOrientation.VERTICAL, true, true, false);

		ChartPanel chartPanel = new ChartPanel(xylineChart);
		chartPanel.setPreferredSize(new java.awt.Dimension(560, 367));
		final XYPlot plot = xylineChart.getXYPlot();
		setContentPane(chartPanel);
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		renderer.setSeriesPaint(0, Color.RED);
		renderer.setSeriesPaint(1, Color.GREEN);
		renderer.setSeriesPaint(2, Color.YELLOW);
		renderer.setSeriesPaint(3, Color.BLACK);
		renderer.setSeriesPaint(4, Color.blue);
		renderer.setSeriesStroke(0, new BasicStroke(1.0f));
		renderer.setSeriesStroke(1, new BasicStroke(3.0f));
		renderer.setSeriesStroke(2, new BasicStroke(3.0f));
		renderer.setSeriesStroke(3, new BasicStroke(3.0f));
		renderer.setSeriesStroke(4, new BasicStroke(3.0f));
		plot.setRenderer(renderer);
	}

	public ChartMakerCost(String applicationTitle, String chartTitle, List<ResultDB> rss, List<Float> BudgetFactors,
			List<Float> DeadlineFactors) {
		super(applicationTitle);
		final BoxAndWhiskerCategoryDataset dataset = createDataCostRatioBasedDeadlineFactorBoxChart(rss, BudgetFactors,
				DeadlineFactors);

		final CategoryAxis xAxis = new CategoryAxis("Deadline Factors");
		final NumberAxis yAxis = new NumberAxis("Cost Ratio");
		yAxis.setAutoRangeIncludesZero(false);
		final BoxAndWhiskerRenderer renderer = new BoxAndWhiskerRenderer();
		renderer.setFillBox(false);
		renderer.setToolTipGenerator(new BoxAndWhiskerToolTipGenerator());
		final CategoryPlot plot = new CategoryPlot(dataset, xAxis, yAxis, renderer);

		final JFreeChart chart = new JFreeChart(applicationTitle, new Font("SansSerif", Font.BOLD, 14), plot, true);
		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(450, 270));
		setContentPane(chartPanel);
	}

	public ChartMakerCost(String applicationTitle, String chartTitle, List<ResultDB> rss, List<Float> BudgetFactors,
			List<Float> DeadlineFactors, Boolean time2Cost) {
		super(applicationTitle);
		final BoxAndWhiskerCategoryDataset dataset = createDataTime2CostRatioBasedDeadlineFactorBoxChart(rss,
				BudgetFactors, DeadlineFactors);

		final CategoryAxis xAxis = new CategoryAxis("Deadline Range");
		final NumberAxis yAxis = new NumberAxis("Quality Ratio");
		yAxis.setAutoRangeIncludesZero(false);
		final BoxAndWhiskerRenderer renderer = new BoxAndWhiskerRenderer();
		renderer.setFillBox(false);
		renderer.setToolTipGenerator(new BoxAndWhiskerToolTipGenerator());
		final CategoryPlot plot = new CategoryPlot(dataset, xAxis, yAxis, renderer);

		final JFreeChart chart = new JFreeChart(applicationTitle, new Font("SansSerif", Font.BOLD, 14), plot, true);
		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(450, 270));
		setContentPane(chartPanel);
	}

	public ChartMakerCost(String applicationTitle, String chartTitle, List<ResultDB> rss, List<Float> BudgetFactors,
			List<Float> DeadlineFactors, Boolean time2Cost, Boolean sucessRate) {
		super(applicationTitle);

		JFreeChart barChart = ChartFactory.createBarChart(chartTitle, "Deadlines", "SR",
				createSucessRateBasedDeadlineFactorBoxChart(rss, BudgetFactors, DeadlineFactors),
				PlotOrientation.VERTICAL, true, true, false);
		ChartPanel chartPanel = new ChartPanel(barChart);
		chartPanel.setPreferredSize(new java.awt.Dimension(560, 367));
		setContentPane(chartPanel);

	}

	public static JFreeChart getlineChart(String applicationTitle, String chartTitle, List<ResultDB> rss) {
		JFreeChart xylineChart = ChartFactory.createXYLineChart(chartTitle,
				"interval: " + rss.get(0).getInterval() + " wf:" + rss.get(0).getWorkflow(), "Cost",
				createXYDatasetCost(rss), PlotOrientation.VERTICAL, true, true, false);

		ChartPanel chartPanel = new ChartPanel(xylineChart);
		chartPanel.setPreferredSize(new java.awt.Dimension(560, 367));
		final XYPlot plot = xylineChart.getXYPlot();
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		renderer.setSeriesPaint(0, Color.RED);
		renderer.setSeriesPaint(1, Color.GREEN);
		renderer.setSeriesPaint(2, Color.YELLOW);
		renderer.setSeriesPaint(3, Color.BLACK);
		renderer.setSeriesPaint(4, Color.blue);
		renderer.setSeriesStroke(0, new BasicStroke(4.0f));
		renderer.setSeriesStroke(1, new BasicStroke(3.0f));
		renderer.setSeriesStroke(2, new BasicStroke(2.0f));
		renderer.setSeriesStroke(3, new BasicStroke(3.5f));
		renderer.setSeriesStroke(4, new BasicStroke(1.0f));
		plot.setRenderer(renderer);
		return xylineChart;
	}

	public static CategoryDataset createDatasetCost(ResultDB rs) {
		final DefaultCategoryDataset ds = new DefaultCategoryDataset();
		// ResultDB rs = Utility.dbs.get(index);
		for (AlgorithmResult as : rs.algorithms) {
			if ((as.cost < (rs.getCostConstrained() * 3)) && (as.makespan < (rs.getTimeConstrained() * 3)))
				ds.addValue(as.cost, as.name, "Cost");
		}
		return ds;
	}

	public static XYDataset createXYDatasetCost(List<ResultDB> rss) {
		final XYSeriesCollection ds = new XYSeriesCollection();

		final XYSeries Const = new XYSeries("CostConstrain");
		for (int j = 0; j < rss.size(); j++) {

			Const.add(rss.get(j).getXcost(), rss.get(j).getCostConstrained());
		}
		ds.addSeries(Const);
		for (int i = 0; i < rss.get(0).getAlgorithms().size(); i++) {
			final XYSeries alg = new XYSeries(rss.get(0).getAlgorithms().get(i).name);
			for (int j = 0; j < rss.size(); j++) {
				AlgorithmResult as = rss.get(j).algorithms.get(i);
				alg.add(rss.get(j).getXcost(), as.cost);
			}
			ds.addSeries(alg);
		}
		return ds;
	}

	public static CategoryDataset createXYCategoryDatasetCost(List<ResultDB> rss) {

		final DefaultCategoryDataset result = new DefaultCategoryDataset();
		String algorithmName = "CostConstrain";
		for (int j = 0; j < rss.size(); j++) {
			result.addValue(rss.get(j).getCostConstrained(), algorithmName, String.valueOf(rss.get(j).getXcost()));
		}
		for (int i = 0; i < rss.get(0).getAlgorithms().size(); i++) {
			algorithmName = rss.get(0).getAlgorithms().get(i).name;
			for (int j = 0; j < rss.size(); j++) {
				AlgorithmResult as = rss.get(j).algorithms.get(i);
				if (as.cost > 0 && as.cost < (rss.get(j).getCostConstrained() * 4) && as.makespan > 0
						&& as.makespan < (rss.get(j).getTimeConstrained() * 4))
					result.addValue(as.cost, algorithmName, String.valueOf(rss.get(j).getXcost()));
			}
		}
		return result;

	}

	private BoxAndWhiskerCategoryDataset createDataCostRatioBasedDeadlineFactorBoxChart(List<ResultDB> rss,
			List<Float> BudgetFactors, List<Float> DeadlineFactors) {
		// creates data needed for createing Box whiskers Chart to demonistrate Time
		// Ratios of different budget Factors
		final DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
		float curDeadlineFactor, curBudgetFactor = 0;
		// series
		for (int i = 0; i < rss.get(0).getAlgorithms().size(); i++) {
			// Categories
			for (int f = 0; f < DeadlineFactors.size(); f++) {
				final List list = new ArrayList();
				curDeadlineFactor = DeadlineFactors.get(f);
				for (int v = 0; v < BudgetFactors.size(); v++) {
					curBudgetFactor = BudgetFactors.get(v);
					for (int j = 0; j < rss.size(); j++) {
						if (rss.get(j).getXcost() == curBudgetFactor && rss.get(j).getXtime() == curDeadlineFactor) {
							AlgorithmResult as = rss.get(j).algorithms.get(i);
							// EntityCount
							list.add(as.BudgetRatio);
							// if (as.BudgetRatio<(float)2)
							// list.add(as.BudgetRatio);
							// //we add 2 in here because budget ratio bigger than 1 is a failed
							// schedulation
							// // we limit the chart between 0 to 2;
							// else list.add((float)2);

						}
					}
				}
				dataset.add(list, rss.get(0).getAlgorithms().get(i).name, curDeadlineFactor);

			}

		}

		return dataset;
	}

	private BoxAndWhiskerCategoryDataset createDataTime2CostRatioBasedDeadlineFactorBoxChart(List<ResultDB> rss,
			List<Float> BudgetFactors, List<Float> DeadlineFactors) {
		ChartDataMaker.rangeValuePrint = false;
		// creates data needed for createing Box whiskers Chart to demonistrate Time
		// Ratios of different budget Factors
		final DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
		float curDeadlineFactor, curBudgetFactor = 0;

		if (ChartDataMaker.rangeValuePrint)
			System.out.println(" \n          D:Quality Based on Deadline Range------------------------");
		// series
		float br = 0, tr = 0, qr = 0;
		String Al = "";
		int successCount = 0;
		int totalCount = 0;
		for (int i = 0; i < rss.get(0).getAlgorithms().size(); i++) {
			// Categories
			Al = rss.get(0).getAlgorithms().get(i).name;
			if (ChartDataMaker.rangeValuePrint)
				System.err.println("<<" + Al + ">>" + "  " + rss.get(0).getWorkflow());
			for (int f = 0; f < DeadlineFactors.size(); f++) {
				final List list = new ArrayList();
				curDeadlineFactor = DeadlineFactors.get(f);
				successCount = 0;
				totalCount = 0;
				if (ChartDataMaker.rangeValuePrint)
					System.out.println(Al + "   Deadline Range:" + curDeadlineFactor);
				for (int v = 0; v < BudgetFactors.size(); v++) {
					curBudgetFactor = BudgetFactors.get(v);
					if (ChartDataMaker.rangeValuePrint)
						System.out.print(Al + "    --------Budget Range:" + curBudgetFactor + "  : ");
					for (int j = 0; j < rss.size(); j++) {
						if (rss.get(j).getXcost() == curBudgetFactor && rss.get(j).getXtime() == curDeadlineFactor) {
							AlgorithmResult as = rss.get(j).algorithms.get(i);
							// EntityCount
							br = as.BudgetRatio;
							tr = as.TimeRatio;
							if (br <= 1 && tr <= 1)
								successCount++;
							totalCount++;
							if (ChartDataMaker.NormalizedRangeValue) {
								if (br > 2)
									br = 2 + (br % 3);
								if (tr > 2)
									tr = 2 + (tr % 3);
							}
						
							
							qr=(float)(br+tr)/2;
							// we want to aquire succssfull Quality rate which is less or eqaul than 1.
							if (((br > 1) || (tr > 1)) && ChartDataMaker.successQuality) 
								qr = (float) (br + tr + 1) / 2;
							
								
							
							if (ChartDataMaker.rangeValuePrint)
								System.out.print("    Q: " + qr + " B: " + br + " T: " + tr);
							
							list.add(qr);
							if (ChartDataMaker.rangeValuePrint)
								System.out.print(" \n");
							// if (as.BudgetRatio<(float)2)
							// list.add(as.BudgetRatio);
							// //we add 2 in here because budget ratio bigger than 1 is a failed
							// schedulation
							// // we limit the chart between 0 to 2;
							// else list.add((float)2);

						}
					}

				}
				if (ChartDataMaker.rangeValuePrint)
					System.out.println("      ====>successRatio:" + ((float) successCount * 100 / totalCount)
							+ "  sCount:" + successCount + " tCount:" + totalCount + "\n");
				dataset.add(list, rss.get(0).getAlgorithms().get(i).name, curDeadlineFactor);

			}

		}

		return dataset;
	}

	private BoxAndWhiskerCategoryDataset createSucessRateBasedDeadlineFactorBoxChart(List<ResultDB> rss,
			List<Float> BudgetFactors, List<Float> DeadlineFactors) {
		ChartDataMaker.rangeValuePrint = true;
		// creates data needed for createing Box whiskers Chart to demonistrate Time
		// Ratios of different budget Factors
		final DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
		float curDeadlineFactor, curBudgetFactor = 0;

		if (ChartDataMaker.rangeValuePrint)
			System.out.println(" \n          D:SuccessRatio Based on Deadline Range------------------------");
		// series
		float br = 0, tr = 0, qr = 0;
		String Al = "";
		int successCount = 0;
		int totalCount = 0;
		for (int i = 0; i < rss.get(0).getAlgorithms().size(); i++) {
			// Categories
			Al = rss.get(0).getAlgorithms().get(i).name;
			if (ChartDataMaker.rangeValuePrint)
				System.err.println("<<" + Al + ">>" + "  " + rss.get(0).getWorkflow());
			for (int f = 0; f < DeadlineFactors.size(); f++) {
				final List list = new ArrayList();
				curDeadlineFactor = DeadlineFactors.get(f);
				successCount = 0;
				totalCount = 0;
				if (ChartDataMaker.rangeValuePrint)
					System.out.println(Al + "   Deadline Range:" + curDeadlineFactor);
				for (int v = 0; v < BudgetFactors.size(); v++) {
					curBudgetFactor = BudgetFactors.get(v);
					if (ChartDataMaker.rangeValuePrint)
						System.out.print(Al + "    --------Budget Range:" + curBudgetFactor + "  : ");
					for (int j = 0; j < rss.size(); j++) {
						if (rss.get(j).getXcost() == curBudgetFactor && rss.get(j).getXtime() == curDeadlineFactor) {
							AlgorithmResult as = rss.get(j).algorithms.get(i);
							// EntityCount
							br = as.BudgetRatio;
							tr = as.TimeRatio;
							if (br <= 1 && tr <= 1)
								successCount++;
							totalCount++;
							if (ChartDataMaker.NormalizedRangeValue) {
								if (br > 2)
									br = 2 + (br % 3);
								if (tr > 2)
									tr = 2 + (tr % 3);
							}
							qr = (float) (br + tr) / 2;
							if (ChartDataMaker.rangeValuePrint)
								System.out.print("    Q: " + qr + " B: " + br + " T: " + tr);

							// we want to aquire succssfull Quality rate which is less or eqaul than 1.
							// if (qr <= 1 && ((br > 1) || (tr > 1))) {
							// if (qr <= 1 && (br > 1))
							// br = ((float) 1.02 - qr) + br;
							// else if (qr <= 1 && (tr > 1))
							// tr = ((float) 1.02 - qr) + tr;
							// qr = (float) (br + tr) / 2;
							// System.out.print(" AC Q: " + qr + " B: " + br + " T: " + tr);
							// }
							if (br <= 1 && tr <= 1)
								qr = 1;
							else
								qr = 0;

							if (ChartDataMaker.rangeValuePrint)
								System.out.print(" \n");
							// if (as.BudgetRatio<(float)2)
							// list.add(as.BudgetRatio);
							// //we add 2 in here because budget ratio bigger than 1 is a failed
							// schedulation
							// // we limit the chart between 0 to 2;
							// else list.add((float)2);

						}
					}

				}
				if (ChartDataMaker.rangeValuePrint)
					System.out.println("      ====>successRatio:" + ((float) successCount * 100 / totalCount)
							+ "  sCount:" + successCount + " tCount:" + totalCount + "\n");
				list.add(((float) successCount * 100 / totalCount));
				dataset.add(list, rss.get(0).getAlgorithms().get(i).name, curDeadlineFactor);

			}

		}

		return dataset;
	}

}