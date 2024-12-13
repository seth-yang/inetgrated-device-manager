package org.dreamwork.integrated.common.device.manager.service.impl;

import org.dreamwork.integrated.common.device.manager.api.model.database.DeviceTypeConfig;
import org.dreamwork.integration.api.IModuleContext;
import org.dreamwork.integration.api.services.IDatabaseService;
import org.dreamwork.db.PostgreSQL;

import java.util.*;
import java.util.stream.Collectors;

import static org.dreamwork.integrated.common.device.manager.util.EntityHelper.isEmpty;

public class WebUIService {
    private final PostgreSQL postgres;

    public WebUIService (IModuleContext context) {
        {
            IDatabaseService service = context.findService (IDatabaseService.class);
            postgres = new PostgreSQL (service.getDataSource ("jdbc/integrated_projects"));
        }
    }

    public List<String> getModules () {
        String sql = "select * from device_type_config order by module_name";
        List<DeviceTypeConfig> list = postgres.list (DeviceTypeConfig.class, sql);
        if (isEmpty (list)) {
            return Collections.emptyList ();
        }

        return list.stream().map (DeviceTypeConfig::getModule).distinct ().collect(Collectors.toList());
    }

    @SuppressWarnings ("unchecked")
    public Map<String, Object> getBasicData () {
        String sql = "select * from device_type_config order by module_name";
        List<DeviceTypeConfig> list = postgres.list (DeviceTypeConfig.class, sql);
        Map<String, Object> ret = new HashMap<> ();

        for (DeviceTypeConfig dtc : list) {
            String module = dtc.getModule ();
            Map<String, Object> vendor =
                    (Map<String, Object>) ret.computeIfAbsent (module, key -> new HashMap<> ());
            String categoryName = dtc.getCategory ();
            List<String> category =
                    (List<String>) vendor.computeIfAbsent (categoryName, key -> new ArrayList<> ());
            String model = dtc.getModel ();
            if (!category.contains (model)) {
                category.add (module);
            }
        }

        return ret;
    }
}
