package net.dougqh.jak.disassembler;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.dougqh.jak.Jak;
import net.dougqh.jak.JavaField;
import net.dougqh.jak.JavaMethodDescriptor;
import net.dougqh.java.meta.types.JavaTypes;
import static net.dougqh.jak.jvm.ConstantPoolConstants.*;

/**
 * Implementation of ConstantPool used during reading of class files.
 */
final class ConstantPool {
	public static final class MethodTypeDescriptor {
		final List<Type> paramTypes;
		final Type returnType;
		
		MethodTypeDescriptor( final List<Type> paramTypes, final Type returnType ) {
			this.paramTypes = paramTypes;
			this.returnType = returnType;
		}
	}
	
	/*
	This class uses a rather intricate multi-level caching scheme to handle 
	decoding UTF-8 strings into objects.
	 
	When a UTF-8 is decoded indirectly through a singular or dual reference, 
	the decoded value is stored inside the reference object and is assumed 
	to be correct going forward.
	 
	Conversely, when a UTF-8 is decoded directly (not through a reference - 
	as is the case for fields and methods being declared), the decoded value
	is cached, but the decoder must be checked for each request.
	 
	The difference is that when coming through a reference, the type of the
	decoder can be assumed to be constant.  So by caching the value in the 
	reference directly, the interpretation is known to always be correct in 
	that context.
	 
	However, when decoding directly, the UTF-8 reference could be shared 
	by a string literal and the value being decoded.  While this is highly
	unlikely, ConstantPool checks for this case anyway to guarantee 
	correctness.
	*/
	
	private final Object[] constants;
	private final DecodeCacheEntry[] decodeCache;
	
	ConstantPool( final JvmInputStream in ) throws IOException {		
		int count = in.u2();
		this.constants = new Object[count];
		this.decodeCache = new DecodeCacheEntry[count];
		
		for ( int index = 1; index < count; ++index ) {
			byte tag = in.u1();
			
			switch ( tag ) {
				case CLASS: {
					this.constants[index] = readClassInfo(in);
					break;
				}
				
				case FIELD_REF: {
					this.constants[index] = readFieldRef(in);
					break;
				}
				
				case METHOD_REF: {
					this.constants[index] = readMethodRef(in);
					break;
				}
				
				case INTERFACE_METHOD_REF: {
					this.constants[index] = readInterfaceMethodRef(in);
					break;
				}
				
				case STRING: {
					this.constants[index] = readString(in);
					break;
				}
				
				case INTEGER: {
					this.constants[index] = readInteger(in);
					break;
				}
				
				case FLOAT: {
					this.constants[index] = readFloat(in);
					break;
				}
				
				case LONG: {
					this.constants[index] = readLong(in);
					++index;
					break;
				}
				
				case DOUBLE: {
					this.constants[index] = readDouble(in);
					++index;
					break;
				}
				
				case NAME_AND_TYPE: {
					this.constants[index] = readNameAndType(in);
					break;
				}
				
				case UTF8: {
					this.constants[index] = readUtf8(in);
					break;
				}
			}
		}
	}
	
	final List<String> getReferencedTypeNames(final int thisIndex) {
		// TODO: Could probably be smarter here.  Interfaces will have greater proportion 
		// of their constant pool occupied by related types.
		
		int initialCapacity = Math.min(32, this.constants.length >> 2);
		ArrayList<String> types = new ArrayList<String>(initialCapacity);
		
		for ( int i = 0; i < this.constants.length; ++i ) {
			// skip the class entry for the class being declared
			if ( thisIndex == i ) {
				continue;
			}
			
			Object constant = this.constants[i];
			
			if ( constant instanceof ClassReference ) {
				types.add(this.typeName(i));
			}
		}
		
		return Collections.unmodifiableList(types);
	}
	
	final List<Type> getReferencedTypes(final int thisIndex) {
		// TODO: Could probably be smarter here.  Interfaces will have greater proportion 
		// of their constant pool occupied by related types.
		
		int initialCapacity = Math.min(32, this.constants.length >> 2);
		ArrayList<Type> types = new ArrayList<Type>(initialCapacity);
		
		for ( int i = 0; i < this.constants.length; ++i ) {
			// skip the class entry for the class being declared
			if ( thisIndex == i ) {
				continue;
			}
			
			Object constant = this.constants[i];
			
			if ( constant instanceof ClassReference ) {
				types.add(this.type(i));
			}
		}
		
		return Collections.unmodifiableList(types);
	}
	
	public final String typeName( final int classIndex ) {
		int stringIndex = this.index(classIndex);
		
		return translateClass( this.utf8(stringIndex) );
	}
	
