package org.dreamwork.integrated.common.device.manager.util;

public interface Const {
    interface Error {
        int ERR_OK                      = 0;
        int ERR_INVALID_PARAMETER       = 400;
        int ERR_DEVICE_EXISTS           = 419;
        int ERR_INTERNAL_ERROR          = 500;

        int ERR_MISSING_IMEI            = 400010;
        int ERR_MISSING_MODULE          = 400011;
        int ERR_MISSING_VENDOR          = 400012;
        int ERR_UNKNOWN_DEVICE_TYPE     = 400013;

        int ERR_DEVICE_NOT_FOUND        = 400100;
        int ERR_DEVICE_IS_NOT_ACTIVE    = 400101;
        int ERR_COMMAND_TIMEOUT         = 400102;
    }
}
