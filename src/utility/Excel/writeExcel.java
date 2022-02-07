package utility.Excel;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;

import charts.ChartDataMaker;
import utility.AlgorithmResult;
import utility.ResultDB;

public class writeExcel {

	public static int idNum = 1;
public static Boolean writePermission=false; 

	private static void writeData(String FileName, Map<String, Object[]> data) {
		if (writePermission==false) {
			System.err.println(" \n ******** Write Permission is set to False");
		return;	
		}
		XSSFWorkbook workbook = new XSSFWorkbook();

		// Create a blank sheet
		XSSFSheet sheet = workbook.createSheet(FileName);
		int count = data.size();
		Set<String> keyset = data.keySet();
		int rownum = 0;
		for (int i = 0; i < count; i++) {
			Row row = sheet.createRow(rownum++);
			Object[] objArr = data.get(String.valueOf(i));
			int cellnum = 0;
			for (Object obj : objArr) {
				Cell cell = row.createCell(cellnum++);
				if (obj instanceof String)
					cell.setCellValue((String) obj);
				else if (obj instanceof Integer)
					cell.setCellValue((Integer) obj);
			}
		}
	
		FileName = "excelOutput/" + FileName + ".xlsx";
		try {
			// Write the workbook in file system
			FileOutputStream out = new FileOutputStream(new File(FileName));
			workbook.write(out);
			out.close();

			System.out.println(FileName + " written successfully on disk.");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void NwriteExcelBasedDeadlineFactorBoxChart(List<ResultDB> rss, List<Float> BudgetFactors,
			List<Float> DeadlineFactors) {
		ChartDataMaker.rangeValuePrint = false;

		// creates data needed for createing Box whiskers Chart to demonistrate Time
		// Ratios of different budget Factors

		float curDeadlineFactor, curBudgetFactor = 0;

		// series
		float br = 0, tr = 0, qr = 0;
		String Al = "";
		int successCount = 0;
		int totalCount = 0;
		String wf = rss.get(0).getWorkflow().substring(rss.get(0).getWorkflow().indexOf("/") + 1,
				rss.get(0).getWorkflow().indexOf("."));
		Random rn = new Random();
		String FileName = wf + "Interval" + rss.get(0).getInterval() + "DeadlineRange" + rn.nextInt(100);

		Map<String, Object[]> data = new TreeMap<String, Object[]>();
		data.put("0", new Object[] { "ID", "Algorithm", "DeadlineRange", "BudgetRange", "QualityRatio", "BudgetRatio",
				"TimeRatio" });
		idNum = 1;
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

							data.put(String.valueOf(idNum),
									new Object[] { String.valueOf(idNum), as.name, String.valueOf(curDeadlineFactor),
											String.valueOf(curBudgetFactor), String.valueOf(qr), String.valueOf(br),
											String.valueOf(tr) });
							idNum++;
							// we want to aquire succssfull Quality rate which is less or eqaul than 1.
							// if (qr <= 1 && ((br > 1) || (tr > 1))) {
							// if (qr <= 1 && (br > 1))
							// br = ((float) 1.02 - qr) + br;
							// else if (qr <= 1 && (tr > 1))
							// tr = ((float) 1.02 - qr) + tr;
							// qr = (float) (br + tr) / 2;
							// System.out.print(" AC Q: " + qr + " B: " + br + " T: " + tr);
							// }

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

			}

		}

		// sends to write
		writeData(FileName, data);
	}

