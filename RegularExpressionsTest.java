import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import myfileio.MyFileIO;

// TODO: Auto-generated Javadoc
@TestMethodOrder(OrderAnnotation.class)
class RegularExpressionsTest {
	MyFileIO fio = new MyFileIO();
	RegularExpressions re = new RegularExpressions();
	private static final boolean DEBUG = false;

	static boolean validBranch = true;
    static Set<String> branches = new HashSet<String>(Arrays.asList("main","regex","benchmark"));
    
    
    private static String checkOriginalBranch(String sha) {
		BufferedReader br;
		String[] cmd = { "git", "switch", "-" };
		String output = sha;
		ProcessBuilder pb = new ProcessBuilder(cmd);
		pb.redirectErrorStream(true);
		try {
			Process proc = pb.start();
			proc.waitFor();
		} catch (Exception e) {
			System.out.println("Exception executing cmd");
		}
		
		String[] cmd2 = {"git","branch"};
		pb = new ProcessBuilder(cmd2);
		pb.redirectErrorStream(true);
		try {
			Process proc = pb.start();
			br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			proc.waitFor();
			output = extractActiveBranch(br);
			br.close();
		} catch (Exception e) {
			System.out.println("Exception executing cmd2");
		}
		
		String[] cmd3 = { "git", "checkout", sha };
		pb = new ProcessBuilder(cmd3);
		pb.redirectErrorStream(true);
		try {
			Process proc = pb.start();
			proc.waitFor();
		} catch (Exception e) {
			System.out.println("Exception executing cmd3");
		}
		return output;
   	
    }
    
	/**
	 * Extract process output.
	 *
	 * @param br the br
	 * @return the array list
	 * @throws Exception the exception
	 */
	private static String extractActiveBranch(BufferedReader br) throws Exception {
		String output;
		while ((output = br.readLine()) != null) {
			if (output.contains("*")) {
				if (output.contains("HEAD detached")) {
					output = output.replaceAll(".*at\s+(\\w+).*","$1");
				} else {
					output = output.replaceAll(".*\\s+", "");
				}
				return output;
			}
		}
		return "main";
	}

	/**
	 * Execute process.
	 *
	 * @param cmd       the cmd
	 * @param directory the directory
	 * @param timeout   the timeout
	 * @return the process results
	 */
	private static String getActiveBranch() {
		BufferedReader br;
		String[] cmd = { "git", "branch" };
		String output = "main";
		ProcessBuilder pb = new ProcessBuilder(cmd);
		pb.redirectErrorStream(true);
		try {
			Process proc = pb.start();
			br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			proc.waitFor();
			output = extractActiveBranch(br);
			br.close();
		} catch (Exception e) {
			System.out.println("Exception executing cmd");
		}
		if (!branches.contains(output)) {
			output = checkOriginalBranch(output);
		}
		return output;
	}    
    
    /**
     * Gets the operating system from the System.
     *
     * @return the operating system from the 
     */
    private static String getOperatingSystem() {
    	String os = System.getProperty("os.name");
    	return os;
    }
	
