package org.dreamwork.integrated.common.device.manager.service.impl;

import org.dreamwork.integrated.common.device.manager.api.services.ISessionManager;
import org.apache.mina.core.session.IoSession;
import org.dreamwork.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Resource
public class IoSessionManagerImpl implements ISessionManager {
    private final Logger logger = LoggerFactory.getLogger (IoSessionManagerImpl.class);
    private final Map<String, IoSession> pool = new ConcurrentHashMap<> ();
    private volatile boolean disposed;

    @Override
    public synchronized void push (String imei, IoSession session) {
        if (!disposed) {
            pool.put (imei, session);
        }
    }

    @Override
    public synchronized IoSession get (String imei) {
        return pool.get (imei);
    }

    @Override
    public boolean exists (String imei) {
        return pool.containsKey (imei);
    }

    @Override
    public void remove (String imei) {
        pool.remove (imei);
    }

    @Override
    public synchronized void remove (IoSession session) {
        String imei = (String) session.getAttribute ("imei");
        if (!StringUtil.isEmpty (imei)) {
            remove (imei);
        } else {
            Map<String, IoSession> temp;
            synchronized (pool) {
                temp = new HashMap<> (pool);
            }
            for (Map.Entry<String, IoSession> e : temp.entrySet ()) {
                if (e.getValue () == session) {
                    remove (e.getKey ());
                    break;
                }
            }
        }
    }

    @Override
    @PreDestroy
    public void dispose () {
        if (logger.isTraceEnabled ()) {
            logger.trace ("disposing io session pool ...");
        }
        disposed = true;
        if (!pool.isEmpty ()) {
            Set<IoSession> set = new HashSet<> (pool.values ());
            set.forEach (session -> {
                session.closeNow ();
                remove (session);
            });
        }
        if (logger.isTraceEnabled ()) {
            logger.trace ("io session pool disposed.");
        }
    }
}