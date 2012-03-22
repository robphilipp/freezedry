package org.freezedry.persistence.keyvalue.renderers;

import java.util.List;

import org.freezedry.persistence.containers.Pair;
import org.freezedry.persistence.copyable.Copyable;
import org.freezedry.persistence.tree.InfoNode;

/**
 * 
 * @author rob
 */
public interface PersistenceRenderer extends Copyable< PersistenceRenderer >{

	/**
	 * 
	 * @param infoNode
	 * @param key
	 * @param keyValues
	 * @param isWithholdPersistName
	 */
	void buildKeyValuePair( final InfoNode infoNode, 
							final String key, 
							final List< Pair< String, Object > > keyValues,
							final boolean isWithholdPersistName );
}
