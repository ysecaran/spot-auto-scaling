package auto_scaling.util.cloudsim;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/** 
* @ClassName: TimeConverter 
* @Description: convert simulation time to date
* @author Chenhao Qu
* @date 07/06/2015 5:19:07 pm 
*  
*/
public class TimeConverter {

	/** 
	* @Fields simulationStartTime : the start timestamp of the simulation
	*/ 
	private static Date simulationStartTime;
	
	/**
	 * @Title: convertSimulationTimeToDate 
	 * @Description: convert simulation time to date
	 * @param simulationTime the simulation time
	 * @return the timestamp
	 * @throws
	 */
	public static Date convertSimulationTimeToDate(double simulationTime) {
		return new Date(simulationStartTime.getTime() + (long)simulationTime * 1000);
	}
	
	public static String convertSimulationTimeToString(double simulationTime) {
		Date temp = new Date(simulationStartTime.getTime() + (long) simulationTime * 1000);
		SimpleDateFormat sd = new SimpleDateFormat("MM/dd/yyyy,HH:mm:ss");
		String d = sd.format(temp);
//		System.out.println(d);
		return d;
		
	}
	
	/**
	 * @Title: setSimulationStartTime 
	 * @Description: set the simulation start time
	 * @param startTime the start time
	 * @throws
	 */
	public static void setSimulationStartTime(Date startTime) {
		simulationStartTime = startTime;
	}
}
