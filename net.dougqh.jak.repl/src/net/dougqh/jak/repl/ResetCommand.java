package net.dougqh.jak.repl;

import java.io.IOException;

final class ResetCommand extends FixedIdCommand {
	static final String ID = "reset";
	static final ResetCommand INSTANCE = new ResetCommand();
	
	private ResetCommand() {
		super( ID );
	}

	@Override
	final boolean run(
		final JakRepl repl,
		final String command,
		final String[] args )
		throws IOException
	{
		checkNoArguments( args );
		
		repl.resetProgram();
		return true;
	}
}
