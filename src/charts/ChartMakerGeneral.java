
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

public class ChartMakerGeneral extends ApplicationFrame {
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
	public ChartMakerGeneral(String applicationTitle, String chartTitle, ResultDB resultdb) {
		super(applicationTitle);
		JFreeChart barChart = ChartFactory.createBarChart(chartTitle, "Algorithms", "COST", createDatasetCost(resultdb),
				PlotOrientation.VERTICAL, true, true, false);
		ChartPanel chartPanel = new ChartPanel(barChart);
		chartPanel.setPreferredSize(new java.awt.Dimension(560, 367));
		setContentPane(chartPanel);

	}

	public ChartMakerGeneral(String applicationTitle, String chartTitle, List<ResultDB> rss, Boolean utilizationXY) {
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

	//Creates Utilization BoxWhiskerCharts based on DeadlineFactors;
	public ChartMakerGeneral(String applicationTitle, String chartTitle, List<ResultDB> rss, List<Float> BudgetFactors,
			List<Float> DeadlineFactors,Boolean utilization) {
		super(applicationTitle);
		final BoxAndWhiskerCategoryDataset dataset = createUtilizationRatioBasedDeadlineFactorBoxChart(rss, BudgetFactors,
				DeadlineFactors);

		final CategoryAxis xAxis = new CategoryAxis("Deadline Factors");
		final NumberAxis yAxis = new NumberAxis("Utilization Rate");
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
	public ChartMakerGeneral(String applicationTitle, String chartTitle, List<ResultDB> rss, List<Float> BudgetFactors,
			List<Float> DeadlineFactors,Boolean succesRate,Boolean successRate) {
		super(applicationTitle);
		final BoxAndWhiskerCategoryDataset dataset = createUtilizationRatioBasedDeadlineFactorBoxChart(rss, BudgetFactors,
				DeadlineFactors);

		final CategoryAxis xAxis = new CategoryAxis("Deadline Factors");
		final NumberAxis yAxis = new NumberAxis("PSR Rate");
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

	public static JFreeChart getlineChart1(String applicationTitle, String chartTitle, List<ResultDB> rss) {
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

	public static CategoryDataset createUtilization(ResultDB rs) {
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

//	public static CategoryDataset createBarChartsOfUtilization(List<ResultDB> rss) {
//		final DefaultCategoryDataset ds = new DefaultCategoryDataset();
//		// ResultDB rs = Utility.dbs.get(index);
//		
//		for (AlgorithmResult as : rs.algorithms) {
//			if ((as.cost < (rs.getCostConstrained() * 3)) && (as.makespan < (rs.getTimeConstrained() * 3)))
//				ds.addValue(as.cost, as.name, "Cost");
//		}
//		return ds;
//	}
	
	private BoxAndWhiskerCategoryDataset createUtilizationRatioBasedDeadlineFactorBoxChart(List<ResultDB> rss,
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
							list.add(as.Utilization);
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

}