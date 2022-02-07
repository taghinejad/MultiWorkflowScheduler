package Broker;

import java.util.ArrayList;
import java.util.List;

import DAG.LinkageType;
import Broker.result;
import Broker.execution;

public class Instance {

	private int id;
	private Resource type;
	private long startTime, finishTime;
	private String firstTaskId, lastTaskId;
	private List<WorkflowNode> tasks;
	private long RunTime, RealRunTime;
	private double utilization;
	private List<File> FileSet;
	private List<execution> exeList;
	List<execution> gapList;
	private float frequencyLevel = 1;
	private int energyBusy = 0;
	private int energyIdle = 0;
	private long busyTime, idleTime = 0;

	public Instance(int newId, Resource t) {

		id = newId;
		type = t;
		startTime = 0;
		finishTime = 0;
		FileSet = new ArrayList<File>();
		tasks = new ArrayList<WorkflowNode>();
		gapList = new ArrayList<execution>();
	}

	public List<execution> getGaplist() {
		return gapList;
	}

	public Instance(int id, Resource type, long startTime, long finishTime, String firstTaskId, String lastTaskId,
			List<WorkflowNode> tasks, long runTime, long realRunTime, double utilization, List<File> fileSet,
			List<execution> exeList, float frequencyLevel, int energyBusy, int energyIdle, long busyTime,
			long idleTime) {
		super();
		this.id = id;
		this.type = type;
		this.startTime = startTime;
		this.finishTime = finishTime;
		this.firstTaskId = firstTaskId;
		this.lastTaskId = lastTaskId;
		this.tasks = tasks;
		RunTime = runTime;
		RealRunTime = realRunTime;
		this.utilization = utilization;
		FileSet = fileSet;
		this.exeList = exeList;
		this.frequencyLevel = frequencyLevel;
		this.energyBusy = energyBusy;
		this.energyIdle = energyIdle;
		this.busyTime = busyTime;
		this.idleTime = idleTime;
	}

	public Instance(int newId, Resource t, long st, long ft) {
		id = newId;
		type = t;
		startTime = st;
		finishTime = ft;
		FileSet = new ArrayList<File>();
		tasks = new ArrayList<WorkflowNode>();
	}

	public void addFiles(List<File> files) {
		if (files == null)
			return;
		for (File file : files) {
			if (!this.FileSet.contains(file))
				this.FileSet.add(file);
		}
	}

	public List<File> getNotExistedFiles(List<File> files) {

		List<File> ff = new ArrayList<File>();
		int index = -1;
		for (File f1 : files) {
			index = -1;
			index = SearchIndexFile(f1);
			if (index == -1)
				ff.add(f1);
		}
		return ff;
	}

	public List<File> getFileSet() {
		return FileSet;
	}

	public void setFileSet(List<File> fileSet) {
		FileSet = fileSet;
	}

	public int SearchIndexFile(File f1) {

		File file = null;
		for (int i = 0; i < FileSet.size(); i++) {
			file = FileSet.get(i);
			if (file.fileName.contains(f1.getFileName()))
				return i;
		}
		return -1;

	}

	public long getNotExistedFileSizes(List<File> files) {
		if (files == null)
			return 0;
		List<File> ff = getNotExistedFiles(files);
		long size = 0;
		for (File file : ff) {
			if (file.getLink() == LinkageType.INPUT)
				size += file.getFileSize();
		}
		return size;
	}

	public void addExe(String id, long start, long finish, long readStart, float cost) {
		if (exeList == null)
			exeList = new ArrayList<execution>();
		execution ex = new execution(id, start, finish, readStart, cost, this.getId());
		stableExeIns(id, start, finish);
		exeList.add(ex);
	}

	public void addGap(long start, long finish) {
		if (gapList == null)
			gapList = new ArrayList<execution>();
		execution ex = new execution(Integer.toString(gapList.size()), start, finish, 0, 0, this.getId());

		gapList.add(ex);
	}

	public void sortExeList() {
		List<execution> exlist = new ArrayList<>();
		execution exi;
		long minStart = Integer.MAX_VALUE;
		int index;
		if (exeList == null || exeList.size() == 0)
			return;
		while (!exeList.isEmpty()) {
			minStart = exeList.get(0).getStart();
			index = 0;
			for (int j = 0; j < exeList.size(); j++) {
				if (minStart > exeList.get(j).getStart()) {
					minStart = exeList.get(j).getStart();
					index = j;
				}
			}
			exlist.add(exeList.get(index));
			exeList.remove(index);
		}
		exeList.clear();
		exeList.addAll(exlist);
	}

	public List<execution> giveGap() {
		sortExeList();
		if (exeList == null)
			return null;
		List<execution> exlist = new ArrayList<>();
		for (int j = 0; j < exeList.size() - 1; j++) {
			if (exeList.get(j).getFinish() < exeList.get(j + 1).getStart()) {
				exlist.add(new execution("G" + j, exeList.get(j).getFinish(), exeList.get(j + 1).getStart(), 0,
						this.getId()));
			}
		}
		return exlist;
	}

