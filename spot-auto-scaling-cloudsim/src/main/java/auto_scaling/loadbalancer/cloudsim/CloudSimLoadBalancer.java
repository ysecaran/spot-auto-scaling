package auto_scaling.loadbalancer.cloudsim;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.core.CloudSim;

import auto_scaling.cloud.InstanceStatus;
import auto_scaling.cloud.OnDemandInstanceStatus;
import auto_scaling.cloud.RunningStatus;
import auto_scaling.cloud.SpotInstanceStatus;
import auto_scaling.cloud.SpotPricingStatus;
import auto_scaling.core.SpotPricingManager;
import auto_scaling.core.cloudsim.ApplicationBrokerManager;
import auto_scaling.core.cloudsim.CloudSimBroker;
import auto_scaling.loadbalancer.LoadBalancer;
import auto_scaling.loadbalancer.weightcalculator.IWeightCalculator;
import auto_scaling.util.cloudsim.SystemUtil;
import auto_scaling.util.cloudsim.TimeConverter;

/**
 * @ClassName: CloudSimLoadBalancer
 * @Description: the load balancer implementation for cloudSim
 * @author Chenhao Qu
 * @date 06/06/2015 1:50:51 pm
 * 
 */
public class CloudSimLoadBalancer extends LoadBalancer {

	protected Map<String, Date> billableInstances;
	protected List<InstanceStatus> instancesStillOnline;
	protected Map<String, Double> costOfInstances;

	/**
	 * <p>
	 * Description: initialize with weight calculator
	 * </p>
	 * 
	 * @param weightCalculator
	 *            the weight calculator
	 */
	public CloudSimLoadBalancer(IWeightCalculator weightCalculator) {
		super(weightCalculator);
		billableInstances = Collections.synchronizedMap(new HashMap<String, Date>());
		costOfInstances = Collections.synchronizedMap(new HashMap<String,Double>());
		instancesStillOnline = Collections.synchronizedList(new ArrayList<InstanceStatus>());
	}

	/*
	 * (non-Javadoc) <p>Title: rebalance</p> <p>Description: </p>
	 * 
	 * @param weights
	 * 
	 * @return
	 * 
	 * @throws IOException
	 * 
	 * @see auto_scaling.loadbalancer.LoadBalancer#rebalance(java.util.Map)
	 */
	@Override
	protected boolean rebalance(Map<InstanceStatus, Integer> weights) throws IOException {
		ApplicationBrokerManager applicationBrokerManager = ApplicationBrokerManager.getApplicationBrokerManager();
		CloudSimBroker cloudSimBroker = applicationBrokerManager.getCloudSimBroker();

		Map<Integer, Integer> newWeights = new HashMap<Integer, Integer>();
		String logMsg = "";
		for (InstanceStatus instanceStatus : weights.keySet()) {
			int id = Integer.parseInt(instanceStatus.getId());
			int weight = weights.get(instanceStatus);
			newWeights.put(id, weight);
			logMsg += id + ": " + weight + ", ";
		}
		lbLog.info(logFormatter.getMessage(logMsg));
		return cloudSimBroker.rebalance(newWeights);
	}

	public boolean attach(Collection<InstanceStatus> instances) throws IOException {
		boolean isSuccess = super.attach(instances);
		if (isSuccess) {
			for (InstanceStatus status : instances) {
				billableInstances.put(status.getId(), status.getLaunchTime());
			}
		}
		Iterator<InstanceStatus> it = instances.iterator();
		lbLog.trace("Attach start : Taking Stock after calling super.attach");
		while (it.hasNext()) {
			InstanceStatus inst = it.next();
			if (inst instanceof SpotInstanceStatus) {
				SpotInstanceStatus in = (SpotInstanceStatus) inst;
				double bidPrice = in.getBiddingPrice();
				SpotPricingStatus status = SpotPricingManager.getSpotPricingManager()
						.getSpotPricingStatus(in.getType());
				double marketPrice = status.getPrice();
				lbLog.info("Bid Price:" + bidPrice + "\t Market Price:" + marketPrice);
			} else if (inst instanceof OnDemandInstanceStatus) {
				OnDemandInstanceStatus on = (OnDemandInstanceStatus) inst;
				lbLog.info("On dem price:" + on.getType().getOnDemandPrice());
			}
			lbLog.info("id:" + inst.getId() + "\tAttached:" + inst.isAttached() + "\tType:" + inst.getType().getName()
					+ "\tLaunch Time:" + inst.getLaunchTime());
		}
		lbLog.trace("Attach end : Taking Stock");
		return isSuccess;
	}

