import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(OrderAnnotation.class)
class BenchmarkFileAccessTest {
	
	static final int MIN_FILE_LENGTH = 100;
	static final int TEST_SCALE_FACTOR = 20;
	static final int REPEAT_FACTOR = 3;
	static final int NUM_TESTS = 5;
	static final int NUM_ITER = 10;
	
	/** The first 10. */
	String[] first10 = {"mupnbhpirr","vsjjgiavuz","ubmugngmiw","mjhktqcwgj","wjkzgzccak"};

    static boolean validBranch = true;
    static Set<String> branches = new HashSet<String>(Arrays.asList("main","benchmark"));
    
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
		BenchmarkFileAccess.main(null);
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	double calcModifiedAverage(long[] results) {
		Arrays.sort(results);
		double sum = 0.0;
		for (int i = 1; i < results.length - 1; i++) {
			sum += results[i];
		}
		return sum/(results.length-2);
	}
	
	void calculateTestAverages(File bmResults, double[] averages) {
		assertTrue(bmResults.exists());
		assertTrue(bmResults.length()>MIN_FILE_LENGTH);
		String line;
		long[][] resArray = new long[NUM_TESTS][];
		int test = 0;
		try (BufferedReader br = new BufferedReader(new FileReader(bmResults))) {
			while ((line = br.readLine()) != null) {
				int iter = 0;
				String[] tokens = line.split(",");
				if (!tokens[0].contains("test_")) continue;
				resArray[test] = new long[tokens.length - 1];
				while (iter < tokens.length-1) {
					long data = Long.parseLong(tokens[iter+1]);
					assertTrue(data > 0);
					resArray[test][iter] = data;
					iter++;
				}
				averages[test] = calcModifiedAverage(resArray[test]);
				test++;
			}	
			br.close();
		} catch (Exception e) {
			fail();
		}
	}

	boolean checkTestAverages(double[] testAverages) {
		boolean[] passingTests = new boolean[NUM_TESTS - 1]; // only do 4 relative comparisons
		boolean result = true;
		for (int i = 0; i < NUM_TESTS -1; i++) {
			System.out.print("Test_"+(i+1)+"/Test_"+i+" average = " +
		                       ((double) testAverages[i+1]/testAverages[i]));
			passingTests[i] = testAverages[i] < REPEAT_FACTOR * testAverages[i+1];
			System.out.println("    "+((passingTests[i] ? "PASSED" : "FAILED")));
			result = result && passingTests[i];
		}
		return result;
	}
	
	@Test
	@Order(2)
	void test_FileReaderBM() {
		Assumptions.assumeTrue(validBranch);
		String frbm = "output/FileReaderBenchmark.csv";
		double[] testAverages = new double[NUM_TESTS];
		calculateTestAverages(new File(frbm),testAverages);
		System.out.println("Checking data from "+frbm);
		boolean status = checkTestAverages(testAverages);
		assertTrue(status);
		assertTrue(TEST_SCALE_FACTOR*testAverages[0]<testAverages[NUM_TESTS-1]);
		System.out.println("Test_" + (NUM_TESTS - 1) + "/Test_0 averages = "
		                   + testAverages[NUM_TESTS -1]/testAverages[0]);
		
	}

	@Test
	@Order(3)
	void test_BufferedReaderBM() {
		Assumptions.assumeTrue(validBranch);
		String brbm = "output/BufferedReaderBenchmark.csv";
		double[] testAverages = new double[NUM_TESTS];
		calculateTestAverages(new File(brbm),testAverages);
		System.out.println("Checking data from "+brbm);
		boolean status = checkTestAverages(testAverages);
		assertTrue(status);
		assertTrue(TEST_SCALE_FACTOR*testAverages[0]<testAverages[NUM_TESTS-1]);
		System.out.println("Test_" + (NUM_TESTS - 1) + "/Test_0 averages = "
                + testAverages[NUM_TESTS -1]/testAverages[0]);
		
	}

	@Test
	@Order(4)
	void test_FileWriterBM() {
		Assumptions.assumeTrue(validBranch);
		String fwbm = "output/FileWriterBenchmark.csv";
		double[] testAverages = new double[NUM_TESTS];
		calculateTestAverages(new File(fwbm),testAverages);
		System.out.println("Checking data from "+fwbm);
		boolean status = checkTestAverages(testAverages);
		assertTrue(status);
		assertTrue(TEST_SCALE_FACTOR*testAverages[0]<testAverages[NUM_TESTS-1]);
		System.out.println("Test_" + (NUM_TESTS - 1) + "/Test_0 averages = "
                + testAverages[NUM_TESTS -1]/testAverages[0]);
		
	}

	@Test
	@Order(5)
	void test_BufferedWriterBM() {
		Assumptions.assumeTrue(validBranch);
		String bwbm = "output/BufferedWriterBenchmark.csv";
		double[] testAverages = new double[NUM_TESTS];
		calculateTestAverages(new File(bwbm),testAverages);
		System.out.println("Checking data from "+bwbm);
		boolean status = checkTestAverages(testAverages);
		assertTrue(status);
		assertTrue(TEST_SCALE_FACTOR*testAverages[0]<testAverages[NUM_TESTS-1]);
		System.out.println("Test_" + (NUM_TESTS - 1) + "/Test_0 averages = "
                + testAverages[NUM_TESTS -1]/testAverages[0]);
		
	}
	
	@Test
	@Order(1)
	void test_dataFiles() {
		Assumptions.assumeTrue(validBranch);
		for (int i=0; i < 5; i++ ) {
			String rdStr = "";
			File fd = new File("data/test_"+i);
			try {
				FileReader fr = new FileReader(fd);
				assertTrue(fr != null);
				for (int j = 0; j < 10; j++) {

					rdStr += (char) fr.read();
				}
				assertTrue(first10[i].equals(rdStr));
				System.out.println(" OKAY");
				fr.close();
			}
			catch (IOException e) {
				System.out.println("Caught an IOException: ");
				e.printStackTrace();
			}
		}
	}


}
