package auto_scaling.monitor.cloudsim;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import auto_scaling.event.Event;
import auto_scaling.event.EventGenerator;
import auto_scaling.event.EventQueueManager;
import auto_scaling.event.Events;
import auto_scaling.handler.EventHandlerManager;
import auto_scaling.handler.cloudsim.CloudSimRequestEstimationHandler;
import auto_scaling.monitor.Monitor;

public class CloudSimRequestForecastMonitor extends Monitor {

	public CloudSimRequestForecastMonitor(String monitorName, int monitorInterval) {
		super(monitorName, monitorInterval);
	}

	@Override
	public void doMonitoring() throws Exception {
		/*
		 * Get the stream from request estimate event; Flush it inside synchronous
		 * block; Trigger the forecast event
		 */

		CloudSimRequestEstimationHandler reqEstimate = (CloudSimRequestEstimationHandler) EventHandlerManager
				.getEventHandlerManager().getEventHandler(Events.REQUEST_ESTIMATE_EVENT);
		PrintWriter reqStream = reqEstimate.getPrintWriter();
		synchronized (reqStream) {
			// Time to flush the stream and trigger the forecast event which sets the min
			// and max estimate as part of the systemStatus
			reqStream.flush();
			// Trigger the event here
			Map<String, Object> data = new HashMap<String, Object>();
			Event forecastEvent = EventGenerator.getEventGenerator().generateEvent(Events.REQUEST_FORECAST_EVENT, data);

			Queue<Event> eventQueue = EventQueueManager.getEventsQueue();
			eventQueue.add(forecastEvent);
			monitorLog.info(logFormatter.getGenerateEventLogString(forecastEvent, "Forecast event triggered"));
		}
	}

}
