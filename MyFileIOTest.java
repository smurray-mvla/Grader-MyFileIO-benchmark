package myfileio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

// TODO: Auto-generated Javadoc
/**
 * The Class MyFileIOTest.
 */
@TestMethodOrder(OrderAnnotation.class)
class MyFileIOTest {
	
	/** The fio. */
	MyFileIO fio = new MyFileIO();
	
	/** The fr. */
	FileReader fr;
	
	/** The fw. */
	FileWriter fw;
	
	/** The br. */
	BufferedReader br;
	
	/** The bw. */
	BufferedWriter bw;
	
	/** The first 10. */
	String[] first10 = {"mupnbhpirr","vsjjgiavuz","ubmugngmiw","mjhktqcwgj","wjkzgzccak"};
	
	/** The fd. */
	private static File fd;

	static boolean validBranch = true;
    static Set<String> branches = new HashSet<String>(Arrays.asList("main","myfileio","regex","benchmark"));
    
    
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
	 * Sets the up before class. This will delete any test files created
	 * by failed runs of this JUnit test before starting so that the
	 * expected conditions of the File objects are correct
	 *
	 * @throws Exception the exception
	 */
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		System.out.println("Running on: "+getOperatingSystem());
		String branch = getActiveBranch();
		validBranch = branches.contains(branch);

