package org.openmrs.groovyimporter.source;

import java.io.StringWriter;
import au.com.bytecode.opencsv.*;
import au.com.bytecode.opencsv.bean.*;
import org.apache.log4j.Logger;
import org.openmrs.*;
import org.apache.commons.lang.StringUtils;
/**
 *
 * wraps a data file we're reading from
 *
 * @author me
 *
 */

class CsvFileSource implements ImportSource{
    static Logger log = Logger.getLogger("openmrs.tools.importer");

    String filepath = null;
    def file = null;
    def reader = null;

    au.com.bytecode.opencsv.bean.HeaderColumnNameTranslateMappingStrategy strategy = null;
    def headerList = [:]; //map column text name to index
    def currentLine = null;
    int  currentLineNum = 1;//header line is consumed on construction
    static 	String dateFormat = "M/dd/yyyy h:mm:ss"; //how access exported them

    CSVWriter returnToCsv = null;
    StringWriter returnToCsvWriter = new StringWriter();// CSVWriter hides its
    // buffer


    CsvFileSource(){
	init(null);
    }

    CsvFileSource(String filepath){
	init(filepath);

    }

    CsvFileSource(java.io.Reader newReader){
	//use for testing - hand it a StringReader with header and test data
	initReader(newReader);

    }

    def currentLineToString(){
	return writeAsCsv(currentLine);

    }

    int getCurrentLineNum(){ return currentLineNum;}

    def init( String filepath){
	if( filepath == null ){
	    return;
	}
	file = new File(filepath);
	if( file != null  )
	    return initReader (file.newReader("UTF-8"));
    }

    private def initReader( Reader newReader){

	reader =  new au.com.bytecode.opencsv.CSVReader(newReader);
	//reader =  new au.com.bytecode.opencsv.CSVReader(file.newReader("UTF-8"),
	//	CSVParser.DEFAULT_SEPARATOR, CSVParser.DEFAULT_QUOTE_CHARACTER,
	//	(char)'!'); //set escape chars to ! - nothing uses that

	strategy = new au.com.bytecode.opencsv.bean.HeaderColumnNameTranslateMappingStrategy();
	strategy.captureHeader(getReader()); //consumes the first line of input
	currentLineNum = 1;

	returnToCsv = new au.com.bytecode.opencsv.CSVWriter(returnToCsvWriter);
	//call next() to move to the first line of input after the header

	//peel off names with indexes
	strategy.header.eachWithIndex { value, index->
	    headerList[value] =  index;
	}
    }

    boolean hasNext(){
	return reader.@hasNext;

    }
    /**
     * @return String[] on success, null otherwise
     * @should return String[] of line if text
     * @should return false if no next line
     * @should increment currentLineNum
     */
    def next(){
	def line = null;
	while( reader.@hasNext  ) {
	    currentLine = StringUtils.stripAll(reader.readNext());
	    currentLineNum++;
	    if( currentLine != null &&//there was a result
		! StringUtils.isBlank(currentLine[0]) &&
		currentLine[0].charAt(0) != '#') //it wasn't a comment
		return currentLine;
	}
	return null; //got to the end
    }

    void finalize(){
	if( reader != null ){
	    reader.close();
	    reader = null;
	}

	if( file != null ){
	    file.close();
	    file = null;
	}
    }

    def readValue(String key, String[] values = this.currentLine){
	if( values == null )
	    return null;

	def index = headerList[key];
	if( index == null)
	    return null;
	if( index >= values.size()){
	    log.error("ERROR: tried to look up key " + key + " with index " +
		    index + " but this line only goes up to index " +
		    (values.size() - 1) + ". Line is " + writeAsCsv(values));

	    return null;
	}
	return values[index];
    }

    def parseDate(String key, String[] values = this.currentLine){

	String dateValue = readValue(key, values);
	if( StringUtils.isBlank(dateValue))
	    return null;
	Date myDate = null;
	try {
	    java.text.SimpleDateFormat dater = new java.text.SimpleDateFormat(dateFormat);
	    myDate = dater.parse(dateValue);
	} catch ( java.text.ParseException e){
	    log.error("Could not parse date:  date value was " + dateValue +
		    " exception was " + e.getMessage());
	}
	return myDate;
    }

    def parseBoolean(String key, String[] values = this.currentLine){
	String value = readValue(key, values);
	if( StringUtils.isBlank(value) ||
	    value == "0" || value == 0 )
	    return false;
	if( value == "1" || value == 1 )
	    return true;
	return Boolean.parseBoolean(value);

    }

    def parseInteger(String key, String[] values = this.currentLine){

	String value = readValue(key, values);
	if( StringUtils.isBlank(value))
	return null;
	try {
	    return Integer.parseInt(value);

	} catch( NumberFormatException e){
	    log.error("Could not parse ${key} value ${value} as an integer, line ${currentLineNum}. "+
		    " Error was " + e.getMessage()+ "Line was:");
	    log.error(writeAsCsv());
	    return null;
	}
    }

    def echoCurrentLine(){
	return "line ${currentLineNum}, " + currentLine.join(",");

    }

    String writeAsCsv(String[] entry = currentLine) {
	//PrintWriter returnToCsvWriter = returnToCsv.@pw;
	// clear any old messages
	returnToCsvWriter.getBuffer().setLength(0);
	returnToCsv.writeNext(entry);
	try {
	    returnToCsv.flush();
	} catch (java.io.IOException e) {
	    log.error("CSV Writer failed to write to a StringBuffer ?!? Line was \n"
		    + writeAsCsv(entry));
	}

	return returnToCsvWriter.toString();
    }
}

