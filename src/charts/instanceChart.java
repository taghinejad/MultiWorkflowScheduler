package charts;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.IntervalCategoryDataset;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.jfree.data.time.SimpleTimePeriod;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import Broker.Instance;
import Broker.InstanceSet;
import Broker.WorkflowBroker;
import Broker.execution;

public class instanceChart extends ApplicationFrame {

	public instanceChart(final String title, final WorkflowBroker wb) {

		super(title);

		final IntervalCategoryDataset dataset = createDatasetInstances(wb);

		//final JFreeChart chart = createChart(title, dataset);
		 final JFreeChart chart = ChartFactory.createGanttChart(
		           "Gantt Chart Demo",  // chart title
		           "VMs",              // domain axis label
		           "Time(Hour:Miniute)",              // range axis label
		           dataset,             // data
		           true,                // include legend
		           true,                // tooltips
		           false                // urls
		       );
		 

		final CategoryPlot plot = (CategoryPlot) chart.getPlot();
		plot.getDomainAxis().setLabel("VMs");
		plot.getRangeAxis().setLabel("Time(Hour:Miniute)");
		// plot.getDomainAxis().setMaxCategoryLabelWidthRatio(10.0f);
		final CategoryItemRenderer renderer = plot.getRenderer();
		renderer.setSeriesPaint(0, Color.blue);

		// add the chart to a panel...
		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 400));
		setContentPane(chartPanel);

	}
	public instanceChart(final String title, final InstanceSet wb) {

		super(title);

		final IntervalCategoryDataset dataset = createDatasetInstances(wb);

		//final JFreeChart chart = createChart(title, dataset);
		 final JFreeChart chart = ChartFactory.createGanttChart(
		           "Gantt Chart Demo",  // chart title
		           "VMs",              // domain axis label
		           "Time(Hour:Miniute)",              // range axis label
		           dataset,             // data
		           true,                // include legend
		           true,                // tooltips
		           false                // urls
		       );
		 

		final CategoryPlot plot = (CategoryPlot) chart.getPlot();
		plot.getDomainAxis().setLabel("VMs");
		plot.getRangeAxis().setLabel("Time(Hour:Miniute)");
		// plot.getDomainAxis().setMaxCategoryLabelWidthRatio(10.0f);
		final CategoryItemRenderer renderer = plot.getRenderer();
		renderer.setSeriesPaint(0, Color.blue);

		// add the chart to a panel...
		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 400));
		setContentPane(chartPanel);

	}
	public static IntervalCategoryDataset createDatasetInstances(WorkflowBroker wb) {
		final InstanceSet instances = wb.policy.instances;
		final int interval = wb.getInterval();
		List<Task> series = new ArrayList<Task>();
		TaskSeries tseries = new TaskSeries("Time interval:" + interval + " s");
		int vmCount = 0;
		for (int i = 0; i < instances.getSize(); i++) {
			Instance cur = instances.getInstance(i);
			float curCost = (float) Math.ceil((double) (cur.getFinishTime() - cur.getStartTime()) / (double) interval)
					* cur.getType().getCost();

			Date start = new Date(0, 0, 0, 0, 0, (int) cur.getStartTime());
			Date end = new Date(0, 0, 0, 0, 0, (int) cur.getFinishTime());
			Task vm = new Task(String.valueOf(cur.getId()) + " c:" + cur.getType().getCost() + "_tc:" + curCost,
					new SimpleTimePeriod(start, end));

			// PCP dont store exeList;
			String taskId = cur.getExeList().get(0).getId();
			for (int j = 0; j < cur.getExeList().size(); j++) {
				execution ex = cur.getExeList().get(j);
				start = new Date(0, 0, 0, 0, 0, (int) ex.getStart());
				end = new Date(0, 0, 0, 0, 0, (int) ex.getFinish());

				vm.addSubtask(new Task(String.valueOf(vmCount), new SimpleTimePeriod(start, end)));
				// vm.add(new Task(String.valueOf(vmCount), new SimpleTimePeriod(ex.start,
				// ex.finish)));
			}

			tseries.add(vm);
			vmCount++;
		}

		TaskSeriesCollection vmcol = new TaskSeriesCollection();
		vmcol.add(tseries);

		return vmcol;
	}
	public static IntervalCategoryDataset createDatasetInstances(final InstanceSet instances) {
		
	
		List<Task> series = new ArrayList<Task>();
		TaskSeries tseries = new TaskSeries("Time");
		int vmCount = 0;
		for (int i = 0; i < instances.getSize(); i++) {
			Instance cur = instances.getInstance(i);
//			float curCost = (float) Math.ceil((double) (cur.getFinishTime() - cur.getStartTime()) / (double) interval)
//					* cur.getType().getCost();

			Date start = new Date(0, 0, 0, 0, 0, (int) cur.getStartTime());
			Date end = new Date(0, 0, 0, 0, 0, (int) cur.getFinishTime());
			Task vm = new Task("r" + String.valueOf(cur.getId()) + " c:" + cur.getType().getCost(),
					new SimpleTimePeriod(start, end));

			// PCP dont store exeList;
			String taskId = cur.getExeList().get(0).getId();
			for (int j = 0; j < cur.getExeList().size(); j++) {
				execution ex = cur.getExeList().get(j);
				start = new Date(0, 0, 0, 0, 0, (int) ex.getStart());
				end = new Date(0, 0, 0, 0, 0, (int) ex.getFinish());

				vm.addSubtask(new Task(String.valueOf(vmCount), new SimpleTimePeriod(start, end)));
				// vm.add(new Task(String.valueOf(vmCount), new SimpleTimePeriod(ex.start,
				// ex.finish)));
			}

			tseries.add(vm);
			vmCount++;
		}

		TaskSeriesCollection vmcol = new TaskSeriesCollection();
		vmcol.add(tseries);

		return vmcol;
	}
	

	/**
	 * Utility method for creating <code>Date</code> objects.
	 *
	 * @param day
	 *            the date.
	 * @param month
	 *            the month.
	 * @param year
	 *            the year.
	 *
	 * @return a date.
	 */
	private static Date date(final int day, final int month, final int year) {

		final Calendar calendar = Calendar.getInstance();
		calendar.set(year, month, day);
		final Date result = calendar.getTime();
		return result;

	}

	/**
	 * Creates a chart.
	 * 
	 * @param dataset
	 *            the dataset.
	 * 
	 * @return The chart.
	 */
	private JFreeChart createChart(String title, final IntervalCategoryDataset dataset) {
		final JFreeChart chart = ChartFactory.createGanttChart(title, // chart title
				"Task", // domain axis label
				"Date", // range axis label
				dataset, // data
				true, // include legend
				true, // tooltips
				false // urls
		);
		// chart.getCategoryPlot().getDomainAxis().setMaxCategoryLabelWidthRatio(10.0f);
		return chart;
	}

	/**
	 * Starting point for the demonstration application.
	 *
	 * @param args
	 *            ignored.
	 */
	public static void PrintInstanceGantt(final WorkflowBroker wb, String Scheduler) {
		final instanceChart demo = new instanceChart("Instance " + Scheduler, wb);
		demo.pack();
		RefineryUtilities.positionFrameRandomly(demo);
		demo.setVisible(true);
	}
	// private static void main(final String[] args) {
	//
	// final instanceChart demo = new instanceChart("Gantt Chart Demo 1");
	// demo.pack();
	// RefineryUtilities.centerFrameOnScreen(demo);
	// demo.setVisible(true);
	//
	// }

}