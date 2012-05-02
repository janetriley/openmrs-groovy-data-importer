package org.openmrs.groovyimporter.source;

public interface DataIterator {
	def next();
	boolean hasNext();
	int getCurrentLineNum();
	def currentLineToString();
}
