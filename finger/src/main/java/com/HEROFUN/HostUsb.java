package com.HEROFUN;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;

import wee.digital.finger.HeroFun;

public class HostUsb {

    private final int endPointSize = 4096;
    private final byte[] transferBuff = new byte[endPointSize];
    private UsbInterface usbInterface = null;
    private UsbDeviceConnection connection = null;
    private UsbEndpoint endpointIn = null;
    private UsbEndpoint endpointOut = null;

    public HostUsb() {

    }

    /**
     * Methods for fields: HostUsb.connection, HostUsb.endpointIn, HostUsb.endpointOut, HostUsb.usbInterface... in trust
     */
    public int open() {

        UsbDevice usb = HeroFun.getDevice();
        if (usb == null || !HeroFun.hasPermission(usb)) return -1;

        connection = HeroFun.open(usb);
        if (!connection.claimInterface(usb.getInterface(0), true)) return -1;

        if (usb.getInterfaceCount() < 1) return -1;
        usbInterface = usb.getInterface(0);

        if (usbInterface.getEndpointCount() == 0) return -1;

        for (int i = 0; i < usbInterface.getEndpointCount(); i++) {
            if (usbInterface.getEndpoint(i).getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                if (usbInterface.getEndpoint(i).getDirection() == UsbConstants.USB_DIR_IN) {
                    endpointIn = usbInterface.getEndpoint(i);
                } else if (usbInterface.getEndpoint(i).getDirection() == UsbConstants.USB_DIR_OUT) {
                    endpointOut = usbInterface.getEndpoint(i);
                }
            } else if (usbInterface.getEndpoint(i).getType() == UsbConstants.USB_ENDPOINT_XFER_INT) {
            } else {
            }
        }

        if (connection != null) {
            return connection.getFileDescriptor();
        } else {
            return -1;
        }
    }

    public void close() {
        if (connection != null) {
            connection.releaseInterface(usbInterface);
            connection.close();
        }
    }

    public boolean bulkIn(byte[] bytes, int lng, int timeout) {
        try {
            int i, n, r, ret;
            n = lng / endPointSize;
            r = lng % endPointSize;
            for (i = 0; i < n; i++) {
                System.arraycopy(bytes, i * endPointSize, transferBuff, 0, endPointSize);
                ret = connection.bulkTransfer(endpointOut, transferBuff, endPointSize, timeout);
                if (ret != endPointSize)
                    return false;
            }
            if (r > 0) {
                System.arraycopy(bytes, i * endPointSize, transferBuff, 0, r);
                ret = connection.bulkTransfer(endpointOut, transferBuff, r, timeout);
                return ret == r;
            }
        } catch (Exception e) {
        }
        return true;
    }

    public boolean bulkOut(byte[] bytes, int lng, int timeout) {
        try {
            int i, n, r, ret;
            n = lng / endPointSize;
            r = lng % endPointSize;
            for (i = 0; i < n; i++) {
                ret = connection.bulkTransfer(endpointIn, transferBuff, endPointSize, timeout);
                if (ret != endPointSize)
                    return false;
                System.arraycopy(transferBuff, 0, bytes, i * endPointSize, endPointSize);
            }
            if (r > 0) {
                ret = connection.bulkTransfer(endpointIn, transferBuff, r, timeout);
                if (ret != r)
                    return false;
                System.arraycopy(transferBuff, 0, bytes, i * endPointSize, r);
            }
        } catch (Exception e) {
        }
        return true;
    }

}