	public int canFitGap(long start, long end) {
		List<execution> exlist = giveGap();
		
		if (exlist == null)
			return -1;
		for (int j = 0; j < exlist.size(); j++) {
			if (end <= exlist.get(j).getFinish() && start >= exlist.get(j).getStart()) {
				return j;
			}
		}
		return -1;
	}

	public float canFitGapPercent(long start, long end) {
		float fitPercent = 0;
		List<execution> exlist = giveGap();
		if (exlist == null)
			return 0;
		for (int j = 0; j < exlist.size(); j++) {
			if (end <= exlist.get(j).getFinish() && start >= exlist.get(j).getStart()) {
				fitPercent = ((float) (exlist.get(j).getFinish() - exlist.get(j).getStart()) / (end - start));
				return fitPercent;
			}
		}
		return 0;
	}

	public result canFitGapPro2(long earlistStart, long latestEnd, long computation) {
		// return a positive value (fitPercent)
		float fitPercent = 0;
		List<execution> exlist = giveGap();

		Boolean canFit = false;
		result r = new result();

		float maxFit = 0;
		long gStart, gEnd = 0, gLenght = 0;
		r.finishTime = -1;
		if (exlist == null)
			return r;
		for (int j = 0; j < exlist.size(); j++) {
			gStart = exlist.get(j).getStart();
			gEnd = exlist.get(j).getFinish();
			gLenght = gEnd - gStart;
			long pStart = Math.max(gStart, earlistStart);
			long pEnd = pStart + computation;
			if (pStart >= gStart && pEnd <= gEnd) {
				fitPercent = ((float) (computation) / (gLenght));
				if (fitPercent > maxFit && fitPercent <= 1) {
					maxFit = fitPercent;
					r.startTime = (int) pStart;
					r.finishTime = (int) pEnd;
					r.gapFitpercent = fitPercent;
				}
			}

		}
		return r;
	}

	public result canFitGapPro(long earlistStart, long latestEnd, long computation) {
		// return a positive value (fitPercent)
		float fitPercent = 0;

		Boolean BestFit = true;
		Boolean canFit = false;
		result r = new result();

		float maxFit = 0;
		long gStart, gEnd = 0, gLenght = 0;
		r.finishTime = -1;
		r.gapid = -1;
		if (gapList == null)
			return r;
		for (int j = 0; j < gapList.size(); j++) {
			gStart = gapList.get(j).getStart();
			gEnd = gapList.get(j).getFinish();
			gLenght = gEnd - gStart;
			long pStart = Math.max(gStart, earlistStart);
			long pEnd = pStart + computation;
			if (pStart >= gStart && pEnd <= gEnd) {

//				best fit policy
				if (BestFit) {
					fitPercent = ((float) (computation) / (gLenght));
					if (fitPercent > maxFit && fitPercent <= 1) {
						maxFit = fitPercent;
						r.startTime = (int) pStart;
						r.finishTime = (int) pEnd;
						r.gapFitpercent = fitPercent;
						r.gapid = j;
					}
				} else if (fitPercent > maxFit && fitPercent <= 1) {
					r.startTime = (int) pStart;
					r.finishTime = (int) pEnd;
					r.gapFitpercent = fitPercent;
					r.gapid = j;
					break;
				}
			}

		}
		return r;
	}

	public void updateExe(String id, long start, long finish, long readStart, float cost) {
		if (exeList == null)
			return;
		execution exi;
		for (int i = 0; i < exeList.size(); i++) {
			exi = exeList.get(i);
			if (exi.getId().contains(id)) {
				exeList.get(i).setStart(start);
				exeList.get(i).setFinish(finish);
				exeList.get(i).setReadStart(readStart);

				exeList.get(i).setCost(cost);
				if (cost < 0) {
					// throw new ArithmeticException("cost is zero");
					System.out.println("cost is zero");
				}
				stableExeIns(id, start, finish);
				return;
			}
		}
	}

	public void addExe(String id, long start, long finish, long readStart, float cost, int energy, float reliability) {
		if (exeList == null)
			exeList = new ArrayList<execution>();
		execution ex = new execution(id, start, finish, readStart, cost, energy, reliability, this.getId());
//		if (!checkExe(id, start, finish))
		exeList.add(ex);
		stableExeIns(id, start, finish);
	}

	private void stableExeIns(String taskId, long start, long finish) {
		if (this.getFinishTime() < finish) {
			this.setFinishTime(finish);
			this.setLastTask(taskId);
		}
		if (this.getStartTime() > start) {
			this.setStartTime(start);
			this.setFirstTask(taskId);
		}
	}

	public void addExe(String id, long start, long finish, long readStart, float cost, int energy, float reliability,
			float freq, float freqLevel) {

		if (exeList == null)
			exeList = new ArrayList<execution>();
		execution ex = new execution(id, start, finish, readStart, cost, energy, reliability, freq, this.getId(),
				freqLevel);
		if (cost < 0) {
			// throw new ArithmeticException("cost is zero");
			System.out.println("cost is zero");
		}

		stableExeIns(id, start, finish);
//		if (!checkExe(id, start, finish))
		exeList.add(ex);

	}

