package Broker;

public enum ScheduleType {
	Fastest("Fastest"), Cheapest("Cheapest"), CheapestCP("CheapestCP"), MY_FAST("MY_FAST"), HEFT("HEFT"), BDHEFT("BDHEFT"), MYBDHEFT("MYBDHEFT"),FastestCP("FastestCP"),ERES_Neha("ERES_Neha"),NOSF("NOSF"),ECTD("ECTD"),CTD("CTD"),EUSF("EUSF"),CUSF("CUSF");
	
	private final String value;

	private ScheduleType(String value) {
		this.value = value;
	}

	public String toString() {
		return value;
	}

	public static ScheduleType convert(String val) {
		for (ScheduleType inst : values()) {
			if (inst.toString().equals(val)) {
				return inst;
			}
		}
		return null;
	}
}
