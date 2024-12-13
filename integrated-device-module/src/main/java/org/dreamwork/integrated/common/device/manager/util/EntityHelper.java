package org.dreamwork.integrated.common.device.manager.util;

import org.dreamwork.integrated.common.device.manager.api.model.database.Device;
import org.dreamwork.integrated.common.device.manager.api.model.database.DeviceTypeConfig;
import org.dreamwork.integrated.common.device.manager.api.model.downlink.CommandCache;
import org.dreamwork.util.StringUtil;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class EntityHelper {
    public static String buildTripleKey (DeviceTypeConfig config) {
        String vendor = config.getVendor (),
                category = config.getCategory (),
                model = config.getModel ();
        return buildTripleKey (vendor, category, model);
    }

    public static  String buildTripleKey (String vendor, String category, String model) {
        if (StringUtil.isEmpty (vendor) || StringUtil.isEmpty (category) || StringUtil.isEmpty (model)) {
            throw new IllegalArgumentException ();
        }

        return escape (vendor) + '-' + escape (category) + '-' + escape (model);
    }

    public static String escape (String text) {
        if (text.contains ("$")) {
            text = text.replace ("$", "$24");
        }
        if (text.contains ("-")) {
            text = text.replace ("-", "$2D");
        }
        if (text.contains (" ")) {
            text = text.replace (" ", "$20");
        }
        return text;
    }

    public static boolean isEmpty (Collection<?> c) {
        return c == null || c.isEmpty ();
    }

    public static boolean isNotEmpty (Collection<?> c) {
        return c != null && !c.isEmpty ();
    }

    public static boolean isEmpty (Map<?, ?> m) {
        return m == null || m.isEmpty ();
    }

    public static boolean isNotEmpty (Map<?, ?> m) {
        return m != null && !m.isEmpty ();
    }

    public static Map<String, String> device2map (Device device) {
        if (device == null) {
            return Collections.emptyMap ();
        }

        Map<String, String> map = new HashMap<> ();
        map.put ("imei", device.getImei ());
        map.put ("module", device.getModule ());
        map.put ("protocol", device.getProtocol ());
        map.put ("vendor", device.getVendor ());
        map.put ("category", device.getCategory ());
        map.put ("model", device.getModel ());
        map.put ("register_time", String.valueOf (device.getRegisterTime ().getTime ()));
        return map;
    }

    public static Device map2device (Map<String, String> map) {
        if (isEmpty (map) || !map.containsKey ("imei")) {
            return null;
        }

        Device device = new Device ();
        device.setImei (map.get ("imei"));
        device.setModule (map.get ("module"));
        device.setProtocol (map.get ("protocol"));
        device.setVendor (map.get ("vendor"));
        device.setCategory (map.get ("category"));
        device.setModel (map.get ("model"));

        String registerTime = map.get ("register_time");
        device.setRegisterTime (new Timestamp (Long.parseLong (registerTime)));
        return device;
    }

    public static CommandCache map2commandCache (Map<String, String> map) {
        if (isEmpty (map) || !map.containsKey ("command")) {
            return null;
        }

        CommandCache cache = new CommandCache ();
        cache.command = map.get ("command");
        cache.logId   = map.get ("logId");
        if (map.containsKey ("timestamp")) {
            cache.cachedTimestamp = Long.parseLong (map.get ("timestamp"));
        }
        return cache;
    }
}