	public final Type type( final int classIndex ) {
		return this.decodeRef(classIndex, TypeDecoder.INSTANCE);
	}
	
	public final int intValue( final int index ) {
		return (Integer)this.constants[index];
	}
	
	public final float floatValue( final int index ) {
		return (Float)this.constants[index];
	}
	
	public final long longValue( final int index ) {
		return (Long)this.constants[index];
	}
	
	public final double doubleValue( final int index ) {
		return (Double)this.constants[index];
	}
	
	public final String utf8( final int index ) {
		return (String)this.constants[index];
	}
	
	public final Type targetType( final int refIndex ) {
		return this.type( this.firstIndex(refIndex) );
	}
	
	public final JavaField field( final int refIndex ) {
		int nameAndTypeIndex = this.secondIndex(refIndex);
		
		String name = this.decodeFirstRef(nameAndTypeIndex, StringDecoder.INSTANCE);
		Type type = this.decodeSecondRef(nameAndTypeIndex, FieldDescriptorDecoder.INSTANCE);
		
		return Jak.field(type, name);
	}
	
	public final MethodTypeDescriptor methodTypeDescriptor( final int index ) {
		return this.decode(index, MethodSignatureDecoder.INSTANCE);
	}
	
	public final JavaMethodDescriptor methodRef( final int refIndex ) {
		int nameAndTypeIndex = this.secondIndex(refIndex);
		
		String name = this.decodeFirstRef(nameAndTypeIndex, StringDecoder.INSTANCE);
		MethodTypeDescriptor signature = this.decodeSecondRef(nameAndTypeIndex, MethodSignatureDecoder.INSTANCE);
				
		return Jak.method(signature.returnType, name, signature.paramTypes);
	}

	private final int index( final int index ) {
		return ( (SingularReference)this.constants[index] ).index;
	}
	
	private final <T> T decode( final int index, final Decoder<T> decoder ) {
		DecodeCacheEntry cacheEntry = this.decodeCache[index];
		if ( cacheEntry != null && cacheEntry.matches(decoder) ) {
			return cacheEntry.<T>get();
		}
		
		String string = this.utf8(index);
		T value = decoder.decode(string);
		this.decodeCache[index] = new DecodeCacheEntry(decoder, value);
		return value;
	}
	
	private final <T> T decodeRef( final int index, final Decoder<T> decoder ) {
		SingularReference ref = (SingularReference)this.constants[index];
		
		if ( ref.decoded == null ) {
			String utf8 = this.utf8(ref.index);
			ref.decoded = decoder.decode(utf8);
		}
		
		@SuppressWarnings("unchecked")
		T result = (T)ref.decoded;
		return (T)result;
	}
	
	private final int firstIndex( final int index ) {
		return ( (DualReference)this.constants[index] ).firstIndex;
	}
	
	private final <T> T decodeFirstRef( final int index, final Decoder<T> decoder ) {
		DualReference ref = (DualReference)this.constants[index];
		
		if ( ref.firstDecoded == null ) {
			String utf8 = this.utf8(ref.firstIndex);
			ref.firstDecoded = decoder.decode(utf8);
		}
		
		@SuppressWarnings("unchecked")
		T result = (T)ref.firstDecoded;
		return (T)result;
	}

	private final int secondIndex( final int index ) {
		return ( (DualReference)this.constants[index] ).secondIndex;
	}
	
	private final <T> T decodeSecondRef( final int index, final Decoder<T> decoder ) {
		DualReference ref = (DualReference)this.constants[index];
		
		if ( ref.secondDecoded == null ) {
			String utf8 = this.utf8(ref.secondIndex);
			ref.secondDecoded = decoder.decode(utf8);
		}
		
		@SuppressWarnings("unchecked")
		T result = (T)ref.secondDecoded;
		return (T)result;
	}
	
	private static final SingularReference readClassInfo( final JvmInputStream in ) throws IOException {
		int classIndex = in.u2();
		return new ClassReference(classIndex);
	}
	
	private static final DualReference readFieldRef( final JvmInputStream in ) throws IOException {
		int classIndex = in.u2();
		int nameAndTypeIndex = in.u2();
		return new DualReference(classIndex, nameAndTypeIndex);
	}
	
	private static final DualReference readMethodRef( final JvmInputStream in ) throws IOException {
		int classIndex = in.u2();
		int nameAndTypeIndex = in.u2();
		return new DualReference(classIndex, nameAndTypeIndex);
	}

