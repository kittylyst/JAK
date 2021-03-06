package net.dougqh.jak;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

public final class JavaAnnotationDescriptor {
	private final int flags;
	private final String name;
	
	JavaAnnotationDescriptor(
		final JavaModifiers flagsBuilder,
		final String name )
	{
		this.flags = flagsBuilder.flags();
		this.name = name;
	}
	
	public final TypeDescriptor typeDescriptor() {
		return new TypeDescriptor(
			this.flags,
			this.name,
			new TypeVariable<?>[] {},
			Object.class,
			new Type[] { Annotation.class } );
	}
}
