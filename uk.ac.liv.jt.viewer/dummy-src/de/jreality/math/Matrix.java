package de.jreality.math;

public class Matrix
{
	double[] values;

	public Matrix()
	{
		this.values = new double[4 * 4];
	}

	public Matrix( double[] values )
	{
		this.values = values;
	}

	public void transpose()
	{
		double[] newValues = new double[4 * 4];

		newValues[0 * 0] = this.values[0 * 0];
		newValues[0 * 1] = this.values[1 * 0];
		newValues[0 * 2] = this.values[2 * 0];
		newValues[0 * 3] = this.values[3 * 0];
		newValues[1 * 0] = this.values[0 * 1];
		newValues[1 * 1] = this.values[1 * 1];
		newValues[1 * 2] = this.values[2 * 1];
		newValues[1 * 3] = this.values[3 * 1];
		newValues[2 * 0] = this.values[0 * 2];
		newValues[2 * 1] = this.values[1 * 2];
		newValues[2 * 2] = this.values[2 * 2];
		newValues[2 * 3] = this.values[3 * 2];
		newValues[3 * 0] = this.values[0 * 3];
		newValues[3 * 1] = this.values[1 * 3];
		newValues[3 * 2] = this.values[2 * 3];
		newValues[3 * 3] = this.values[3 * 3];

		this.values = newValues;
	}

	public double[] getArray()
	{
		return this.values;
	}

}
