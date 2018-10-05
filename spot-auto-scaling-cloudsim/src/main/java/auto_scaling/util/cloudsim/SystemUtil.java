package auto_scaling.util.cloudsim;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;

import org.cloudbus.cloudsim.core.CloudSim;

import auto_scaling.cloud.InstanceStatus;
import auto_scaling.cloud.OnDemandInstanceStatus;
import auto_scaling.cloud.SpotInstanceStatus;
import auto_scaling.cloud.SpotPricingStatus;
import auto_scaling.core.SpotPricingManager;

public class SystemUtil {

	public static double getUsageInMinutes(Date launchTime, Date endTime) {
		double durationInMinutes = 0;
		try {
			// Make sure the time zone is UTC and passed as VM Argument
			Calendar calInstance = Calendar.getInstance();
			calInstance.setTime(launchTime);
			LocalDateTime launch = LocalDateTime.of(calInstance.get(Calendar.YEAR),
					(calInstance.get(Calendar.MONTH) + 1), calInstance.get(Calendar.DAY_OF_MONTH),
					calInstance.get(Calendar.HOUR_OF_DAY), calInstance.get(Calendar.MINUTE),
					calInstance.get(Calendar.SECOND));

			calInstance.setTime(endTime);
			LocalDateTime end = LocalDateTime.of(calInstance.get(Calendar.YEAR), (calInstance.get(Calendar.MONTH) + 1),
					calInstance.get(Calendar.DAY_OF_MONTH), calInstance.get(Calendar.HOUR_OF_DAY),
					calInstance.get(Calendar.MINUTE), calInstance.get(Calendar.SECOND));
			
			durationInMinutes = Duration.between(launch, end).toMinutes();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("\t Time of occurence:" + CloudSim.clock());
		}

		return durationInMinutes;
	}

	public static double getUsageInHours(Date launchTime, Date endTime) {
		return Math.ceil((getUsageInMinutes(launchTime, endTime) / 60));
	}

	public static double getInstancePrice(InstanceStatus status) {
		double price = 0.0;
		if (status instanceof SpotInstanceStatus) {
			SpotInstanceStatus in = (SpotInstanceStatus) status;
			// double bidPrice = in.getBiddingPrice();
			SpotPricingStatus spotStatus = SpotPricingManager.getSpotPricingManager()
					.getSpotPricingStatus(in.getType());
			price = spotStatus.getPrice();
		} else if (status instanceof OnDemandInstanceStatus) {
			OnDemandInstanceStatus on = (OnDemandInstanceStatus) status;
			price = on.getType().getOnDemandPrice();
		}
		return price;
	}

	/*
	 * This method right now accepts the file and returns the estimate - which
	 * includes starting and stopping of Rserve. In future this shall be moved to a
	 * separate util file seeking inputs like 'h',order of the model,
	 */
	public static String getEstimatesFromRserve(String filePath) {
		return null;
	}

}
