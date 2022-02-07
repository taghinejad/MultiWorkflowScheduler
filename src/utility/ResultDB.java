package utility;

import java.util.ArrayList;
import java.util.List;
import utility.AlgorithmResult;
public class ResultDB {
	String id;
	long timeConstrained;
	float costConstrained;
	int interval;
	long bandwidth;
	String Workflow;
	public List<AlgorithmResult> algorithms;
	private float xcost;
	private float xtime;
	float y; 
	public ResultDB(String id,long timeConstrained, float costConstrained, int interval, int bandwidth,float xcost, String workflow) {
		super();
		this.id=id;
		this.timeConstrained = timeConstrained;
		this.costConstrained = costConstrained;
		this.interval = interval;
		this.bandwidth = bandwidth;
		
		setXcost(xcost);
		Workflow = workflow;
		algorithms=new ArrayList<AlgorithmResult>();
	}
	public ResultDB(String id,long timeConstrained, float costConstrained, int interval, int bandwidth,float xcost,float xtime, String workflow) {
		super();
		this.id=id;
		this.timeConstrained = timeConstrained;
		this.costConstrained = costConstrained;
		this.interval = interval;
		this.bandwidth = bandwidth;

		setXcost(xcost);
		setXtime(xtime);
		
		Workflow = workflow;
		algorithms=new ArrayList<AlgorithmResult>();
	}
	
	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}

	public long getTimeConstrained() {
		return timeConstrained;
	}
	public void setTimeConstrained(long timeConstrained) {
		this.timeConstrained = timeConstrained;
	}
	public float getCostConstrained() {
		return costConstrained;
	}
	public void setCostConstrained(float costConstrained) {
		this.costConstrained = costConstrained;
	}
	public int getInterval() {
		return interval;
	}
	public void setInterval(int interval) {
		this.interval = interval;
	}
	public long getBandwidth() {
		return bandwidth;
	}
	public void setBandwidth(long bandwidth) {
		this.bandwidth = bandwidth;
	}
	public String getWorkflow() {
		return Workflow;
	}
	public void setWorkflow(String workflow) {
		Workflow = workflow;
	}
	public List<AlgorithmResult> getAlgorithms() {
		return algorithms;
	}
	public void setAlgorithms(List<AlgorithmResult> algorithms) {
		this.algorithms = algorithms;
	}

	public float getXcost() {
		return xcost;
	}

	public void setXcost(float x) {
		this.xcost = x;
	}
	public float getXtime() {
		return xtime;
	}

	public void setXtime(float xtime) {
		this.xtime = xtime;
	}
	
}
