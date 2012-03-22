package org.freezedry.persistence.writers.keyvalue.renderers.decorators;

import org.freezedry.persistence.copyable.Copyable;

public interface Decorator extends Copyable< Decorator > {

	String decorate( final Object object );
}
