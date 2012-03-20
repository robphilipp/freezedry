package org.freezedry.persistence.writers.keyvalue.renderers;

import org.freezedry.persistence.utils.Require;
import org.freezedry.persistence.writers.PersistenceWriter;
import org.freezedry.persistence.writers.keyvalue.KeyValueWriter;

public abstract class AbstractPersistenceRenderer implements PersistenceRenderer {

	private final KeyValueWriter writer;
	
	/**
	 * Constructor for the {@link PersistenceRenderer}s that stores the associated 
	 * {@link PersistenceWriter} needed for resursion.
	 * @param writer The associated {@link PersistenceWriter}
	 */
	public AbstractPersistenceRenderer( final KeyValueWriter writer )
	{
		Require.notNull( writer );
		this.writer = writer;
	}
	
	/**
	 * Copy constructor
	 * @param renderer
	 */
	public AbstractPersistenceRenderer( final AbstractPersistenceRenderer renderer )
	{
		this.writer = renderer.writer;
	}
	
	/**
	 * @return The persistence writer associated with this renderer for use in recursion.
	 */
	protected KeyValueWriter getPeristPersistenceWriter()
	{
		return writer;
	}
}
