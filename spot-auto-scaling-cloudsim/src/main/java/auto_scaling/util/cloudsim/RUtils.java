package auto_scaling.util.cloudsim;

import java.util.HashMap;
import java.util.Map;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPFactor;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.RList;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

public class RUtils {

	public static String[] callUDFunction(String path, RConnection c, String paramsCombined, String functionName)
			throws RserveException, REXPMismatchException {
		// Sourcing the script
		c.eval("source(\"" + path + "\")");
		String[] result = null;
		// Running the function with the specified parameters
		if (paramsCombined != null) {
			String command = functionName + "(\"" + paramsCombined + "\")";
			result = c.eval(command).asStrings();
			// for(String i: result) {
			// System.out.println(i);
			// }
		} else {
			REXP res = null;
			res = c.eval(functionName + "()");
			System.out.println(res);
		}
		return result;
	}

	public static RList callUDFunctionForList(String path, RConnection con, String paramsCombined, String functionName)
			throws RserveException, REXPMismatchException {
		con.eval("source(\"" + path + "\")");
		RList result = null;
		String command = functionName + "(\"" + paramsCombined + "\")";
		result = con.eval(command).asList();

		return result;
	}

	public static Map<String, String> obtainMapFromList(RList result) {

		// The RList returns factors that needs to be converted to string and then
		// synthesized as list

		REXPFactor time = (REXPFactor) result.get(0);
		REXPFactor mean = (REXPFactor) result.get(1);
		REXPFactor loRange = (REXPFactor) result.get(2);
		REXPFactor hiRange = (REXPFactor) result.get(3);

		int len = time.length(); // The length is common across all Factors hence using one randomly

		String[] timeString = time.asStrings();
		String[] meanString = mean.asStrings();
		String[] loRangeString = loRange.asStrings();
		String[] hiRangeString = hiRange.asStrings();

		Map<String, String> forecast = new HashMap<String, String>();
		for (int i = 0; i < len; i++) {
			forecast.put(timeString[i], meanString[i] + "," + loRangeString[i] + "," + hiRangeString[i]);
		}

		return forecast;
	}
}
