package auto_scaling.event;

public class RequestForecastEvent extends Event {
	public RequestForecastEvent(int criticalLevel) {
		super(criticalLevel, Events.REQUEST_FORECAST_EVENT);
	}
}
