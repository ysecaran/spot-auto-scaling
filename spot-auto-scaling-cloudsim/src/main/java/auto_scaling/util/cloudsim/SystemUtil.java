package auto_scaling.util.cloudsim;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;

import org.cloudbus.cloudsim.core.CloudSim;

import auto_scaling.cloud.InstanceStatus;
import auto_scaling.cloud.OnDemandInstanceStatus;
import auto_scaling.cloud.SpotInstanceStatus;
import auto_scaling.cloud.SpotPricingStatus;
import auto_scaling.core.SpotPricingManager;

public class SystemUtil {
	
	public static double getUsageInMinutes(Date launchTime,Date endTime) {
		LocalDateTime launch = LocalDateTime.of(launchTime.getYear(), launchTime.getMonth() + 1,
				launchTime.getDay(), launchTime.getHours(), launchTime.getMinutes(),
				launchTime.getSeconds());
		LocalDateTime end = LocalDateTime.of(endTime.getYear(), endTime.getMonth() + 1, endTime.getDay(),
				endTime.getHours(), endTime.getMinutes(), endTime.getSeconds());
		long durationInMinutes = Duration.between(launch, end).toMinutes();
		return durationInMinutes;
	}
	
	public static double getUsageInHours(Date launchTime,Date endTime) {
		return Math.ceil((getUsageInMinutes(launchTime,endTime) / 60));
	}
	
	public static double getInstancePrice(InstanceStatus status) {
		double price = 0.0;
		if (status instanceof SpotInstanceStatus) {
			SpotInstanceStatus in = (SpotInstanceStatus) status;
//			double bidPrice = in.getBiddingPrice();
			SpotPricingStatus spotStatus = SpotPricingManager.getSpotPricingManager()
					.getSpotPricingStatus(in.getType());
			price = spotStatus.getPrice();
		} else if (status instanceof OnDemandInstanceStatus) {
			OnDemandInstanceStatus on = (OnDemandInstanceStatus) status;
			price =  on.getType().getOnDemandPrice();
		}
		return price;
	}
	
	/*
	 * This method right now accepts the file and returns the estimate - which includes starting and stopping of Rserve.
	 * In future this shall be moved to a separate util file seeking inputs like 'h',order of the model,
	 */
	public static String getEstimatesFromRserve(String filePath) {
		return null;
	}

}
