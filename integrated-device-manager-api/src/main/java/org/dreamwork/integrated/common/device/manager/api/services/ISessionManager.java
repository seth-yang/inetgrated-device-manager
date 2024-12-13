package org.dreamwork.integrated.common.device.manager.api.services;

import org.apache.mina.core.session.IoSession;

public interface ISessionManager {
    void push (String imei, IoSession session);
    IoSession get (String imei);
    boolean exists (String imei);
    void remove (String imei);
    void remove (IoSession session);

    void dispose ();
}
