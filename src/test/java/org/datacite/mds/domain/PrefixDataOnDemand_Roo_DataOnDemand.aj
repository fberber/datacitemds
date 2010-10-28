// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package org.datacite.mds.domain;

import java.util.List;
import java.util.Random;
import org.datacite.mds.domain.Prefix;
import org.springframework.stereotype.Component;

privileged aspect PrefixDataOnDemand_Roo_DataOnDemand {
    
    declare @type: PrefixDataOnDemand: @Component;
    
    private Random PrefixDataOnDemand.rnd = new java.security.SecureRandom();
    
    private List<Prefix> PrefixDataOnDemand.data;
    
    public Prefix PrefixDataOnDemand.getNewTransientPrefix(int index) {
        org.datacite.mds.domain.Prefix obj = new org.datacite.mds.domain.Prefix();
        obj.setPrefix("prefix_" + index);
        return obj;
    }
    
    public Prefix PrefixDataOnDemand.getSpecificPrefix(int index) {
        init();
        if (index < 0) index = 0;
        if (index > (data.size() - 1)) index = data.size() - 1;
        Prefix obj = data.get(index);
        return Prefix.findPrefix(obj.getId());
    }
    
    public Prefix PrefixDataOnDemand.getRandomPrefix() {
        init();
        Prefix obj = data.get(rnd.nextInt(data.size()));
        return Prefix.findPrefix(obj.getId());
    }
    
    public boolean PrefixDataOnDemand.modifyPrefix(Prefix obj) {
        return false;
    }
    
    public void PrefixDataOnDemand.init() {
        data = org.datacite.mds.domain.Prefix.findPrefixEntries(0, 10);
        if (data == null) throw new IllegalStateException("Find entries implementation for 'Prefix' illegally returned null");
        if (!data.isEmpty()) {
            return;
        }
        
        data = new java.util.ArrayList<org.datacite.mds.domain.Prefix>();
        for (int i = 0; i < 10; i++) {
            org.datacite.mds.domain.Prefix obj = getNewTransientPrefix(i);
            obj.persist();
            obj.flush();
            data.add(obj);
        }
    }
    
}
