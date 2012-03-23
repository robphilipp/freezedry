package org.freezedry.persistence.keyvalue;

import java.util.List;

import org.freezedry.persistence.containers.Pair;
import org.freezedry.persistence.tree.InfoNode;

public interface KeyValueBuilder {

	List< Pair< String, Object > > buildKeyValuePairs( final InfoNode rootInfoNode );
	void buildKeyValuePairs( final InfoNode infoNode, final String key, final List< Pair< String, Object > > keyValues );
	void createKeyValuePairs( final InfoNode infoNode, final String key, final List< Pair< String, Object > > keyValues, final boolean isWithholdPersitName );	
	String getSeparator();
}
