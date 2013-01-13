package basicblocks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.dougqh.jak.jvm.operations.JvmOperation;

public final class BasicBlock implements Iterable<JvmOperation> {
	private final int pos;
	private final List<JvmOperation> operations = new ArrayList<JvmOperation>(4);
	
	private boolean initial = false;
	private boolean terminating = false;
	private Integer exitPos = null;
	private Integer conditionalExitPos = null;
	
	BasicBlock(final int offset) {
		this.pos = offset;
	}
	
	public final int pos() {
		return this.pos;
	}
	
	public final boolean initial() {
		return this.initial;
	}
	
	public final boolean terminating() {
		return this.terminating;
	}
	
	public final Integer exitPos() {
		return this.exitPos;
	}
	
	public final boolean hasConditionalExit() {
		return ( this.conditionalExitPos != null );
	}
	
	public final Integer conditionalExitPos() {
		return this.conditionalExitPos;
	}
	
	final void add(final JvmOperation op) {
		this.operations.add(op);
	}
	
	public final boolean exitsTo(final BasicBlock block) {
		return this.exitsTo(block.pos());
	}
	
	public final boolean exitsTo(final int pos) {
		if ( this.terminating ) {
			return false;
		} else if ( this.exitPos == pos ) {
			return true;
		} else if ( this.hasConditionalExit() && this.conditionalExitPos == pos ) {
			return true;
		} else {
			return false;
		}
	}
	
	public final void addTargets(final Collection<Integer> targets) {
		if ( this.terminating ) return;
		
		targets.add(this.exitPos);
		
		if ( this.conditionalExitPos != null ) {
			targets.add(this.conditionalExitPos);
		}
	}
	
	public final void addForwardTargets(final Collection<Integer> forwardTargets) {
		if ( this.terminating ) return;
		
		if ( this.exitPos > this.pos ) {
			forwardTargets.add(this.exitPos);
		}
		
		if ( this.conditionalExitPos != null && this.conditionalExitPos > this.pos ) {
			forwardTargets.add(this.conditionalExitPos);
		}
	}
	
	public final void addBackwardTargets(final Collection<Integer> backwardTargets) {
		if ( this.terminating ) return;
		
		if ( this.exitPos <= this.pos ) {
			backwardTargets.add(this.exitPos);
		}
		
		if ( this.conditionalExitPos != null && this.conditionalExitPos <= this.pos ) {
			backwardTargets.add(this.conditionalExitPos);
		}
	}
	
	public final void addConditionalTargets(final Collection<Integer> conditionalTargets) {
		if ( this.terminating ) return;
		
		if ( this.conditionalExitPos != null ) {
			conditionalTargets.add(this.conditionalExitPos);
		}
	}
	
	@Override
	public final Iterator<JvmOperation> iterator() {
		return Collections.unmodifiableList(this.operations).iterator();
	}
	
	final void initExit(final int pos) {
		this.exitPos = pos;
	}
	
	final boolean missingExitPos() {
		return ! this.terminating && ( this.exitPos == null );
	}
	
	final void initConditionalExit(final int pos) {
		this.conditionalExitPos = pos;
	}
	
	final void initInitial() {
		this.initial = true;
	}
	
	final void initTerminating() {
		this.terminating = true;
	}
	
	@Override
	public final String toString() {
		StringBuilder builder = new StringBuilder();

		if ( this.initial ) {
			builder.append(" --->\n");
		}
		builder.append("Basic Block ").append(this.pos).append('\n');
		
		for ( JvmOperation op: this.operations ) {
			builder.append(op.pos()).append(' ').append(op).append('\n');
		}
		
		if ( this.terminating ) {
			builder.append(" -> returns" );
		} else if ( this.conditionalExitPos != null ) {
			builder.append(" true  -> ").append(this.conditionalExitPos).append('\n');
			builder.append(" false -> ").append(this.exitPos);
		} else if ( this.exitPos != null ) {
	        builder.append(" ->" ).append(this.exitPos);
		} else {
			builder.append("??");
		}
		
		return builder.toString();
	}
}
