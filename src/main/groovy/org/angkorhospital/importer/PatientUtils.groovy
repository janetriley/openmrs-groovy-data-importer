package org.angkorhospital.importer

import org.openmrs.*;
import org.apache.commons.lang.StringUtils;

/**
 *
 * a utility class to copy changes from one Patient object to another
 * writeTo receives all the changes
 * readFrom is the patient we're updating from
 *
 * writeTo will be a Patient that's already saved --  all members have an ID, a uuid, a create time, etc. already
 * We want to save that.
 *
 * If readFrom has a new member, it will be added to writeTo.
 * If readFrom is missing a member, it will be removed from writeTo.
 *
 *
 *
 */
class PatientUtils {

	//TO DO: compare relationships

	static boolean updateAll(Patient writeTo, Patient readFrom){
		def changed = updateNames(writeTo, readFrom);
		changed = changed || updateAddresses(writeTo, readFrom);
		changed = changed || updateIdentifiers(writeTo, readFrom);
		changed = changed || updateAttributes(writeTo, readFrom);
		//TO DO : relationships
		return changed;
	}

	static boolean updateNames(Patient writeTo, Patient readFrom){

		def namesToRemove = aNotInB(writeTo.getNames(), readFrom.getNames(), compareContents);
		def namesToAdd = aNotInB(readFrom.getNames(), writeTo.getNames(),  compareContents);

		if( namesToRemove.isEmpty() && namesToAdd.isEmpty())
			return false ; // no work to do -- nothing changed
		namesToRemove.each(){ n ->
			writeTo.removeName(n);
		}

		namesToAdd.each(){ n ->
			writeTo.addName(n);
		}

		return true; //something changed
	}


	static boolean  updateAddresses(Patient writeTo, Patient readFrom){

		def addressesToRemove = aNotInB(writeTo.getAddresses(), readFrom.getAddresses(), compareContents);
		def addressesToAdd = aNotInB(readFrom.getAddresses(), writeTo.getAddresses(),  compareContents);

		if( addressesToRemove.isEmpty() && addressesToAdd.isEmpty())
			return false ; // no work to do -- nothing changed
		addressesToRemove.each(){ n ->
			writeTo.removeAddress(n);
		}

		addressesToAdd.each(){ n ->
			writeTo.addAddress(n);
		}

		return true; //something changed
	}


	static boolean updateAttributes(Patient writeTo, Patient readFrom){
		def attributesToRemove = aNotInB(writeTo.getAttributes(), readFrom.getAttributes(), compareContents);
		def attributesToAdd = aNotInB(readFrom.getAttributes(), writeTo.getAttributes(),  compareContents);

		if( attributesToRemove.isEmpty() && attributesToAdd.isEmpty())
			return false ; // no work to do -- nothing changed
		attributesToRemove.each(){ n ->
			writeTo.removeAttribute(n);
		}

		attributesToAdd.each(){ n ->
			writeTo.addAttribute(n);
		}

		return true; //something changed
	}

	static boolean updateIdentifiers(Patient writeTo, Patient readFrom){
		def identifiersToRemove = aNotInB(writeTo.getIdentifiers(), readFrom.getIdentifiers(), compareContents);
		def identifiersToAdd = aNotInB(readFrom.getIdentifiers(), writeTo.getIdentifiers(),  compareContents);

		if( identifiersToRemove.isEmpty() && identifiersToAdd.isEmpty())
			return false ; // no work to do -- nothing changed
		identifiersToRemove.each(){ n ->
			writeTo.removeIdentifier(n);
		}
		identifiersToAdd.each(){ n ->
			writeTo.addIdentifier(n);
		}
		return true; //something changed
	}



	static def compareNames = { PersonName n1, n2 ->
		if( (n1 == null && n2 == null) ||  //same
		n1 == n2 ) //object comparison
			return true; //same
		if( n1 == null || n2 == null )
			return false; // they aren't both null - so one doesn't match
		return ( n1.equalsContent(n2) && n2.equalsContent(n1));  //API says it will ignore nulls in the readFrom name, so check both directions
	}

	static def compareContents = {  n1, n2 ->
		if( (n1 == null && n2 == null) ||  //same
		n1 == n2 ) //object comparison
			return true; //same
		if( n1 == null || n2 == null )
			return false; // they aren't both null - so one doesn't match
		return ( n1.equalsContent(n2) && n2.equalsContent(n1));  //API says it will ignore nulls in the readFrom name, so check both directions
	}



	static Set aNotInB( Collection setA, Collection setB, Closure equality){
		HashSet results = new HashSet();
		for( def a : setA ){
			def found = setB.find(){ b ->
				return equality.call(a,b);
			}
			if( found == null )
				results.add(a);//a was not found in B
		}

		return results;
	}

	static Boolean isNewRelationship(Relationship newRelationship, List<Relationship> existingRelationships){
		if( existingRelationships.size() == 0 )
			return true;
		def sameType = existingRelationships.findAll(){ it-> it.relationshipType.id == newRelationship.relationshipType.id;}
		if( sameType == null || sameType.size() == 0)
			return true; //no such type
		def samePerson = sameType.findAll(){  it->
			//it.personB.personName.equalsContent(newRelationship.perosnB.personName)
			it.personA.personName?.equalsContent(newRelationship.personA.personName);
		}

		return ( samePerson == null || samePerson.size() == 0);
	}
}
