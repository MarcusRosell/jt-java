/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.liv.jt.codec;

import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * Wrapper for java.util.zip.Inflater, implementing IInflater
 * Created by InflaterFactory
 * 
 * Default inflater for Java version
 * 
 * 
 * @author przym
 */
public class InternalInflater implements IInflater {

    Inflater inflater;

    @Override
	public void init(boolean nowrap) {
        inflater = new Inflater(nowrap);
    }

    @Override
	public boolean finished() {
        return inflater.finished();
    }

    @Override
	public boolean needsInput() {
        return inflater.needsInput();
    }

    @Override
	public void setInput(byte[] tmp) {
        inflater.setInput(tmp);
    }

    @Override
	public int inflate(byte[] p) throws DataFormatException {
        return inflater.inflate(p);
    }

    @Override
	public void end() {
        inflater.end();
    }
}
