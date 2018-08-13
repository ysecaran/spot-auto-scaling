package auto_scaling.handler.cloudsim;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cloudbus.cloudsim.core.CloudSim;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.RList;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

import auto_scaling.event.Event;
import auto_scaling.event.RequestForecastEvent;
import auto_scaling.handler.EventHandler;
import auto_scaling.util.cloudsim.RUtils;

public class CloudSimRequestForecastHandler extends EventHandler {

	private static Logger foreCastLog = LogManager.getLogger(CloudSimRequestForecastHandler.class);
	public static String FORECAST_SCRIPT = "forecastRequests.R";

	/*
	 * Initialize Log Remove SOP Make sure a copy of the file is written to the logs
	 * as estimate_cloudsimClockTime.txt Log the error from running the process
	 * command - can this be made a graceful exit?
	 * 
	 */

	public CloudSimRequestForecastHandler() {
		super(RequestForecastEvent.class);
	}

	@Override
	protected void doHandling(Event event) {
		// estimateFile = new File(System.getProperty("user.dir")+"\\estimate.txt");
		try {
			Runtime.getRuntime().exec(new String[] { "R", "CMD", "Rserve" });

			// logInformationFromStream(startServer.getErrorStream());

			// Have to log the output of the command and the error to a file with timestamp
			// for each time the command is run
			RConnection con = new RConnection();
			String path = (System.getProperty("user.dir") + "\\" + CloudSimRequestForecastHandler.FORECAST_SCRIPT)
					.replace('\\', '/');
			String estimateFile = (System.getProperty("user.dir") + "\\estimate.txt").replace('\\', '/');

			foreCastLog.info("Loading script from:" + path);
			foreCastLog.info("Estimate file loaded from :" + estimateFile);

			File backUpFile = new File(System.getProperty("user.dir") + "/EstimateBackup");
			if (!backUpFile.exists()) {
				boolean outcome = backUpFile.mkdir();
				foreCastLog.info("is directory created for backups:" + outcome);
			}
			backUpFile = new File(backUpFile.getPath() + "/estimate_" + CloudSim.clock() + ".txt");
			foreCastLog.info("Creating backup:" + backUpFile.getPath());

			// Make a copy of Estimate File for reference - this is only until the estimates
			// are incremental
			FileUtils.copyFile(new File(estimateFile), backUpFile);

			// Continuing to call the Rscript
			RList res = RUtils.callUDFunctionForList(path, con, estimateFile, "forecastEstimate");
			Map<String, String> forecast = RUtils.obtainMapFromList(res);

			// Writing the forecasts to a file for comparison with the estimates - this is
			// just temporary
			File forecastFile = new File(
					System.getProperty("user.dir") + "/EstimateBackup/forecast_" + CloudSim.clock() + ".txt");
			foreCastLog.info("Forecast File:" + forecastFile.getPath());
			PrintWriter p = new PrintWriter(new FileWriter(forecastFile));
			Iterator<String> keysIterator = forecast.keySet().iterator();
			while (keysIterator.hasNext()) {
				String key = keysIterator.next();
				p.println(key + "\t" + forecast.get(key));
			}
			p.flush();
			p.close();

			// TODO:Do i need to set the system status with the forecasts? Need to work on
			// the logic for that
			con.shutdown();
		} catch (IOException | RserveException | REXPMismatchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			foreCastLog.info(e.getMessage());
			System.exit(0);
		}
	}

	// private void logInformationFromStream(InputStream in) {
	// BufferedReader reader = new BufferedReader(new InputStreamReader(in));
	// try {
	// String content = reader.readLine();
	// while (content != null) {
	// foreCastLog.info(content);
	// content = reader.readLine();
	// }
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// System.exit(0);
	// }
	// }

}
