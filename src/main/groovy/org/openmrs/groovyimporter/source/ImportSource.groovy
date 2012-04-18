package org.openmrs.groovyimporter.source;

public interface ImportSource {
	def next();
	boolean hasNext();
	int getCurrentLineNum();
	def currentLineToString();
}
