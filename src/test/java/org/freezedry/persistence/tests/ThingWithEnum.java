package org.freezedry.persistence.tests;


import org.freezedry.persistence.annotations.PersistEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rob on 3/21/14.
 */
public class ThingWithEnum
{
	private List< String > evilDoings = new ArrayList<>();
	@PersistEnum( nameMethod = "getName" )
	private Things thing = Things.THING_THREE;
	private Things otherThing = Things.THING_ONE;
}
