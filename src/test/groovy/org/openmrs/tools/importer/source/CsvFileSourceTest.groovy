package org.openmrs.tools.importer.source;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.apache.commons.lang.StringUtils;
import au.com.bytecode.opencsv.*;

class CsvFileSourceTest {
    def mySource;
    def filepath="/Volumes/ETUI/prod_exports/sept_exports/patient_sample.txt";

    @Before
    public void setUp() throws Exception {
	mySource = new CsvFileSource(filepath);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testCsvFileDataSource() {
	assertNotNull(mySource);
	assertNotNull(mySource.strategy);
	assertNotNull(mySource.file);
	assertNotNull(mySource.reader);
	assertEquals("starting on the first readable line",1,mySource.currentLineNum);
	assertNotNull("First line is queued up",mySource.next());
    }

    @Test
    public void testEchoLine() {
	mySource.next();
	assertNotNull(mySource.echoCurrentLine());
	println mySource.echoCurrentLine();
    }


    @Test
    public void testCheckMappings() {
	assertNotNull(mySource.strategy.header);
    }

    /**
     *
     * Check that mySource iterates over all of the lines, nothing left out at the beginning or end
     *
     */

    @Test
    public void hasNext_shouldIterate(){
	assertEquals(1, mySource.currentLineNum); //constructor consumes header line
	def wrappedline =mySource.currentLine;

	BufferedReader reader =
		new BufferedReader(new FileReader(filepath));
	def rawline = reader.readLine();//consume header
	def linecount=1;
	while( (rawline = reader.readLine()) != null ){
	    linecount++;
	    wrappedline =  mySource.next();
	    assertTrue(mySource.hasNext());
	    assertEquals(linecount, mySource.currentLineNum);
	    //grab the first field, leaving out the ""s
	    def splitzies = rawline.split(',');
	    assertNotNull(wrappedline);
	    assertEquals(StringUtils.replaceChars(splitzies[0],'"',""), wrappedline[0]);
	}
	assertNull("Make sure mySource has read everything - really really ", mySource.next());
	assertFalse(mySource.hasNext());
    }

    @Test
    public void writeToCsv(){
	String input = "first,second,third";
	String csvInput = '"first","second","third"' + "\n";
	String[] entries = StringUtils.stripAll(StringUtils.split(input, ','));
	String output = mySource.writeAsCsv(entries);
	assertEquals(csvInput, output);

    }



    @Test
    public void badDateFormats(){
	String[] entry = [ "baddate":"this is an unparseable date"];
	def parsedDate = mySource.parseDate("baddate", entry);
	assertNull(parsedDate);
	//implied test: assert no unhandled exception

    }

    @Test
    public void initBufferedStringReader(){
	String sample = '"RegtID","Reg_PatientID","Reg_DateOfVisit","Registered","FeePaid",' +
		'"GeneralImpression","Reg_DiseaseID","Reg_DiseaseID2","Reg_DiseaseID3",' +
		'"ChronicUrlDxID","DidNotWait","Return","FollowUp","Referal","Referred From",' +
		'"DateCreated","Wt/Ht","Admitted To","CaseDx1","CaseDx2","CaseDx3"' + "\n" +
		'"1945855662,"2003-016045",7/7/2007 0:00:00,1,0,,"R19.1",,,,0,0,0,0,,7/7/2007 0:00:00,,,,,"';
	def reader = new StringReader(sample);
	assertNotNull(reader);


    }
    @Test
    public void handlesCommentsAndBlanks(){

	def mySource = new CsvFileSource(new StringReader(
		'"RegtID","Reg_PatientID","Reg_DateOfVisit","Registered","FeePaid",' +
		'"GeneralImpression","Reg_DiseaseID","Reg_DiseaseID2","Reg_DiseaseID3",' +
		'"ChronicUrlDxID","DidNotWait","Return","FollowUp","Referal","Referred From",' +
		'"DateCreated","Wt/Ht","Admitted To","CaseDx1","CaseDx2","CaseDx3"' + "\n" +
		"#This is a comment\n" +
		"\n" +  //blank
		'"1945855662,"2003-016045",7/7/2007 0:00:00,1,0,,"R19.1",,,,0,0,0,0,,7/7/2007 0:00:00,,,,,"'
		));

	mySource.next();
	def line = mySource.echoCurrentLine();
	assertNotNull(line);
	println line;
	//cut and paste form println - make sure it matches above
	assertTrue(line.startsWith("line 4, 1945855662",0));

    }


}
