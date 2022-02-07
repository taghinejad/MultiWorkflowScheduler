package utility;

import java.util.Random;

public class configuration {
	private static boolean printGapAlert = false;
	private static boolean printGapInsertation = true;
	public static int standardMIPS = 100;
	public static int standardDataTransfer = 1;

	private static int provisioningDelay = 97;
	// provisioning Delay boot time of a VM to 97 	seconds, based on the results of Amazon EC2

	public static boolean NotFoundBest = false;

	public static boolean UncertainTaskExecution = false;
	public static float ExecutionDeviation = 0;

	// Communication to Computation Ratio (CCR) is CCR
	private static float Communication2ComputationRatio = (float) 0.5;
	private static boolean enableCCR = false;
	private static long bandwidth;

	//

	public static void EnableUncertainTaskExecution(float deviation) {
		ExecutionDeviation = deviation;
		UncertainTaskExecution = true;
	}

	public static double NormalGenerator(double mean, double deviation) {
		Random randomno = new Random();
		double x = randomno.nextGaussian() * deviation + mean;
		;
		return x;
	}
	public static int GetPoissonRandom(double mean) {
	    int r = 0;
	    Random random = new Random();
	    double a = random.nextDouble();
	    double p = Math.exp(-mean);

	    while (a > p) {
	        r++;
	        a = a - p;
	        p = p * mean / r;
	    }
	    return r;
	}


	public static long GetUncertaionExecutionTime(long mean) {
		if (!UncertainTaskExecution)
			return mean;

		Random randomno = new Random();
		double x = randomno.nextGaussian() * (ExecutionDeviation * mean) + mean;
		return Math.round(x);

	}

	public static boolean isPrintGapAlert() {
		return printGapAlert;
	}

	public static int getProvisioningDelay() {
		return provisioningDelay;
	}

	public static void setProvisioningDelay(int provisioningDelay) {
		configuration.provisioningDelay = provisioningDelay;
	}

	public static void setPrintGapAlert(boolean printGapAlert) {
		configuration.printGapAlert = printGapAlert;
	}

	public static boolean isPrintGapInsertation() {
		return printGapInsertation;
	}

	public static void setPrintGapInsertation(boolean printGapInsertation) {
		configuration.printGapInsertation = printGapInsertation;
	}

	public static int getStandardMIPS() {
		return standardMIPS;
	}

	public static void setStandardMIPS(int standardMIPS) {
		configuration.standardMIPS = standardMIPS;
	}

	public static int getStandardDataTransfer() {
		return standardDataTransfer;
	}

	public static void setStandardDataTransfer(int standardDataTransfer) {
		configuration.standardDataTransfer = standardDataTransfer;
	}

	public static float getCommunication2ComputationRatio() {
		return Communication2ComputationRatio;
	}

	public static void setCommunication2ComputationRatio(float communication2ComputationRatio) {
		Communication2ComputationRatio = communication2ComputationRatio;
	}

	public static boolean isEnableCCR() {
		return enableCCR;
	}

	public static void setEnableCCR(boolean enableCCR) {
		configuration.enableCCR = enableCCR;
	}

	public static long getBandwidth() {
		return bandwidth;
	}

	public static void setBandwidth(long bandwidth) {
		configuration.bandwidth = bandwidth;
	}

}