	public static void writeExcelBasedDeadlineFactorBoxChart(List<ResultDB> rss, List<Float> BudgetFactors,
			List<Float> DeadlineFactors) {
		ChartDataMaker.rangeValuePrint = false;

		// creates data needed for createing Box whiskers Chart to demonistrate Time
		// Ratios of different budget Factors

		float curDeadlineFactor, curBudgetFactor = 0;

		// series
		float br = 0, tr = 0, qr = 0;
		String Al = "";
		int successCount = 0;
		int totalCount = 0;
		String wf = rss.get(0).getWorkflow().substring(rss.get(0).getWorkflow().indexOf("/") + 1,
				rss.get(0).getWorkflow().indexOf("."));
		Random rn = new Random();
		String FileName = wf + "Interval" + rss.get(0).getInterval() + "DeadlineRange" + rn.nextInt(100);

		Map<String, Object[]> data = new TreeMap<String, Object[]>();
		data.put("0", new Object[] { "ID", "Algorithm", "DeadlineRange", "BudgetRange", "QualityRatio", "BudgetRatio",
				"TimeRatio" });
		idNum = 1;
		for (int f = 0; f < DeadlineFactors.size(); f++) {
			for (int i = 0; i < rss.get(0).getAlgorithms().size(); i++) {
				// Categories
				Al = rss.get(0).getAlgorithms().get(i).name;
				if (ChartDataMaker.rangeValuePrint)
					System.err.println("<<" + Al + ">>" + "  " + rss.get(0).getWorkflow());

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

							data.put(String.valueOf(idNum),
									new Object[] { String.valueOf(idNum), as.name, String.valueOf(curDeadlineFactor),
											String.valueOf(curBudgetFactor), String.valueOf(qr), String.valueOf(br),
											String.valueOf(tr) });
							idNum++;
							// we want to aquire succssfull Quality rate which is less or eqaul than 1.
							// if (qr <= 1 && ((br > 1) || (tr > 1))) {
							// if (qr <= 1 && (br > 1))
							// br = ((float) 1.02 - qr) + br;
							// else if (qr <= 1 && (tr > 1))
							// tr = ((float) 1.02 - qr) + tr;
							// qr = (float) (br + tr) / 2;
							// System.out.print(" AC Q: " + qr + " B: " + br + " T: " + tr);
							// }

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

			}

		}

		// sends to write
		writeData(FileName, data);
	}

