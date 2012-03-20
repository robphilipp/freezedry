package org.freezedry.persistence.renderers;

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
	 * @return
	 */
	Pair< String, Object > createKeyValuePair( final InfoNode infoNode, final String key );
}
