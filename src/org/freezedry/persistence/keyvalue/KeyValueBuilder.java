package org.freezedry.persistence.keyvalue;

import java.util.List;

import org.freezedry.persistence.containers.Pair;
import org.freezedry.persistence.tree.InfoNode;

public interface KeyValueBuilder {

	List< Pair< String, Object > > buildKeyValuePairs( final InfoNode rootInfoNode );
	
	String getSeparator();
}
