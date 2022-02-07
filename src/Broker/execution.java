package Broker;



public class execution {
	public String id;
	private long start;
	private long finish;
	public long readStart;
	public float Cost;
	public int Energy;
	public float Reliability;
	public float Frequency;
	public float FrequencyLevel;
	private int instanceId;

	execution(String id, long start, long finish, long readStart, int instanceId) {
		this.id = id;
		this.setStart(start);
		this.finish = finish;
		this.readStart = readStart;
		this.instanceId = instanceId;
	}

	execution(String id, long start, long finish, long readStart, float cost, int instanceId) {
		this.id = id;
		this.setStart(start);

		this.finish = finish;
		this.readStart = readStart;
		this.Cost = cost;
		this.instanceId = instanceId;
	}

	public execution(String id, long start, long finish, long readStart, float cost, int energy, float reliability,
			float frequency, int instanceId, float freqLevel) {
		super();
		this.id = id;
		this.setStart(start);
		this.finish = finish;
		this.readStart = readStart;
		Cost = cost;
		Energy = energy;
		Reliability = reliability;
		Frequency = frequency;
		this.instanceId = instanceId;
		this.FrequencyLevel=freqLevel;
	}

	public execution(String id, long start, long finish, long readStart, float cost, int energy, float reliability,
			int instanceId) {
		super();
		this.id = id;
		this.setStart(start);
		this.finish = finish;
		this.readStart = readStart;
		Cost = cost;
		Energy = energy;
		Reliability = reliability;
		this.instanceId = instanceId;
	}
	

	public execution(String id, long start, long finish, long readStart, float cost, int energy, float reliability,
			float frequency, float frequencyLevel, int instanceId) {
		super();
		this.id = id;
		this.start = start;
		this.finish = finish;
		this.readStart = readStart;
		Cost = cost;
		Energy = energy;
		Reliability = reliability;
		Frequency = frequency;
		FrequencyLevel = frequencyLevel;
		this.instanceId = instanceId;
	}

	public String getId() {
		return id;
	}

	public long getExecutionTime() {
		if (start < 0) {
			System.out.print("start can  not  be zero");
		}
		return this.getFinish() - this.getStart();
	}

	public void setId(String id) {
		this.id = id;
	}

	public long getStart() {
		
		return start;
	}

	public void setStart(long start) {

		if (start < 0) {
					throw new ArithmeticException ("start of "+ getId()+ "can not be less than zero -start: "+start);
					//System.out.println("start of " + getId() + "can not be less than zero -start: " + start);
			
		} else {
			this.start = start;
		}
	}

	public long getFinish() {
		return finish;
	}

	public void setFinish(long finish) {

		this.finish = finish;
	}

	public long getReadStart() {
		return readStart;
	}

	public void setReadStart(long readStart) {
		this.readStart = readStart;
	}

	public float getCost() {
		return Cost;
	}

	public void setCost(float cost) {
		Cost = cost;
	}

	public int getEnergy() {
		return Energy;
	}

	public void setEnergy(int energy) {
		Energy = energy;
	}

	public float getReliability() {
		return Reliability;
	}

	public void setReliability(float reliability) {
		Reliability = reliability;
	}

	public float getFrequency() {
		if (Frequency == 0)
			Frequency = 1;
		return Frequency;
	}
	

	public float getFrequencyLevel() {
		return FrequencyLevel;
	}

	public void setFrequencyLevel(float frequencyLevel) {
		FrequencyLevel = frequencyLevel;
	}

	public void setFrequency(float frequency) {
		Frequency = frequency;
	}

	public int getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(int instanceId) {
		this.instanceId = instanceId;
	}

}
