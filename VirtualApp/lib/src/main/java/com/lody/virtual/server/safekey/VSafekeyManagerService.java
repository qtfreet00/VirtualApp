package com.lody.virtual.server.safekey;

import android.content.Context;
import android.os.RemoteException;
import android.util.Pair;

import com.lody.virtual.helper.utils.VLog;
import com.lody.virtual.server.interfaces.IVSafekeyManager;
import com.lody.virtual.server.interfaces.IVSCallback;
import com.xdja.SafeKey.JNIAPI;
import com.xdja.multichip.jniapi.JarJniApiProxy;
import com.xdja.multichip.jniapi.JarMultiJniApiManager;
import com.xdja.multichip.param.JniApiParam;

import java.util.List;

/**
 * Created by wxudong on 17-12-16.
 */

public class VSafekeyManagerService extends IVSafekeyManager.Stub {

    private static final String TAG = VSafekeyManagerService.class.getSimpleName();
    private static VSafekeyManagerService sInstance;
    private static Context mContext = null;
    private static JarMultiJniApiManager jniApiManager = null;
    private static Pair<Integer, List<JniApiParam>> all = null;
    private static JarJniApiProxy jniProxy = null;
    private static String cardId = null;
    private static Pair<Integer, JarJniApiProxy> jniProxyPair = null;

    private static boolean cardFlag = false;

    private IVSCallback mvsCallback = null;

    public static void systemReady(Context context) {
        mContext = context;
        sInstance = new VSafekeyManagerService();
        initSafekeyLib();
    }

    public static VSafekeyManagerService get() {
        return sInstance;
    }

    public static boolean initSafekeyLib(){
        try {
            VLog.e(TAG, "VS initSafekeyLib");
            jniApiManager = JarMultiJniApiManager.getInstance();
            if(jniApiManager == null) {
                VLog.e(TAG, "jniApiManager is null ");
                return false;
            }

            //如果芯片管家为未启动，此处崩溃。
            all = jniApiManager.getAll(mContext);
            if(all == null) {
                VLog.e(TAG, "List<JniApiParam> is null ");
                return false;
            }
            cardId = getCardIdStatic();
            if(cardId == null) {
                VLog.e(TAG, "cardId is null ");
                return false;
            }
            jniProxy = getJniProxy(mContext, cardId);
            if(jniProxy == null) {
                VLog.e(TAG, "jniProxy is null ");
                return false;
            }
            return true;

        }catch (Exception e){
            VLog.e(TAG, "checkCard exception ");
            e.printStackTrace();
            return false;
        }
    }



    private static String getCardIdStatic(){
        if(all != null){
            if (all.first != 0){
                VLog.e(TAG,"Get card id failed ");
                return null;
            }
            String cardIdStr = null;
            for (JniApiParam jap : all.second) {

                VLog.d(TAG, "CardId : " + jap.cardId + "CardType : " + jap.chipType);

                if (jap.chipType == JniApiParam.TYPE_ONBOARD) {
                    cardIdStr = jap.cardId;
                } else if (jap.chipType == JniApiParam.TYPE_TF) {
                    cardIdStr = jap.cardId;
                } else if (jap.chipType == JniApiParam.TYPE_BLUETOOTH) {
                    cardIdStr = jap.cardId;
                } else if (jap.chipType == JniApiParam.TYPE_COVERED) {
                    cardIdStr = jap.cardId;
                }
                if(cardIdStr != null){
                    cardFlag = true;
                    return cardIdStr;
                }

            }
        } else{
            VLog.e(TAG,"List<JniApiParam> is null, Get card id failed ! ");
        }
        return null;
    }

    @Override
    public String getCardId() throws RemoteException {
        if(all != null){
            if (all.first != 0){
                VLog.e(TAG,"Get card id failed ");
                return null;
            }
            String cardIdStr = null;
            for (JniApiParam jap : all.second) {

                VLog.d(TAG, "CardId : " + jap.cardId + "CardType : " + jap.chipType);

                if (jap.chipType == JniApiParam.TYPE_ONBOARD) {
                    cardIdStr = jap.cardId;
                } else if (jap.chipType == JniApiParam.TYPE_TF) {
                    cardIdStr = jap.cardId;
                } else if (jap.chipType == JniApiParam.TYPE_BLUETOOTH) {
                    cardIdStr = jap.cardId;
                } else if (jap.chipType == JniApiParam.TYPE_COVERED) {
                    cardIdStr = jap.cardId;
                }
                if(cardIdStr != null){
                    cardFlag = true;
                    return cardIdStr;
                }

            }
        } else{
            VLog.e(TAG,"List<JniApiParam> is null, Get card id failed ! ");
        }
        return null;
    }

