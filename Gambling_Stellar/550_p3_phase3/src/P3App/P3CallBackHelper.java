package P3App;


/**
* P3App/P3CallBackHelper.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from P3.idl
* Sunday, April 21, 2019 12:08:30 AM CDT
*/

abstract public class P3CallBackHelper
{
  private static String  _id = "IDL:P3App/P3CallBack:1.0";

  public static void insert (org.omg.CORBA.Any a, P3App.P3CallBack that)
  {
    org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
    a.type (type ());
    write (out, that);
    a.read_value (out.create_input_stream (), type ());
  }

  public static P3App.P3CallBack extract (org.omg.CORBA.Any a)
  {
    return read (a.create_input_stream ());
  }

  private static org.omg.CORBA.TypeCode __typeCode = null;
  synchronized public static org.omg.CORBA.TypeCode type ()
  {
    if (__typeCode == null)
    {
      __typeCode = org.omg.CORBA.ORB.init ().create_interface_tc (P3App.P3CallBackHelper.id (), "P3CallBack");
    }
    return __typeCode;
  }

  public static String id ()
  {
    return _id;
  }

  public static P3App.P3CallBack read (org.omg.CORBA.portable.InputStream istream)
  {
    return narrow (istream.read_Object (_P3CallBackStub.class));
  }

  public static void write (org.omg.CORBA.portable.OutputStream ostream, P3App.P3CallBack value)
  {
    ostream.write_Object ((org.omg.CORBA.Object) value);
  }

  public static P3App.P3CallBack narrow (org.omg.CORBA.Object obj)
  {
    if (obj == null)
      return null;
    else if (obj instanceof P3App.P3CallBack)
      return (P3App.P3CallBack)obj;
    else if (!obj._is_a (id ()))
      throw new org.omg.CORBA.BAD_PARAM ();
    else
    {
      org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate ();
      P3App._P3CallBackStub stub = new P3App._P3CallBackStub ();
      stub._set_delegate(delegate);
      return stub;
    }
  }

  public static P3App.P3CallBack unchecked_narrow (org.omg.CORBA.Object obj)
  {
    if (obj == null)
      return null;
    else if (obj instanceof P3App.P3CallBack)
      return (P3App.P3CallBack)obj;
    else
    {
      org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate ();
      P3App._P3CallBackStub stub = new P3App._P3CallBackStub ();
      stub._set_delegate(delegate);
      return stub;
    }
  }

}