		for (int i = 0; i <5; i++) {
			fd = new File("output/.testcase"+i);
			if (fd.exists()) { 
			    if (!fd.delete()) throw (new Exception("Failed to delete file "+fd.getName()));
			}
			fd = new File("output/.test_"+i);
			if (fd.exists()) { 
			    if (!fd.delete()) throw (new Exception("Failed to delete file "+fd.getName()));
			}
		}
	}

	/**
	 * Test getFileHandle:
	 *      1) Tests with empty name
	 *      2) Tests with valid name (.readme file in output/ directory 
	 *         so don't delete that!!!)
	 */
	@Test
	@Order(1)
	void testGetFileHandle() {
		Assumptions.assumeTrue(validBranch);
		System.out.println("\n\nTesting getFileHandle:");
		fd = fio.getFileHandle(null);
		System.out.print("   Testing File Handle with null string:");
		assertTrue (fd == null);
		System.out.println(" PASSED");
		fd = fio.getFileHandle("");
		System.out.print("   Testing File Handle with empty name:");
		assertTrue("".equals(fd.getName()));
		assertTrue(fd.exists() == false);
		System.out.println(" PASSED");
		System.out.print("  Testing Filehandle on valid file: ");
		fd = fio.getFileHandle("output/.readme");
		assertTrue(".readme".equals(fd.getName()));
		assertTrue(fd.exists());
		assertTrue(fd.length() >= 63);
		assertTrue(fd.canRead());
		assertTrue(fd.canWrite());
		assertTrue(fd.isFile());
		if (File.separatorChar == '\\') {
			assertTrue("output\\.readme".equals(fd.getPath()));
			
		} else {
			assertTrue("output/.readme".equals(fd.getPath()));
		}
		System.out.println(" PASSED");
	}

	/**
	 * Test createEmptyFile. Creates multiple files in the output/ 
	 * directory, checking the File information for each BEFORE and
	 * AFTER creation. Files are left present in the directory to
	 * support deleteFile testing.
	 */
	@Test
	@Order(2)
	void testCreateEmptyFile() {
		Assumptions.assumeTrue(validBranch);
		String fname;
		File fd;
		System.out.println("\n\nTesting createEmptyFile:");
		for (int i = 0; i < 5 ; i++) {
			fname = "output/.testcase"+i;
			System.out.print("   Attempting to create empty file "+fname);
			fd = fio.getFileHandle(fname);
			assertFalse(fd.exists());
			assertTrue(fd.length() == 0);
			assertTrue(fd.canRead() == false);
			assertTrue(fd.canWrite() == false);
			assertTrue(fd.isFile() == false);
			assertTrue(fio.createEmptyFile(fname));
			assertTrue(fd.exists() == true);
			assertTrue(fd.length() == 0);
			assertTrue(fd.canRead() == true);
			assertTrue(fd.canWrite() == true);
			assertTrue(fd.isFile() == true);
			assertFalse(fio.createEmptyFile(fname));
			System.out.println(" PASSED");
		}			
	} 
	
	/**
	 * Test delete File. Checks for existence of the files created by
	 * the createEmptyFile test above. If they do not exist, then directly
	 * create them. Opens a File object and checks validity of data BEFORE
	 * and AFTER deletion, confirming that the file no longer exists.
	 */
	@Test
	@Order(3)
	void testDeleteFile() {
		Assumptions.assumeTrue(validBranch);
		String fname;
		File fd;
		System.out.println("\n\nTesting deleteFile:");
		for (int i = 0; i < 5 ; i++) {
			fname = "output/.testcase"+i;
			fd = fio.getFileHandle(fname);
			if (!fd.exists()) {
				assertTrue(fio.createEmptyFile(fname));
			}
			System.out.print("   Attempting to delete file "+fname);
			assertTrue(fd.exists());
			assertTrue(fd.length() == 0);
			assertTrue(fd.canRead() == true);
			assertTrue(fd.canWrite() == true);
			assertTrue(fd.isFile() == true);
			assertTrue(fio.deleteFile(fname));
			assertTrue(fd.exists() == false);
			assertTrue(fd.length() == 0);
			assertTrue(fd.canRead() == false);
			assertTrue(fd.canWrite() == false);
			assertTrue(fd.isFile() == false);
			assertFalse(fio.deleteFile(fname));
			System.out.println(" PASSED");
		}
		/* need to check directories will not be deleted by deleteFile() */
		fname = "output/test_dir";
		fd = fio.getFileHandle(fname);
		if (fd.exists()) {
			assertTrue(fd.delete());   
		}
		assertTrue(fd.mkdir());
		String tfname = fname + "/testFile";
		File tf = fio.getFileHandle(tfname);
		assertTrue(fio.createEmptyFile(tfname));
		assertFalse(fio.deleteFile(fname));
		assertFalse(fd.delete());
		assertTrue(fio.deleteFile(tfname));
		assertFalse(fio.deleteFile(fname));
		assertTrue(fd.delete());
	} 
	
	/**
	 * Test checkFileStatus. Test the checkFileStatus produces all possible
	 * status conditions (note that on Windows, it is impossible to test
	 * correct operation of access violations, as Java on Windows does not
	 * support disabling Read/Write access). 
	 */
	@Test
	@Order(4)
	void testCheckFileStatus()  {
		Assumptions.assumeTrue(validBranch);
		System.out.println("\n\nTesting checkFileStatus");

		File fd;
		fd = fio.getFileHandle("");
		System.out.print("   Testing empty file name: ");
		assertEquals(FileStatus.EMPTY_NAME,fio.checkFileStatus(fd, true));
		assertEquals(FileStatus.EMPTY_NAME,fio.checkFileStatus(fd, false));
		System.out.print(" PASSED\n   Testing detection of NOT_A_FILE: ");
		fd = fio.getFileHandle("output");
		assertEquals(FileStatus.NOT_A_FILE,fio.checkFileStatus(fd, true));
		assertEquals(FileStatus.NOT_A_FILE,fio.checkFileStatus(fd, false));
		System.out.print(" PASSED\n   Testing non-existent file (FILE_DOES_NOT_EXIST or FILE_OK): ");
		fd = fio.getFileHandle("output/.testcase1");
		assertEquals(FileStatus.FILE_DOES_NOT_EXIST,fio.checkFileStatus(fd,true));
		assertEquals(FileStatus.FILE_OK,fio.checkFileStatus(fd,false));
		System.out.print(" PASSED\n   Testing detection of Zero Length File ");
		fio.createEmptyFile("output/.testcase1");
		fd = fio.getFileHandle("output/.testcase1");
		assertEquals(FileStatus.ZERO_LENGTH_FILE,fio.checkFileStatus(fd,true));
		assertEquals(FileStatus.WRITE_EXISTS,fio.checkFileStatus(fd,false));
		assertTrue(fio.deleteFile("output/.testcase1"));
		System.out.print(" PASSED\n   Testing detection of Access Issues: ");
		fd = fio.getFileHandle("output/.readme");
		if (fd.setReadable(false)) { 
			assertEquals(FileStatus.NO_READ_ACCESS,fio.checkFileStatus(fd,true));
			fd.setReadable(true);
		}
		if (fd.setWritable(false)) { 
			assertEquals(FileStatus.NO_WRITE_ACCESS,fio.checkFileStatus(fd,false));
			fd.setWritable(true);
		}
		System.out.print(" PASSED\n   Testing FILE_OK for read,WRITE_EXIST for write: ");
		assertEquals(FileStatus.FILE_OK,fio.checkFileStatus(fd,true));
		assertEquals(FileStatus.WRITE_EXISTS,fio.checkFileStatus(fd,false));
		System.out.println(" PASSED");
	}
	
	/**
	 * Test openFileReader and closeFile. 
	 * 1) Verifies that attempting to open FileReader on an non-existent file 
	 *    returns NULL. 
	 * 2) Verifies that the FileReader is opened on each of the data/test_* 
	 *    files, and that the contents of those files are correct (by looking 
	 *    at the first 10 characters of each file). 
	 * 3) Closes each file and then verifies closure by attempting a read() and
	 *    expecting an IOException to be thrown..
	 */
	@Test
	@Order(5)
	void testOpenCloseFileReader() {
		Assumptions.assumeTrue(validBranch);
		// this should not exist
		System.out.println("\n\nTesting openFileReader:");
		String fname = "output/.testcase1";
		System.out.print("   Testing non-existent file: ");
		File fd = new File(fname);
		fr = fio.openFileReader(fd);
		assertNull(fr);
		System.out.println(" PASSED");
		int size = 100;
		System.out.println("   Checking the integrity of the data/test_* files");
		for (int i=0; i < 5; i++ ) {
			String rdStr = "";
			fd = fio.getFileHandle("data/test_"+i);
			System.out.print("      File data/test_"+i+" is ");
			assertTrue(fd.exists());
			assertTrue(fd.length() >= (size));
			assertTrue(fd.length() <= (size+10));
			fr = fio.openFileReader(fd);
			assertTrue(fr != null);
			for (int j = 0; j < 10; j++) {
				try {
					rdStr += (char) fr.read();
				}
				catch (IOException e) {
					System.out.println("Caught an IOException: ");
					e.printStackTrace();
				}
			}
			assertTrue(first10[i].equals(rdStr));
			System.out.println(" OKAY");
			size *=10;
			System.out.print("   Testing closeFile for FileReader: ");
			fio.closeFile(fr);
			assertThrows(IOException.class,()->fr.read());
			System.out.println(" PASSED");
		}
		
	} 
	
	/**
	 * Test openBufferedReader and closeFile. 
	 * 1) Verifies that attempting to open BufferedReader on an non-existent file 
	 *    returns NULL. 
	 * 2) Verifies that the BufferedReader is opened on each of the data/test_* 
	 *    files, and that the contents of those files are correct (by looking 
	 *    at the first 10 characters of each file). 
	 * 3) Closes each file and then verifies closure by attempting a read() and
	 *    expecting an IOException to be thrown..
	 */
	@Test
	@Order(6)
	void testOpenCloseBufferedReader() {
		Assumptions.assumeTrue(validBranch);
		// this should not exist
		System.out.println("\n\nTesting openBufferedReader:");
		String fname = "output/.testcase1";
		System.out.print("   Testing non-existent file: ");
		File fd = new File(fname);
		br = fio.openBufferedReader(fd);
		assertNull(br);
		System.out.println(" PASSED");
		int size = 100;
		System.out.println("   Checking the integrity of the data/test_* files");
		for (int i=0; i < 5; i++ ) {
			String rdStr = "";
			fd = fio.getFileHandle("data/test_"+i);
			System.out.print("      File data/test_"+i+" is ");
			assertTrue(fd.exists());
			assertTrue(fd.length() >= (size));
			assertTrue(fd.length() <= (size+10));
			br = fio.openBufferedReader(fd);
			assertTrue(br != null);
			for (int j = 0; j < 10; j++) {
				try {
					rdStr += (char) br.read();
				}
				catch (IOException e) {
					System.out.println("Caught an IOException: ");
					e.printStackTrace();
				}
			}
			assertTrue(first10[i].equals(rdStr));			
			System.out.println(" OKAY");
			System.out.print("   Testing closeFile for BufferedReader: ");
			fio.closeFile(br);
			assertThrows(IOException.class,()->br.read());
			System.out.println(" PASSED");
			size *=10;
		}
	} 
	
	/**
	 * Test openFileWriter and closeFile.
	 * 1) Opens a FileReader for each of the data/test_* and a FileWriter 
	 *    for a file  in the output/ directory (output/.test_*).
	 * 2) Copies, 1 char at a time, the data/test_* file to the output/.test_*.
	 * 3) Closes each file and then verifies closure by attempting a read() and
	 *    a write on the respective FileReader and FileWriter objects, expecting
	 *    an IOException to be thrown..
	 * 4) Verifies the integrity of the write by checking the existence, size and
	 *    contents (first 10 characters) of the created file then deletes the file.
	 */
	@Test
	@Order(7)
	void testOpenCloseFileWriter() {
		Assumptions.assumeTrue(validBranch);
		// this should not exist
		File fwd;
		File frd;
		int size = 100;
		System.out.println("\n\nTesting openFileWriter:");
		System.out.println("   Copying data/test_* files to output");
		for (int i=0; i < 3; i++) {
			frd = fio.getFileHandle("data/test_"+i);
			fwd = fio.getFileHandle("output/.test_"+i);
			System.out.print("      Copying data/test_"+i+" to output/.test_"+i);
			assertTrue(frd.exists());
			assertFalse(fwd.exists());
			fr = fio.openFileReader(frd);
			fw = fio.openFileWriter(fwd);
			try {
				for (int j=0; j < frd.length(); j++) {

					char aChar = (char) fr.read();
					fw.write(aChar);
				}
			} catch (IOException e) {
				System.out.println("IO Exception occured");
				e.printStackTrace();
			}
			System.out.println(" PASSED");
			System.out.print("   Testing closeFile for FileReader and FileWriter: ");
			fio.closeFile(fr);
			fio.closeFile(fw);
			assertThrows(IOException.class,()->fr.read());
			assertThrows(IOException.class,()->fw.write('F'));
			System.out.println(" PASSED");
		}
		System.out.println("   Checking integrity of copied files:");
		for (int i=0; i < 3; i++ ) {
			String rdStr = "";
			System.out.print("      File output/.test_"+i+" is ");
			fd = fio.getFileHandle("output/.test_"+i);
			assertTrue(fd.exists());
			assertTrue(fd.length() >= (size));
			assertTrue(fd.length() <= (size+10));
			FileReader fr = fio.openFileReader(fd);
			assertTrue(fr != null);
			for (int j = 0; j < 10; j++) {
				try {
					rdStr += (char) fr.read();
				}
				catch (IOException e) {
					System.out.println("Caught an IOException: ");
					e.printStackTrace();
				}
			}
			assertTrue(first10[i].equals(rdStr));
			fio.closeFile(fr);
			fd.delete();
			assertFalse(fd.exists());
			size *=10;
			System.out.println(" OKAY");
		}
	} 
	
	/**
	 * 1) Opens a BufferedReader for each of the data/test_* and a BufferedWriter 
	 *    for a file  in the output/ directory (output/.test_*).
	 * 2) Copies, 1 char at a time, the data/test_* file to the output/.test_*.
	 * 3) Closes each file and then verifies closure by attempting a read() and
	 *    a write on the respective BufferedReader and BufferedWriter objects, expecting
	 *    an IOException to be thrown..
	 * 4) Verifies the integrity of the write by checking the existence, size and
	 *    contents (first 10 characters) of the created file then deletes the file.
	 */
	@Test
	@Order(8)
	void testOpenCloseBufferedWriter() {
		Assumptions.assumeTrue(validBranch);
		// this should not exist
		File bwd;
		File brd;
		int size = 100;
		System.out.println("\n\nTesting BufferedFileWriter:");
		System.out.println("   Copying data/test_* files to output");
		for (int i=0; i < 3; i++) {
			brd = fio.getFileHandle("data/test_"+i);
			bwd = fio.getFileHandle("output/.test_"+i);
			System.out.print("      Copying data/test_"+i+" to output/.test_"+i);
			assertTrue(brd.exists());
			assertFalse(bwd.exists());
			br = fio.openBufferedReader(brd);
			bw = fio.openBufferedWriter(bwd);
			try {
				for (int j=0; j < brd.length(); j++) {
					char aChar = (char) br.read();
					bw.write(aChar);
				}	
			} catch (IOException e) {
				System.out.println("IO Exception occured");
				e.printStackTrace();
			}
			fio.closeFile(br);
			fio.closeFile(bw);
			System.out.println(" PASSED");
			System.out.print("   Testing closeFile for BufferedReader and BufferedWriter: ");
			assertThrows(IOException.class,()->br.read());
			assertThrows(IOException.class,()->bw.write('F'));
			System.out.println(" PASSED");
		}
		System.out.println("   Checking integrity of copied files:");
		for (int i=0; i < 3; i++ ) {
			String rdStr = "";
			System.out.print("      File output/.test_"+i+" is ");
			fd = fio.getFileHandle("output/.test_"+i);
			assertTrue(fd.exists());
			assertTrue(fd.length() >= (size));
			assertTrue(fd.length() <= (size+10));
			BufferedReader br = fio.openBufferedReader(fd);
			assertTrue(br != null);
			for (int j = 0; j < 10; j++) {
				try {
					rdStr += (char) br.read();
				}
				catch (IOException e) {
					System.out.println("Caught an IOException: ");
					e.printStackTrace();
				}
			}
			assertTrue(first10[i].equals(rdStr));
			fio.closeFile(br);
			fd.delete();
			assertFalse(fd.exists());
			size *=10;
			System.out.println(" OKAY");
		}
	} 
	
}
