/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /Users/matsuokayuuma/Documents/workspace/GuchaGuchaRunRecorder/src/app/guchagucharr/service/IRunLoggerService.aidl
 */
package app.guchagucharr.service;
public interface IRunLoggerService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements app.guchagucharr.service.IRunLoggerService
{
private static final java.lang.String DESCRIPTOR = "app.guchagucharr.service.IRunLoggerService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an app.guchagucharr.service.IRunLoggerService interface,
 * generating a proxy if needed.
 */
public static app.guchagucharr.service.IRunLoggerService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof app.guchagucharr.service.IRunLoggerService))) {
return ((app.guchagucharr.service.IRunLoggerService)iin);
}
return new app.guchagucharr.service.IRunLoggerService.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_initGPS:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.initGPS();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements app.guchagucharr.service.IRunLoggerService
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
@Override public int initGPS() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_initGPS, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_initGPS = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
}
public int initGPS() throws android.os.RemoteException;
}
