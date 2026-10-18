package uk.ac.liv.jt.codec;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.DataFormatException;

import org.tukaani.xz.XZInputStream;

/**
 * Inflater for LZMA compressed data (compression algorithm 3), used by JT 10 and later.
 * The compressed data is an XZ stream, decoded with the org.tukaani.xz library.
 * All the compressed data has to be given in a single call to {@link #setInput(byte[])}.
 */
public class LzmaInflater implements IInflater
{
	private InputStream in;

	private boolean finished;

	private String error;

	@Override
	public void init( boolean nowrap )
	{
		this.in = null;
		this.finished = false;
		this.error = null;
	}

	@Override
	public boolean finished()
	{
		return this.finished;
	}

	@Override
	public boolean needsInput()
	{
		return this.in == null && this.error == null;
	}

	@Override
	public void setInput( byte[] tmp )
	{
		try {
			this.in = new XZInputStream( new ByteArrayInputStream( tmp ) );
		}
		catch ( IOException e ) {
			this.error = "Invalid LZMA data: " + e.getMessage(); //$NON-NLS-1$
		}
		catch ( LinkageError e ) {
			this.error = "LZMA compressed JT data requires the org.tukaani.xz library: " + e; //$NON-NLS-1$
		}
	}

	@Override
	public int inflate( byte[] p ) throws DataFormatException
	{
		if ( this.error != null ) {
			throw new DataFormatException( this.error );
		}
		try {
			int n = this.in.read( p );
			if ( n < 0 ) {
				this.finished = true;
				return 0;
			}
			return n;
		}
		catch ( IOException e ) {
			throw new DataFormatException( "Invalid LZMA data: " + e.getMessage() ); //$NON-NLS-1$
		}
	}

	@Override
	public void end()
	{
		if ( this.in != null ) {
			try {
				this.in.close();
			}
			catch ( IOException e ) {
				// nothing to release for in-memory streams
			}
			this.in = null;
		}
	}
}
