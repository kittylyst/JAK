package net.dougqh.jak;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import net.dougqh.jak.Flags;
import net.dougqh.java.meta.types.JavaTypeBuilder;
import net.dougqh.java.meta.types.JavaTypes;

import static net.dougqh.jak.Methods.*;

public class Jak {
	public static final Type void_ = void.class;
	public static final Type boolean_ = boolean.class;
	public static final Type byte_ = byte.class;
	public static final Type char_ = char.class;
	public static final Type short_ = short.class;
	public static final Type int_ = int.class;
	public static final Type long_ = long.class;
	public static final Type float_ = float.class;
	public static final Type double_ = double.class;
	
	public static final JavaPackageDescriptor package_( final String name ) {
		return new JavaPackageDescriptor( name );
	}
	
	public static final JavaFlagsBuilder public_() {
		return new JavaFlagsBuilder( Flags.PUBLIC );
	}
	
	public static final JavaFlagsBuilder protected_() {
		return new JavaFlagsBuilder( Flags.PROTECTED );
	}
	
	public static final JavaFlagsBuilder private_() {
		return new JavaFlagsBuilder( Flags.PRIVATE );
	}
	
	public static final JavaFlagsBuilder abstract_() {
		return new JavaFlagsBuilder( Flags.ABSTRACT );
	}
	
	public static final JavaFlagsBuilder static_() {
		return new JavaFlagsBuilder( Flags.STATIC );
	}
	
	public static final JavaFlagsBuilder final_() {
		return new JavaFlagsBuilder( Flags.FINAL );
	}
	
	public static final JavaFlagsBuilder synchronized_() {
		return new JavaFlagsBuilder( Flags.SYNCHRONIZED );
	}
	
	public static final JavaFlagsBuilder native_() {
		return new JavaFlagsBuilder( Flags.NATIVE );
	}
	
	public static final JavaFlagsBuilder strictfp_() {
		return new JavaFlagsBuilder( Flags.STRICTFP );
	}
	
	public static final JavaFlagsBuilder volatile_() {
		return new JavaFlagsBuilder( Flags.VOLATILE );
	}
	
	public static final JavaFlagsBuilder transient_() {
		return new JavaFlagsBuilder( Flags.TRANSIENT );
	}
	
	public static final JavaFlagsBuilder varargs() {
		return new JavaFlagsBuilder( Flags.VAR_ARGS );
	}
	
	public static final JavaClassDescriptor class_( final String className ) {
		return new JavaFlagsBuilder().class_( className );
	}
	
	public static final JavaClassDescriptor class_(
		final Package aPackage,
		final String className )
	{
		return new JavaFlagsBuilder().class_( aPackage, className );
	}
	
	public static final JavaInterfaceDescriptor interface_(
		final String interfaceName )
	{
		return new JavaFlagsBuilder().interface_( interfaceName );
	}
	
	public static final JavaInterfaceDescriptor interface_(
		final Package aPackage,
		final String interfaceName )
	{
		return new JavaFlagsBuilder().interface_( aPackage, interfaceName );
	}
	
	public static final JavaField field( final Field field ) {
		return new JavaFlagsBuilder( field.getModifiers() ).field(
			field.getGenericType(),
			field.getName() );
	}
	
	public static final JavaField field(
		final Type fieldType,
		final CharSequence fieldName )
	{
		return new JavaFlagsBuilder().field( fieldType, fieldName );
	}
	
	public static final JavaField field(
		final JavaTypeBuilder typeBuilder,
		final CharSequence fieldName )
	{
		return field( typeBuilder.make(), fieldName );
	}
	
	public static final JavaMethodDescriptor method(
		final Method method )
	{
		JavaMethodDescriptor methodDescriptor = new JavaMethodDescriptor(
			new JavaFlagsBuilder( method.getModifiers() ),
			method.getGenericReturnType(),
			method.getName() );
		methodDescriptor.args( method.getGenericParameterTypes() );
		methodDescriptor.throws_( method.getGenericExceptionTypes() );
		
		return methodDescriptor;
	}
	
	public static final JavaMethodDescriptor method(
		final Type returnType,
		final String methodName )
	{
		return new JavaFlagsBuilder().method( returnType, methodName );
	}
	
	public static final JavaMethodDescriptor method(
		final Type returnType,
		final String methodName,
		final Type... args )
	{
		return new JavaFlagsBuilder().method( returnType, methodName, args );
	}
	
	public static final JavaMethodDescriptor method(
		final Type returnType, 
		final String methodName,
		final Type arg1Type, final String arg1Name )
	{
		return new JavaFlagsBuilder().method( returnType, methodName ).
			arg( arg1Type, arg1Name );
	}

	public static final JavaMethodDescriptor method(
		final Type returnType,
		final String methodName,
		final Type arg1Type, final String arg1Name,
		final Type arg2Type, final String arg2Name )
	{
		return new JavaFlagsBuilder().method( returnType, methodName ).
			arg( arg1Type, arg1Name ).
			arg( arg2Type, arg2Name );
	}

	public static final JavaMethodDescriptor method(
		final Type returnType,
		final String methodName,
		final Type arg1Type, final String arg1Name,
		final Type arg2Type, final String arg2Name,
		final Type arg3Type, final String arg3Name )
	{
		return new JavaFlagsBuilder().method( returnType, methodName ).
			arg( arg1Type, arg1Name ).
			arg( arg2Type, arg2Name ).
			arg( arg3Type, arg3Name );
	}	
	
	public static final JavaMethodSignature method(
		final String methodName )
	{
		return new JavaMethodSignature( methodName );
	}
	
	public static final JavaMethodSignature method(
		final String methodName,
		final Type... args )
	{
		return method( methodName ).args( args );
	}
	
	public static final JavaMethodDescriptor init() {
		return new JavaFlagsBuilder().init();
	}
	
	public static final JavaMethodDescriptor init( final Type... args ) {
		return new JavaFlagsBuilder().init( args );
	}
	
	public static final JavaMethodDescriptor clinit() {
		return static_().method( void.class, CLINIT ); 
	}
	
	//Methods from JavaTypes
	public static final JavaTypeBuilder type() {
		return JavaTypes.type();
	}
	
	public static final JavaTypeBuilder type( final TypeMirror typeMirror ) {
		return JavaTypes.type( typeMirror );
	}
	
	public static final Type type( final TypeElement typeElement ) {
		return JavaTypes.type( typeElement );
	}
	
	public static final Type objectTypeName( final String name ) {
		return JavaTypes.objectTypeName( name );
	}
	
	public static final JavaTypeBuilder typeVar( final String name ) {
		return JavaTypes.typeVar( name );
	}
	
	public static final JavaTypeBuilder type( final Class< ? > aClass ) {
		return JavaTypes.type( aClass );
	}
	
	public static final Type array( final Type type ) {
		return JavaTypes.array( type );
	}
}
