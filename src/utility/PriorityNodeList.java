package utility;

import java.util.ArrayList;
import java.util.List;

import Broker.WorkflowNode;


public class PriorityNodeList {

	List<WorkflowNode> list;

	enum ordering {
		ASC, DESC
	};

	public PriorityNodeList() {
		list = new ArrayList();
	}

	public void Add(WorkflowNode wn) {
		list.add(wn);
	}
	public boolean isEmpty()
	{
		if (list.size()<1) return true;
		else return false;
	}

	public void AddNoDuplicate(WorkflowNode wn) {
		boolean notExist = true;
		for (WorkflowNode item : list) {
			if (item.getId().contains(wn.getId())) {
				notExist = false;
				break;
			}
		}
		if (notExist)
			list.add(wn);
	}

	public void removeaNode(WorkflowNode wn) {

		for (WorkflowNode item : list) {
			if (item.getId().contains(wn.getId())) {
				list.remove(item);
				break;
			}
		}
	}

	public WorkflowNode pullbyEST() {
		int est = Integer.MAX_VALUE;
		WorkflowNode temp = list.get(0);
		for (WorkflowNode item : list) {
			if (item.getEST() < est) {
				temp = item;
				est = item.getEST();
			}
		}
		list.remove(temp);
		return temp;
	}
	public WorkflowNode pullbyLST() {
		int est = Integer.MAX_VALUE;
		WorkflowNode temp = list.get(0);
		for (WorkflowNode item : list) {
			if (item.getEST() < est) {
				temp = item;
				est = item.getLST();
			}
		}
		list.remove(temp);
		return temp;
	}
	public WorkflowNode pullbyEarliestDeadline() {
		float est = Integer.MAX_VALUE;
		WorkflowNode temp = list.get(0);
		for (WorkflowNode item : list) {
			if (item.getDeadline() < est) {
				temp = item;
				est = item.getDeadline();
			}
		}
		list.remove(temp);
		return temp;
	}
	public WorkflowNode pullbyLSTGeorgies() {
		int est = Integer.MAX_VALUE;
		int estRuntime=0;
		WorkflowNode temp = list.get(0);
		for (WorkflowNode item : list) {
			if (item.getLST() < est || (item.getLST()==est && item.getRunTime()>estRuntime)) {
				temp = item;
				est = item.getLST();
				estRuntime=item.getRunTime();
			}
		}
		list.remove(temp);
		return temp;
	}
	public WorkflowNode pullbyWeight() {
		int est = Integer.MAX_VALUE;
		WorkflowNode temp = list.get(0);
		for (WorkflowNode item : list) {
			if (item.getRunTime() < est) {
				temp = item;
				est = item.getEST();
			}
		}
		list.remove(temp);
		return temp;
	}
	public WorkflowNode pullbyWeightData(long bw) {
		int est = Integer.MAX_VALUE;
		WorkflowNode temp = list.get(0);
		for (WorkflowNode item : list) {
			if (item.getRunTimeWithData(bw) < est) {
				temp = item;
				est = item.getEST();
			}
		}
		list.remove(temp);
		return temp;
	}
	public WorkflowNode pullbyUpWard() {
		int est = -1;
		WorkflowNode temp = list.get(0);
		for (WorkflowNode item : list) {
			if (item.getUpRank() > est) {
				temp = item;
				est = item.getUpRank();
			}
		}
		list.remove(temp);
		return temp;
	}
	public WorkflowNode pullbyminUpWard() {
		int min =Integer.MAX_VALUE;
		WorkflowNode temp = list.get(0);
		for (WorkflowNode item : list) {
			if (item.getUpRank() < min) {
				temp = item;
				min = item.getUpRank();
			}
		}
		list.remove(temp);
		return temp;
	}
	public PriorityNodeList(List<WorkflowNode> list) {
		super();
		this.list = list;
	}

	public List<WorkflowNode> getList() {
		return list;
	}

	public void setList(List<WorkflowNode> list) {
		this.list = list;
	}

}
