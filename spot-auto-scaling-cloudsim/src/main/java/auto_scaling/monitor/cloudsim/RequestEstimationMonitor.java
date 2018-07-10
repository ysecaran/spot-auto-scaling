package auto_scaling.monitor.cloudsim;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import auto_scaling.core.SystemStatus;
import auto_scaling.event.Event;
import auto_scaling.event.EventGenerator;
import auto_scaling.event.EventQueueManager;
import auto_scaling.event.Events;
import auto_scaling.monitor.Monitor;

public class RequestEstimationMonitor extends Monitor {

	public RequestEstimationMonitor(String monitorName, int monitorInterval) {
		super(monitorName, monitorInterval);
	}

	@Override
	public void doMonitoring() throws Exception {
		
		long totalNoOfRequests = SystemStatus.getSystemStatus().getTotalNumOfRequests();
		if (totalNoOfRequests <= 0) {
			return;
		}
		
		
		EventGenerator eventGenerator = EventGenerator.getEventGenerator();
		Map<String, Object> data = new HashMap<String, Object>();
		Event newEvent = eventGenerator.generateEvent(
				Events.REQUEST_ESTIMATE_EVENT, data);

		Queue<Event> eventQueue = EventQueueManager.getEventsQueue();
		eventQueue.add(newEvent);
		monitorLog.info(logFormatter.getGenerateEventLogString(newEvent, "Request Estimate Event"));

	}

}