	private static final DualReference readInterfaceMethodRef( final JvmInputStream in ) throws IOException {
		int classIndex = in.u2();
		int nameAndTypeIndex = in.u2();
		
		return new DualReference( classIndex, nameAndTypeIndex );
	}
	
	private static final SingularReference readString( final JvmInputStream in ) throws IOException {
		int utf8Index = in.u2();
		return new StringReference(utf8Index);
	}
	
	private static final Integer readInteger( final JvmInputStream in ) throws IOException {
		return in.u4();
	}
	
	private static final Float readFloat( final JvmInputStream in ) throws IOException {
		return in.u4Float();
	}
	
	private final Long readLong( final JvmInputStream in ) throws IOException {
		return in.u8();
	}
	
	private static final Double readDouble( final JvmInputStream in ) throws IOException {
		return in.u8Double();
	}
	
	private static final DualReference readNameAndType( final JvmInputStream in ) throws IOException {
		int nameIndex = in.u2();
		int descriptorIndex = in.u2();
		
		return new DualReference(nameIndex, descriptorIndex);
	}
	
	private static final String readUtf8( final JvmInputStream in ) throws IOException {
		int length = in.u2();
		
		return in.utf8(length);
	}
	
	private static final String translateClass( final String jvmClassName ) {
		return jvmClassName.replace( '/', '.' );
	}

	/**
	 * Used for any single reference entry - like class entry
	 */
	static abstract class SingularReference {
		final int index;
		Object decoded;
		
		SingularReference(final int index) {
			this.index = index;
		}
	}
	
	static final class StringReference extends SingularReference {
		StringReference(final int index) {
			super(index);
		}
	}
	
	static final class ClassReference extends SingularReference {
		ClassReference(final int index) {
			super(index);
		}
	}
	
	/**
	 * Used for any dual reference entry - like...
	 *  - method ref
	 *  - interface method ref
	 *  - field ref
	 *  - name and type pair
	 */
	static final class DualReference {
		final int firstIndex;
		final int secondIndex;
		
		Object firstDecoded;
		Object secondDecoded;
		
		DualReference(
			final int firstIndex,
			final int secondIndex )
		{
			this.firstIndex = firstIndex;
			this.secondIndex = secondIndex;
		}
	}
	
	static abstract class Decoder<T> {
		abstract T decode( final String value );
	}
	
	static final class StringDecoder extends Decoder<String> {
		static final StringDecoder INSTANCE = new StringDecoder();
		
		private StringDecoder() {}
		
		@Override
		final String decode( final String value ) {
			return value;
		}
	}
	
	static final class TypeDecoder extends Decoder<Type> {
		static final TypeDecoder INSTANCE = new TypeDecoder();
		
		private TypeDecoder() {}
		
		@Override
		final Type decode( final String value ) {
			// Not worried about primitives, so I can user a lighter implementation
			return JavaTypes.objectTypeName( translateClass(value) );
		}
	}
	
	static final class FieldDescriptorDecoder extends Decoder<Type> {
		static final FieldDescriptorDecoder INSTANCE = new FieldDescriptorDecoder();
		
		private FieldDescriptorDecoder() {}
		
		@Override
		final Type decode( final String value ) {
			return JavaTypes.fromPersistedName(value);
		}
	}
	
	static final class MethodSignatureDecoder extends Decoder<MethodTypeDescriptor> {
		static final MethodSignatureDecoder INSTANCE = new MethodSignatureDecoder();
		
		private MethodSignatureDecoder() {}
		
		@Override
		final MethodTypeDescriptor decode( final String signature ) {
			if ( signature.startsWith( "()" ) ) {
				return handleWithoutParameters(signature);
			} else {
				return handleWithParameters(signature);
			}
		}
		
		private static final MethodTypeDescriptor handleWithoutParameters(
			final String signature )
		{
			String returnTypeRemainder = signature.substring(2);
			return new MethodTypeDescriptor(
				Collections.<Type>emptyList(),
				JavaTypes.fromPersistedName(returnTypeRemainder));
		}
		
		private static final MethodTypeDescriptor handleWithParameters(
			final String signature )
		{
			throw new IllegalStateException("not implemented");
		}
	}
	
	private static final class DecodeCacheEntry {
		private final Decoder<?> decoder;
		private final Object value;
		
		public DecodeCacheEntry( final Decoder<?> decoder, final Object value ) {
			this.decoder = decoder;
			this.value = value;
		}
		
		boolean matches( final Decoder<?> decoder ) {
			return this.decoder.equals(decoder);
		}
		
		<T> T get() {
			@SuppressWarnings("unchecked")
			T result = (T)value;
			return result;
		}
	}
}