	public boolean detach(Collection<InstanceStatus> instances) throws IOException {

		boolean isSuccess = super.detach(instances);
		if (isSuccess) {
			lbLog.trace("Detach - start: Calculating duration");
			for (InstanceStatus status : instances) {
				lbLog.info("Instance attached:" + status.isAttached() + "\tId:"+status.getId());
				if (!status.isAttached()) {
					Date launchTime = billableInstances.get(status.getId());
					Date endTime = TimeConverter.convertSimulationTimeToDate(CloudSim.clock());
//					LocalDateTime launch = LocalDateTime.of(launchTime.getYear(), launchTime.getMonth() + 1,
//							launchTime.getDay(), launchTime.getHours(), launchTime.getMinutes(),
//							launchTime.getSeconds());
//					LocalDateTime end = LocalDateTime.of(endTime.getYear(), endTime.getMonth() + 1, endTime.getDay(),
//							endTime.getHours(), endTime.getMinutes(), endTime.getSeconds());
//					long durationInMinutes = Duration.between(launch, end).toMinutes();
//					double durationInHours = (double) durationInMinutes / 60;// Duration.between(launch, end).toHours();
//					double finalDurationInHours= Math.ceil(durationInHours);
					long durationInMinutes = (long) SystemUtil.getUsageInMinutes(launchTime, endTime);
					double finalDurationInHours= SystemUtil.getUsageInHours(launchTime, endTime);
					double price = getInstancePrice(status);
					double cost = price * finalDurationInHours;
					costOfInstances.put(status.getId(), new Double(cost));
					lbLog.info("Id:" + status.getId() + "\t LaunchTime:" + launchTime + "\t" + endTime
							+ "\tUsed Duration in minutes:" + durationInMinutes + "\t Used duration in hours:"+finalDurationInHours+ "\tprice:" + price + "\tCost:"
							+cost );
				}
				lbLog.trace("Detach - end: Calculating duration\tSize:"+attachedInstances.size());
//				//There are some situations where in one instance is still attached to the load balancer and running always. This will be billed after the simulation ends
				if(attachedInstances.size() ==1 && attachedInstances.get(0).getRunningStatus().equals(RunningStatus.RUNNING)) {
					InstanceStatus i = attachedInstances.get(0);
					lbLog.info("\tIs Attached:"+ i.isAttached()+"\tId:"+i.getId()+"\tStatus:"+i.getRunningStatus());
					instancesStillOnline.add(i);
					double price = getInstancePrice(i);
					lbLog.info("Price of the last instance online"+price);
//				lbLog.info(attachedInstances.get(0).getId());
//				lbLog.info("\tIs Attached:"+ attachedInstances.get(0).isAttached());
////				lbLog.info("Is present in instances argument:"+instances.contains(attachedInstances.get(0)));
////				//Collection<InstanceStatus> attachedAtTheEnd = InstanceFilter.getAttachedInstances(attachedInstances);
////				InstanceStatus st = attachedInstances.get(0);
			
				}
			}
		}
		
		// Iterator<InstanceStatus> it = instances.iterator();
		// lbLog.trace("Detach Trace Start: Taking stock");
		// while(it.hasNext()) {
		// InstanceStatus inst = it.next();
		// if(inst instanceof SpotInstanceStatus) {
		// SpotInstanceStatus in = (SpotInstanceStatus)inst;
		// double bidPrice = in.getBiddingPrice();
		// SpotPricingStatus status =
		// SpotPricingManager.getSpotPricingManager().getSpotPricingStatus(in.getType());
		// double marketPrice= status.getPrice();
		// lbLog.info("Bid Price:"+bidPrice+"\t Market Price:"+marketPrice);
		// }
		// else if(inst instanceof OnDemandInstanceStatus) {
		// OnDemandInstanceStatus on = (OnDemandInstanceStatus)inst;
		// lbLog.info("On dem price:"+on.getType().getOnDemandPrice());
		// }
		// lbLog.info("id:"+inst.getId()+"\tAttached:"+inst.isAttached()+"\tType:"+inst.getType().getName()+"\tLaunch
		// Time:"+inst.getLaunchTime());
		// }
		// lbLog.trace("Detach Trace End: Taking stock");
		// lbLog.trace("Finishing attach:"+costOfInstances);
		return isSuccess;
	}

	// private void addNewlyAttachedInstances() {
	// for(InstanceStatus instance :attachedInstances) {
	// if(!costOfInstances.containsKey(instance.getId())) {
	// //Not present in the map and hence adding
	// costOfInstances.put(instance.getId(), instance.getLaunchTime());
	// }
	// }
	// }
	public Map<String,Double> getCostOfInstances(){
		return costOfInstances;
	}
	
	public Map<String,Date> getBillableInstances(){
		return billableInstances;
	}
	
	public List<InstanceStatus> getInstancesStillOnline(){
		return instancesStillOnline;
	}
	
	public double getInstancePrice(InstanceStatus status) {
		double price = 0.0;
		if (status instanceof SpotInstanceStatus) {
			SpotInstanceStatus in = (SpotInstanceStatus) status;
			double bidPrice = in.getBiddingPrice();
			SpotPricingStatus spotStatus = SpotPricingManager.getSpotPricingManager()
					.getSpotPricingStatus(in.getType());
			price = spotStatus.getPrice();
			lbLog.info("Bid Price:" + bidPrice + "\t Market Price:" + price);
		} else if (status instanceof OnDemandInstanceStatus) {
			OnDemandInstanceStatus on = (OnDemandInstanceStatus) status;
			price =  on.getType().getOnDemandPrice();
			lbLog.info("On dem price:" + price);
		}
		lbLog.info("Price returned:"+price);
		return price;
	}
	
}
