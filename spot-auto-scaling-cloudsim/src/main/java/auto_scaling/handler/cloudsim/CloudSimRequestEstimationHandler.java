package auto_scaling.handler.cloudsim;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cloudbus.cloudsim.core.CloudSim;

import auto_scaling.capacity.ICapacityCalculator;
import auto_scaling.cloud.InstanceStatus;
import auto_scaling.cloud.InstanceTemplate;
import auto_scaling.cloud.RunningStatus;
import auto_scaling.core.SystemStatus;
import auto_scaling.event.Event;
import auto_scaling.event.EventGenerator;
import auto_scaling.event.EventQueueManager;
import auto_scaling.event.Events;
import auto_scaling.event.RequestEstimateEvent;
import auto_scaling.handler.EventHandler;
import auto_scaling.util.cloudsim.TimeConverter;

public class CloudSimRequestEstimationHandler extends EventHandler {
	
	protected long lastEstimate ;
	
	private File estimateFile ;
	
	private PrintWriter estimateWriter;
	
	private static Logger expectLog = LogManager.getLogger(CloudSimRequestEstimationHandler.class);

	protected ICapacityCalculator capacityCalculator;

	public void setCapacityCalculator(ICapacityCalculator capacityCalculator) {
		this.capacityCalculator = capacityCalculator;
	}
	
	public PrintWriter getPrintWriter() {
		return estimateWriter;
	}

	public CloudSimRequestEstimationHandler() {
		super(RequestEstimateEvent.class);
		lastEstimate = 0;
		estimateWriter = initEstimateFile();
	}
	
	private PrintWriter initEstimateFile() {
		estimateFile  = new File(System.getProperty("user.dir")+"\\estimate.txt");
		try {
			return new PrintWriter(estimateFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			expectLog.info("Exception while creating PrintWriter for estimate file");
			return null;
		}
	}

	@Override
	protected synchronized void doHandling(Event event) {
		eventHandlerLog.info(logFormatter.getMessage("Handling Request Estimation event"));
		// TODO Auto-generated method stub
		// Calculate the estimated number of request and log it to the file as
		// time,noOfRequest
		SystemStatus systemStatus = SystemStatus.getSystemStatus();
		double forecastWindow = systemStatus.getForecastTimeWindow();
		// update the estimated request rate
		synchronized (systemStatus) {
			Collection<InstanceStatus> allInstances = systemStatus.getAllInstances();
			long totalNumOfRequests = 0;
			for (InstanceStatus instanceStatus : allInstances) {

				if (instanceStatus.isAttached() && instanceStatus.getRunningStatus().equals(RunningStatus.RUNNING)) {
					Map<String, Number> resourceConsumptionValues = instanceStatus.getResourceConsumptionValues();
					InstanceTemplate instanceTemplate = instanceStatus.getType();
					long numOfrequests = capacityCalculator.getEstimatedNumOfRequestsByUtilization(instanceTemplate,
							resourceConsumptionValues);

					totalNumOfRequests += numOfrequests;
				}
			}
			estimateWriter.println(TimeConverter.convertSimulationTimeToString(CloudSim.clock())+","+totalNumOfRequests);
//			if(lastEstimate != totalNumOfRequests) {
			double cTime = Math.floor(CloudSim.clock());
			if((cTime % forecastWindow) == 0) {
				//Time to flush the stream and trigger the forecast event which sets the min and max estimate as part of the systemStatus
				expectLog.info("Flushing in progress- check file");
				estimateWriter.flush();
				//Trigger the event here
				Map<String, Object> data = new HashMap<String, Object>();
				Event forecastEvent = EventGenerator.getEventGenerator().generateEvent(Events.REQUEST_FORECAST_EVENT, data);
				
				Queue<Event> eventQueue = EventQueueManager.getEventsQueue();
				eventQueue.add(forecastEvent);
				expectLog.info(logFormatter.getGenerateEventLogString(forecastEvent, "Request Forecast Event"));
				eventHandlerLog.info(logFormatter.getGenerateEventLogString(
						forecastEvent, "Forecast event triggered"));
			}
			expectLog.info(TimeConverter.convertSimulationTimeToDate(CloudSim.clock())+"\t"+totalNumOfRequests);
			lastEstimate = totalNumOfRequests;
//			}
		}
	}
}