	/**
	 * Determines which OS the test is running on, as it makes a difference
	 *
	 * @throws Exception the exception
	 */
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		System.out.println("Running on: "+getOperatingSystem());
		String branch = getActiveBranch();
		validBranch = branches.contains(branch);
	}

	/**
	 * Test swapLastFirst.  Tests a variety of conditions, including
	 * where there is whitespace (spaces, tabs, etc - use the \\s to match)
	 * BEFORE or AFTER (or both) the portion of the name. Last names only and
	 * First names only cases should still match appropriately, so 
	 * "lastName," should produce " lastName" and ",firstName" should
	 * produce "firstName ". All cases are
	 * tested with individual PASS/FAIL results to hopefully aid in debugging.
	 */
	@Test
	@Order(1)
	void test_SwapLastFirst() {
		Assumptions.assumeTrue(validBranch);
		System.out.println("Testing swapLastFirst:");
		File fData = fio.getFileHandle("data/.regex_test_data1");
		BufferedReader brData = fio.openBufferedReader(fData);
		File fExp = fio.getFileHandle("data/.regex_expect_data1");
		BufferedReader brExp = fio.openBufferedReader(fExp);
		ArrayList<String> results = new ArrayList<String>();
		ArrayList<String> input = new ArrayList<String>();
		
		int numTests =  0;
		String line = null;
		boolean done = false;
		boolean passed = true;
		while (!done) {
			try {
				line = brData.readLine();
			} 
			catch (IOException e) {
				System.out.println("Caught an IO exception on iteration "+numTests);
				e.printStackTrace();
			}
			if (line != null) {
				numTests++;
				results.add(re.swapLastFirst(line));
				input.add(line);
			} else {
				done = true;
			}
		}
		fio.closeFile(brData);
		for (int i = 0; i < numTests; i++) {
			System.out.print("   swapLastFirst("+input.get(i)+") ==> returns ");
			System.out.print(results.get(i));
			try {
				line = brExp.readLine();
			}
			catch (IOException e) {
				System.out.println("Caught an IO exception on iteration "+numTests);
				e.printStackTrace();
			}
			assertTrue(line != null);
			if (line.equals(results.get(i))) {
				System.out.println("  PASSED");
			} else {
				System.out.println("  FAILED");
				passed = false;
			}
		}
		fio.closeFile(brExp);
		assertTrue(passed);
	}

	/**
	 * Test padTokensWithSpaces. Checks a variety of cases to ensure
	 * that padTokensWithSpaces correctly matches the required tokens 
	 * and inserts the spaces in the appropriate places. All cases are
	 * tested with individual PASS/FAIL results to hopefully aid in debugging.
	 */
	@Test
	@Order(2)
	void test_PadTokensWithSpaces() {
		Assumptions.assumeTrue(validBranch);
		boolean passed = true;
		System.out.println("Testing padTokensWithSpaces:");
		File fData = fio.getFileHandle("data/.regex_test_data23");
		BufferedReader brData = fio.openBufferedReader(fData);
		File fExp = fio.getFileHandle("data/.regex_expect_data2");
		BufferedReader brExp = fio.openBufferedReader(fExp);
		ArrayList<String> results = new ArrayList<String>();
		ArrayList<String> input = new ArrayList<String>();
		
		int numTests =  0;
		String line = null;
		boolean done = false;
		while (!done) {
			try {
				line = brData.readLine();
			} 
			catch (IOException e) {
				System.out.println("Caught an IO exception on iteration "+numTests);
				e.printStackTrace();
			}
			if (line != null) {
				numTests++;
				results.add(re.padTokensWithSpaces(line));
				input.add(line);
			} else {
				done = true;
			}
		}
		fio.closeFile(brData);
		for (int i = 0; i < numTests; i++) {
			if (DEBUG) System.out.println(results.get(i));
			System.out.print("   padTokensWithSpaces("+input.get(i)+") ==> returns ");
			System.out.print(results.get(i));
			try {
				line = brExp.readLine();
			}
			catch (IOException e) {
				System.out.println("Caught an IO exception on iteration "+numTests);
				e.printStackTrace();
			}
			assertTrue(line != null);
			if (line.equals(results.get(i))) {
				System.out.println("  PASSED");
			} else {
				System.out.println("  FAILED");
				passed = false;
			}
		}
		fio.closeFile(brExp);
		assertTrue(passed);
	}

	/**
	 * Test identifyTokenType. Checks a variety of cases to ensure
	 * that identifyTokenType correctly identifies tokens in a numeric 
	 * string as Operations, Integers, Doubles, or an Error. The type 
	 * is expected to be prepended to the token, followed by a ": " and then
	 * the token, as shown in the example in the main() method of the
	 * RegularExpressions class. All test cases are 
	 * tested with individual PASS/FAIL results to hopefully aid in debugging.
	 */
	@Test
	@Order(3)
	void test_IdentifyTokenType() {
		Assumptions.assumeTrue(validBranch);
		boolean passed = true;
		System.out.println("Testing identifyTokenType:");
		File fData = fio.getFileHandle("data/.regex_test_data23");
		BufferedReader brData = fio.openBufferedReader(fData);
		File fExp = fio.getFileHandle("data/.regex_expect_data3");
		BufferedReader brExp = fio.openBufferedReader(fExp);
		ArrayList<String> results = new ArrayList<String>();
		ArrayList<String> input = new ArrayList<String>();
		
		int numTests =  0;
		String line = null;
		String[] tokens;
		String str = "";
		boolean done = false;
		while (!done) {
			try {
				line = brData.readLine();
			} 
			catch (IOException e) {
				System.out.println("Caught an IO exception on iteration "+numTests);
				e.printStackTrace();
			}
			if (line != null) {
				numTests++;
				tokens = re.identifyTokenType(line);
				str = "";
				for (int i = 0; i < tokens.length; i++) {
					if (i>0)
						str += ",";
					str += tokens[i].replaceFirst("^\\s*", "");
				}
				results.add(str);
				input.add(line);
			} else {
				done = true;
			}
		}
		fio.closeFile(brData);
		for (int i = 0; i < numTests; i++) {
			if (DEBUG) System.out.println(results.get(i));

			System.out.println("   identifyTokenType("+input.get(i)+") ==> returns ");
			System.out.print("   "+results.get(i));
			try {
				line = brExp.readLine();
			}
			catch (IOException e) {
				System.out.println("Caught an IO exception on iteration "+numTests);
				e.printStackTrace();
			}
			assertTrue(line != null);
			if (line.equals(results.get(i))) {
				System.out.println("  PASSED");
			} else {
				System.out.println("  FAILED");
				passed = false;
			}
		}
		fio.closeFile(brExp);
		assertTrue(passed);
	}

}
