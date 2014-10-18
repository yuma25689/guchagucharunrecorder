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
case TRANSACTION_getTimeInMillis:
{
data.enforceInterface(DESCRIPTOR);
long _result = this.getTimeInMillis();
reply.writeNoException();
reply.writeLong(_result);
return true;
}
case TRANSACTION_requestGPS:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.requestGPS();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_getMode:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getMode();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_setMode:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
this.setMode(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_getNoGpsMode:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getNoGpsMode();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_setNoGpsMode:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
this.setNoGpsMode(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_getActivityTypeCode:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getActivityTypeCode();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_setActivityTypeCode:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
this.setActivityTypeCode(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_clearGPS:
{
data.enforceInterface(DESCRIPTOR);
this.clearGPS();
reply.writeNoException();
return true;
}
case TRANSACTION_createLocationManager:
{
data.enforceInterface(DESCRIPTOR);
this.createLocationManager();
reply.writeNoException();
return true;
}
case TRANSACTION_clearLocationManager:
{
data.enforceInterface(DESCRIPTOR);
this.clearLocationManager();
reply.writeNoException();
return true;
}
case TRANSACTION_startLog:
{
data.enforceInterface(DESCRIPTOR);
this.startLog();
reply.writeNoException();
return true;
}
case TRANSACTION_stopLog:
{
data.enforceInterface(DESCRIPTOR);
this.stopLog();
reply.writeNoException();
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
@Override public long getTimeInMillis() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
long _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getTimeInMillis, _data, _reply, 0);
_reply.readException();
_result = _reply.readLong();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int requestGPS() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_requestGPS, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int getMode() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getMode, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void setMode(int mode) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(mode);
mRemote.transact(Stub.TRANSACTION_setMode, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public int getNoGpsMode() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getNoGpsMode, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void setNoGpsMode(int noGpsModeFlg) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(noGpsModeFlg);
mRemote.transact(Stub.TRANSACTION_setNoGpsMode, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public int getActivityTypeCode() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getActivityTypeCode, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void setActivityTypeCode(int activityTypeCode) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(activityTypeCode);
mRemote.transact(Stub.TRANSACTION_setActivityTypeCode, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void clearGPS() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_clearGPS, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void createLocationManager() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_createLocationManager, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void clearLocationManager() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_clearLocationManager, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void startLog() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_startLog, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void stopLog() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_stopLog, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_getTimeInMillis = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_requestGPS = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_getMode = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_setMode = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_getNoGpsMode = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_setNoGpsMode = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_getActivityTypeCode = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
static final int TRANSACTION_setActivityTypeCode = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
static final int TRANSACTION_clearGPS = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
static final int TRANSACTION_createLocationManager = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
static final int TRANSACTION_clearLocationManager = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
static final int TRANSACTION_startLog = (android.os.IBinder.FIRST_CALL_TRANSACTION + 11);
static final int TRANSACTION_stopLog = (android.os.IBinder.FIRST_CALL_TRANSACTION + 12);
}
public long getTimeInMillis() throws android.os.RemoteException;
public int requestGPS() throws android.os.RemoteException;
public int getMode() throws android.os.RemoteException;
public void setMode(int mode) throws android.os.RemoteException;
public int getNoGpsMode() throws android.os.RemoteException;
public void setNoGpsMode(int noGpsModeFlg) throws android.os.RemoteException;
public int getActivityTypeCode() throws android.os.RemoteException;
public void setActivityTypeCode(int activityTypeCode) throws android.os.RemoteException;
public void clearGPS() throws android.os.RemoteException;
public void createLocationManager() throws android.os.RemoteException;
public void clearLocationManager() throws android.os.RemoteException;
public void startLog() throws android.os.RemoteException;
public void stopLog() throws android.os.RemoteException;
}
