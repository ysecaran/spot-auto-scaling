package auto_scaling.event;

public class RequestEstimateEvent extends Event{
	
	public RequestEstimateEvent(int criticalLevel) {
		super(criticalLevel, Events.REQUEST_ESTIMATE_EVENT);
	}

}