	public Boolean checkExe(String id, long start, long finish) {
		boolean finded = false;

		for (execution ex : getExeList()) {
			if (ex.getId().contains(id))// && start == ex.start && ex.finish == finish)
				finded = true;
		}
		return finded;
	}

	public List<execution> getExeList() {
		return exeList;
	}

	public void clearTasks() {
		exeList.clear();

		setStartTime(0);
		setFinishTime(0);

		this.firstTaskId = null;
		this.lastTaskId = null;
		this.tasks.clear();
	}

	public long getRealRunTime() {
		return RealRunTime;
	}

	public double getUtilization() {
		return utilization;
	}

	public void setUtilization(double utilization) {
		this.utilization = utilization;
	}

	public void setRealRunTime(long realRunTime) {
		RealRunTime = realRunTime;
	}

	public float getFrequencyLevel() {
		if (frequencyLevel == 0)
			return 1;
		return frequencyLevel;
	}

	public float getFrequencyByFrequencyLevel(float freqLevel) {
		if (freqLevel == 1)
			return getType().getMaxFrequencyOr1();

		return freqLevel * getType().getMaxFrequency();
	}

	public float getFrequencyLevel(float freq) {
		int rfreq = 1;
		if (getType().getMaxFrequency() > 0)
			rfreq = Math.round((float) freq / getType().getMaxFrequency() * 10);
		else
			return 1;
		return (float) rfreq / 10;
	}

//	public void setFrequencyLevel(float frequencyLevel) {
//		this.frequencyLevel = frequencyLevel;
//	}

	public void CalculateTasksTimeInInstances() {
		try {

			long InsRunTime = 0;
			for (execution ts : getExeList()) {
				InsRunTime += ts.getFinish() - ts.getStart();
			}
			long runtime = getFinishTime() - getStartTime();
			long ceil = (long) Math.ceil((double) runtime / (double) WorkflowBroker.interval);
			long idleTime = ((ceil * WorkflowBroker.interval) - InsRunTime);
			if (idleTime < 0) {
				System.err.println("error idle time is less than zero");
			}
			if (runtime < InsRunTime) {
				System.err.println("insid:" + id + " bigger runtime:" + runtime + " insRuntime:" + InsRunTime);
			}

			double utlization = (double) (runtime) * (100) / (double) (ceil * WorkflowBroker.interval);
			this.setRealRunTime(InsRunTime);
			this.setRunTime(runtime);
			this.setIdleTime(idleTime);
			this.setUtilization(utlization);
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("error: " + e.getMessage());
		}
	}

	public long getRunTime() {
		return RunTime;
	}

	public void setRunTime(long runTime) {
		RunTime = runTime;
	}

	public long getIdleTime() {
		return this.idleTime;
	}

	public void setIdleTime(long idleTime) {
		this.idleTime = idleTime;
	}

	public void setStartTime(long st) {
		if (st >= 0)
			startTime = st;
	}

	public void setFinishTime(long ft) {
		if (ft >= 0)
			finishTime = ft;
	}

	public long getStartTime() {
		return (startTime);
	}

	public long getFinishTime() {
		return (finishTime);
	}

	public int getId() {
		return (id);
	}

	public Resource getType() {
		return (type);
	}

	public void setFirstTask(String id) {
		firstTaskId = id;
	}

	public String getFirstTask() {
		return (firstTaskId);
	}

	public void setLastTask(String id) {
		lastTaskId = id;
	}

	public String getLastTask() {
		return (lastTaskId);
	}

	public List<WorkflowNode> getTasks() {
		return (tasks);
	}

	public void addTask(WorkflowNode task) {
		tasks.add(task);
	}

	public execution getExectionTask(String id) {
		for (execution ex : exeList) {
			if (ex.getId().contains(id)) {
				return ex;
			}
		}
		return null;
	}

	public int getEnergyIdle() {
		return energyIdle;
	}

	public void setEnergyIdle(int energy) {
		energyIdle = energy;
	}

	public int getEnergyBusy() {
		return energyBusy;
	}

	public void setEnergyBusy(int energy) {
		energyBusy = energy;
	}

	private void calcBusyIdleVmtime() {
		long finishIns = finishTime;
		long busyTime = 0;
		long startIns = startTime;
		for (execution ex : getExeList()) {
			{
				busyTime += (ex.getFinish() - ex.getStart());

				if (startIns > ex.getStart())
					startIns = ex.getStart();
				if (finishIns < ex.getFinish())
					finishIns = ex.getFinish();
			}
		}

		this.busyTime = busyTime;
		long runningVmTime = finishIns - startIns;
		this.idleTime = (int) (runningVmTime - busyTime);
//		if (idleTime>0)
//			System.out.println("idleTime is "+this.idleTime+" for ins:" + this.id);
	}

	public long getBusyVmTime() {
		if (this.busyTime == 0)
			calcBusyIdleVmtime();
		return this.busyTime;

	}

	public long getIdleVmTime() {
		if (this.idleTime == 0)
			calcBusyIdleVmtime();
		return this.idleTime;
	}

	public long getTotalRunningTime() {
		return getIdleTime() + getBusyVmTime();

	}

}
