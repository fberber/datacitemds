// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package org.datacite.mds.domain;

import java.util.List;
import java.util.Random;
import org.datacite.mds.domain.OaiSources;
import org.springframework.stereotype.Component;

privileged aspect OaiSourcesDataOnDemand_Roo_DataOnDemand {
    
    declare @type: OaiSourcesDataOnDemand: @Component;
    
    private Random OaiSourcesDataOnDemand.rnd = new java.security.SecureRandom();
    
    private List<OaiSources> OaiSourcesDataOnDemand.data;
    
    public OaiSources OaiSourcesDataOnDemand.getNewTransientOaiSources(int index) {
        org.datacite.mds.domain.OaiSources obj = new org.datacite.mds.domain.OaiSources();
        obj.setLastHarvest(new java.util.Date());
        obj.setLastStatus("lastStatus_" + index);
        obj.setOwner("owner_" + index);
        obj.setUrl("url_" + index);
        return obj;
    }
    
    public OaiSources OaiSourcesDataOnDemand.getSpecificOaiSources(int index) {
        init();
        if (index < 0) index = 0;
        if (index > (data.size() - 1)) index = data.size() - 1;
        OaiSources obj = data.get(index);
        return OaiSources.findOaiSources(obj.getId());
    }
    
    public OaiSources OaiSourcesDataOnDemand.getRandomOaiSources() {
        init();
        OaiSources obj = data.get(rnd.nextInt(data.size()));
        return OaiSources.findOaiSources(obj.getId());
    }
    
    public boolean OaiSourcesDataOnDemand.modifyOaiSources(OaiSources obj) {
        return false;
    }
    
    public void OaiSourcesDataOnDemand.init() {
        data = org.datacite.mds.domain.OaiSources.findOaiSourcesEntries(0, 10);
        if (data == null) throw new IllegalStateException("Find entries implementation for 'OaiSources' illegally returned null");
        if (!data.isEmpty()) {
            return;
        }
        
        data = new java.util.ArrayList<org.datacite.mds.domain.OaiSources>();
        for (int i = 0; i < 10; i++) {
            org.datacite.mds.domain.OaiSources obj = getNewTransientOaiSources(i);
            obj.persist();
            obj.flush();
            data.add(obj);
        }
    }
    
}
