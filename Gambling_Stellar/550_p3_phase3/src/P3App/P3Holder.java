package P3App;

/**
* P3App/P3Holder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from P3.idl
* Sunday, April 21, 2019 12:08:30 AM CDT
*/

public final class P3Holder implements org.omg.CORBA.portable.Streamable
{
  public P3App.P3 value = null;

  public P3Holder ()
  {
  }

  public P3Holder (P3App.P3 initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = P3App.P3Helper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    P3App.P3Helper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return P3App.P3Helper.type ();
  }

}