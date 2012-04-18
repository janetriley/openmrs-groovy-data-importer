	package org.openmrs.groovyimporter;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Ignore;
import org.openmrs.api.context.*;
import org.openmrs.groovyimporter.OpenmrsSession;
import org.openmrs.*;
import org.openmrs.util.*;

/**
 * @author me
 *
 */
public class OpenmrsSessionTest {

    /**
     * Test method for {@link openmrs.tools.importer.OpenmrsSession#initProps()}.
     */
    @Test
    public void testInitProps() {
	OpenmrsSession sess = new OpenmrsSession();
	assertNull( sess.getProps());
	sess.initProps();
	assertNotNull(sess.getProps());
    	//need to debug? here are the props
	//sess.props.each{ it -> println it;}
    }

    @Test
    public void whereAreTheProps(){
	println OpenmrsUtil.getApplicationDataDirectory();
	assertNotNull(OpenmrsUtil.getApplicationDataDirectory());
    }

    /**
     * Test method for {@link openmrs.tools.importer.OpenmrsSession#startOpenmrs()}.
     * {@link openmrs.tools.importer.OpenmrsSession#openOpenmrsSession()}
     * {@link openmrs.tools.importer.OpenmrsSession#closeOpenmrs()}
     */
    //this makes Maven blow out memory heap
    @Ignore
    @Test
    public void testStartOpenmrs() {
	//	fail("Not yet implemented");
	//  Context.updateDatabase();
	OpenmrsSession sess = new OpenmrsSession();

	//		try {
	sess.startOpenmrs();
	//		} catch( org.openmrs.module.OpenmrsCoreModuleException e){
	//			System.out.println(" Got a startup error - no biggie " + e);
	//		}
	/*
	 sess.openOpenmrsSession();
	 assertTrue("openmrs session opened",Context.isSessionOpen());
	 assertNotNull("start created user context (test before downstream hissy fits)",Context.getUserContext());
	 assertTrue("start logged user in",Context.isAuthenticated());
	 */		sess.closeOpenmrsSession();
	assertFalse("close worked",Context.isSessionOpen());

    }


}