	public static void NwriteExcelBasedBudgetFactorBoxChart(List<ResultDB> rss, List<Float> BudgetFactors,
			List<Float> DeadlineFactors) {

		// creates data needed for createing Box whiskers Chart to demonistrate Quality
		// Ratios of different budget Factors
		ChartDataMaker.rangeValuePrint = false;
		float curDeadlineFactor, curBudgetFactor = 0;
		// series
		float br = 0, tr = 0, qr = 0;
		String Al = "";
		int successCount = 0;
		int totalCount = 0;
		String wf = rss.get(0).getWorkflow().substring(rss.get(0).getWorkflow().indexOf("/") + 1,
				rss.get(0).getWorkflow().indexOf("."));
		Random rn = new Random();
		String FileName = wf + "Interval" + rss.get(0).getInterval() + "BudgetRange" + rn.nextInt(100);

		Map<String, Object[]> data = new TreeMap<String, Object[]>();
		data.put("0", new Object[] { "ID", "Algorithm", "BudgetRange", "DeadlineRange", "QualityRatio", "BudgetRatio",
				"TimeRatio" });
		idNum = 1;
		if (ChartDataMaker.rangeValuePrint)
			System.out.println(" \n          B: Quality Based on Budget Range------------------------");
		for (int i = 0; i < rss.get(0).getAlgorithms().size(); i++) {
			// Categories
			Al = rss.get(0).getAlgorithms().get(i).name;
			if (ChartDataMaker.rangeValuePrint)
				System.err.println("<<" + Al + ">>" + "  " + rss.get(0).getWorkflow());
			for (int f = 0; f < BudgetFactors.size(); f++) {
				final List list = new ArrayList();
				curBudgetFactor = BudgetFactors.get(f);
				successCount = 0;
				totalCount = 0;
				if (ChartDataMaker.rangeValuePrint)
					System.out.println(Al + "   Budget Range:" + curBudgetFactor);
				for (int v = 0; v < DeadlineFactors.size(); v++) {
					curDeadlineFactor = DeadlineFactors.get(v);
					if (ChartDataMaker.rangeValuePrint)
						System.out.print(Al + "   --------Deadline Range:" + curDeadlineFactor + "  : ");

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

							data.put(String.valueOf(idNum),
									new Object[] { String.valueOf(idNum), as.name, String.valueOf(curBudgetFactor),
											String.valueOf(curDeadlineFactor), String.valueOf(qr), String.valueOf(br),
											String.valueOf(tr) });
							idNum++;
							// we want to aquire succssfull Quality rate which is less or eqaul than 1.
							// if (qr <= 1 && ((br > 1) || (tr > 1))) {
							// if (qr <= 1 && (br > 1))
							// br = ((float) 1.02 - qr) + br;
							// else if (qr <= 1 && (tr > 1))
							// tr = ((float) 1.02 - qr) + tr;
							// qr = (float) (br + tr) / 2;
							// System.out.print(" AC Q: " + qr + " B: " + br + " T: " + tr);
							// }

							if (ChartDataMaker.rangeValuePrint)
								System.out.print(" \n");
							// list.add(as.TimeRatio);
							// //we add 2 in here because budget ratio bigger than 1 is a failed
							// schedulation
							// // we limit the chart between 0 to 2;
							// else list.add((float) Math.random()*5 +2);

						}
					}

				}
				if (ChartDataMaker.rangeValuePrint)
					System.out.println("      ====>successRatio:" + ((float) successCount * 100 / totalCount)
							+ "  sCount:" + successCount + " tCount:" + totalCount + "\n");

			}

		}
		// sends to write
		writeData(FileName, data);

	}
	public static void writeExcelBasedBudgetFactorBoxChart(List<ResultDB> rss, List<Float> BudgetFactors,
			List<Float> DeadlineFactors) {

		// creates data needed for createing Box whiskers Chart to demonistrate Quality
		// Ratios of different budget Factors
		ChartDataMaker.rangeValuePrint = false;
		float curDeadlineFactor, curBudgetFactor = 0;
		// series
		float br = 0, tr = 0, qr = 0;
		String Al = "";
		int successCount = 0;
		int totalCount = 0;
		String wf = rss.get(0).getWorkflow().substring(rss.get(0).getWorkflow().indexOf("/") + 1,
				rss.get(0).getWorkflow().indexOf("."));
		Random rn = new Random();
		String FileName = wf + "Interval" + rss.get(0).getInterval() + "BudgetRange" + rn.nextInt(100);

		Map<String, Object[]> data = new TreeMap<String, Object[]>();
		data.put("0", new Object[] { "ID", "Algorithm", "BudgetRange", "DeadlineRange", "QualityRatio", "BudgetRatio",
				"TimeRatio" });
		idNum = 1;
		if (ChartDataMaker.rangeValuePrint)
			System.out.println(" \n          B: Quality Based on Budget Range------------------------");
		for (int f = 0; f < BudgetFactors.size(); f++) {
		for (int i = 0; i < rss.get(0).getAlgorithms().size(); i++) {
			// Categories
			Al = rss.get(0).getAlgorithms().get(i).name;
			if (ChartDataMaker.rangeValuePrint)
				System.err.println("<<" + Al + ">>" + "  " + rss.get(0).getWorkflow());

				final List list = new ArrayList();
				curBudgetFactor = BudgetFactors.get(f);
				successCount = 0;
				totalCount = 0;
				if (ChartDataMaker.rangeValuePrint)
					System.out.println(Al + "   Budget Range:" + curBudgetFactor);
				for (int v = 0; v < DeadlineFactors.size(); v++) {
					curDeadlineFactor = DeadlineFactors.get(v);
					if (ChartDataMaker.rangeValuePrint)
						System.out.print(Al + "   --------Deadline Range:" + curDeadlineFactor + "  : ");

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

							data.put(String.valueOf(idNum),
									new Object[] { String.valueOf(idNum), as.name, String.valueOf(curBudgetFactor),
											String.valueOf(curDeadlineFactor), String.valueOf(qr), String.valueOf(br),
											String.valueOf(tr) });
							idNum++;
							// we want to aquire succssfull Quality rate which is less or eqaul than 1.
							// if (qr <= 1 && ((br > 1) || (tr > 1))) {
							// if (qr <= 1 && (br > 1))
							// br = ((float) 1.02 - qr) + br;
							// else if (qr <= 1 && (tr > 1))
							// tr = ((float) 1.02 - qr) + tr;
							// qr = (float) (br + tr) / 2;
							// System.out.print(" AC Q: " + qr + " B: " + br + " T: " + tr);
							// }

							if (ChartDataMaker.rangeValuePrint)
								System.out.print(" \n");
							// list.add(as.TimeRatio);
							// //we add 2 in here because budget ratio bigger than 1 is a failed
							// schedulation
							// // we limit the chart between 0 to 2;
							// else list.add((float) Math.random()*5 +2);

						}
					}

				}
				if (ChartDataMaker.rangeValuePrint)
					System.out.println("      ====>successRatio:" + ((float) successCount * 100 / totalCount)
							+ "  sCount:" + successCount + " tCount:" + totalCount + "\n");

			}

		}
		// sends to write
		writeData(FileName, data);

	}

}