    private static JarJniApiProxy getJniProxy(Context context, String cardId){
        try {
            VLog.e(TAG, "VS getJniProxy");
            jniProxyPair = jniApiManager.make(context, cardId);
            if (jniProxyPair.first == 0) {
                return jniProxyPair.second;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }



    @Override
    public boolean checkCardState(){
        try {
            VLog.e(TAG, "VS checkCardState");
            if (mContext == null || jniApiManager == null) {
                VLog.e(TAG, "mContext or jniApiManager is null ");
                return false;
            }
            all = jniApiManager.getAll(mContext);
            if (all == null) {
                VLog.e(TAG, "List<JniApiParam> is null ");
                return false;
            }
            cardId = getCardId();
            if (cardId == null) {
                VLog.e(TAG, "cardId is null ");
                return false;
            }
            jniProxy = getJniProxy(mContext, cardId);
            if(jniProxy == null){
                VLog.e(TAG, "jniProxy is null ");
                return false;
            }
            return true;
        }catch (Exception e){
            VLog.e(TAG, "checkCardState exception ");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public int getPinTryCount() throws RemoteException {
        try {
            VLog.e(TAG, "VS getPinTryCount");
            int ret = -1;
            int pinRole = 0x11;
            if (jniProxy != null) {
                int num = jniProxy.GetPinTryCount(pinRole);
                VLog.e(TAG, "getPinTryCount : " + num);
                return num;
            }
            return ret;
        }catch (Exception e){
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public int encryptKey(byte[] key, int keylen, byte[] seckey, int seckeylen) throws RemoteException {
        try {
            VLog.e(TAG, "VS encryptKey");
            byte encrypt_kID = 0x08;
            int ret = -1;
            if(jniProxy != null) {

                ret = jniProxy.SM1(key, keylen, JNIAPI.ECB_ENCRYPT, seckey, encrypt_kID, null);

                if(ret < 0){
                    visitSafeKeyErrorCallback();
                }
            }
            VLog.e(TAG, "VS encryptKey ret "+ ret);
            return ret;
        }catch (Exception e){
            visitSafeKeyErrorCallback();
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public int decryptKey(byte[] seckey, int seckeylen, byte[] key, int keylen) throws RemoteException {
        try {
            VLog.e(TAG, "VS decryptKey");
            byte decrypt_kID = 0x09;
            int ret = -1;
            if(jniProxy != null) {

                ret = jniProxy.SM1(seckey, seckeylen, JNIAPI.ECB_DECRYPT, key, decrypt_kID, null);

                if(ret < 0){
                    visitSafeKeyErrorCallback();
                }
            }
            VLog.e(TAG, "VS decryptKey ret "+ ret);
            return ret;
        }catch (Exception e){
            visitSafeKeyErrorCallback();
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public int getRandom(int len, byte[] random) throws RemoteException {
        try {
            VLog.e(TAG, "VS getRandom");
            int ret = -1;
            if(jniProxy != null) {

                ret = jniProxy.GenRandom(len, random);

                if(ret < 0){
                    visitSafeKeyErrorCallback();
                }
            }
            VLog.e(TAG, "VS getRandom ret "+ ret);
            return ret;
        }catch (Exception e){
            visitSafeKeyErrorCallback();
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public void registerCallback(IVSCallback vsCallback) throws RemoteException {
        VLog.e(TAG, "VS registerCallback ");
        if(vsCallback != null){
            mvsCallback = vsCallback;
        }else {
            VLog.e(TAG, "VS vsCallback is null, registerCallback failed");
        }
    }

    @Override
    public void unregisterCallback() throws RemoteException {
        VLog.e(TAG, "VS unregisterCallback ");
        mvsCallback = null;
    }

    private void visitSafeKeyErrorCallback(){
        try {
            if (mvsCallback != null) {
                mvsCallback.visitSafeKeyError();
                VLog.e(TAG, "VS getRandom visitSafeKeyError ");
            } else {
                VLog.e(TAG, "VS mvsCallback is null ");
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
