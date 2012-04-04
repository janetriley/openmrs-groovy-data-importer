package org.openmrs.tools.importer.source;

public interface ImportSource {
	def next();
	boolean hasNext();
	int getCurrentLineNum();
	def currentLineToString();
}
