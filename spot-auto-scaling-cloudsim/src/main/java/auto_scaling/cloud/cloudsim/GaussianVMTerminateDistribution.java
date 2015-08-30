package auto_scaling.cloud.cloudsim;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.tuple.Pair;
import org.uncommons.maths.number.NumberGenerator;
import org.uncommons.maths.random.GaussianGenerator;
import org.uncommons.maths.random.MersenneTwisterRNG;

import auto_scaling.cloud.InstanceTemplate;

/** 
* @ClassName: GaussianVMTerminateDistribution 
* @Description: vm termination time following gaussian distribution
* @author Chenhao Qu
* @date 04/06/2015 1:50:45 pm 
*  
*/
public class GaussianVMTerminateDistribution implements IVMTerminateDistribution{


	/** 
	* @Fields defaultValue : the default value
	*/ 
	protected final long defaultValue;
	/** 
	* @Fields delayGenerators : gaussian distributions for each vm type
	*/ 
	protected final Map<InstanceTemplate, NumberGenerator<Double>> delayGenerators = new HashMap<InstanceTemplate, NumberGenerator<Double>>();


	/** 
	* <p>Description: initialize with mean, variance, random seed, and default value</p> 
	* @param delayDefs the mean and variance for each vm type
	* @param seed the random seed
	* @param defaultVal the default value
	*/
	public GaussianVMTerminateDistribution(
			final Map<InstanceTemplate, Pair<Double, Double>> delayDefs,
			final byte[] seed, final long defaultVal) {
		Random merseneGenerator = new MersenneTwisterRNG(seed);

		this.defaultValue = defaultVal;

		for (Map.Entry<InstanceTemplate, Pair<Double, Double>> entry : delayDefs
				.entrySet()) {
			this.delayGenerators.put(entry.getKey(), new GaussianGenerator(
					entry.getValue().getLeft(), entry.getValue().getRight(),
					merseneGenerator));
		}
	}

	/* (non-Javadoc) 
	* <p>Title: getDelay</p> 
	* <p>Description: return delay following gaussian distribution</p> 
	* @param instanceTemplate
	* @return 
	* @see auto_scaling.cloud.cloudsim.IVMTerminateDistribution#getDelay(auto_scaling.cloud.InstanceTemplate) 
	*/
	@Override
	public long getDelay(InstanceTemplate instanceTemplate) {
		long result = defaultValue;
		NumberGenerator<Double> gaussianGenerator = null;

		if (delayGenerators.containsKey(instanceTemplate)) {
			gaussianGenerator = delayGenerators.get(instanceTemplate);
		}

		if (gaussianGenerator != null) {
			result = gaussianGenerator.nextValue().longValue();
		}

		return result;
	}
}
