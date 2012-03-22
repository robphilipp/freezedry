package org.freezedry.persistence.keyvalue.renderers.decorators;

import org.freezedry.persistence.copyable.Copyable;

public interface Decorator extends Copyable< Decorator > {

	String decorate( final Object object );
}
