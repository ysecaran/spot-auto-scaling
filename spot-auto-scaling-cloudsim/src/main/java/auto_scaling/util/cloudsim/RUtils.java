package auto_scaling.util.cloudsim;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
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
}
