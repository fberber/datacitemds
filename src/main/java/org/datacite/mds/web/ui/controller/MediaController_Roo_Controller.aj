// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package org.datacite.mds.web.ui.controller;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.datacite.mds.domain.Dataset;
import org.datacite.mds.domain.Media;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

privileged aspect MediaController_Roo_Controller {
    
    @RequestMapping(params = "form", method = RequestMethod.GET)
    public String MediaController.createForm(Model uiModel) {
        uiModel.addAttribute("media", new Media());
        List dependencies = new ArrayList();
        if (Dataset.countDatasets() == 0) {
            dependencies.add(new String[]{"dataset", "datasets"});
        }
        uiModel.addAttribute("dependencies", dependencies);
        return "medias/create";
    }
    
    String MediaController.encodeUrlPathSegment(String pathSegment, HttpServletRequest httpServletRequest) {
        String enc = httpServletRequest.getCharacterEncoding();
        if (enc == null) {
            enc = WebUtils.DEFAULT_CHARACTER_ENCODING;
        }
        try {
            pathSegment = UriUtils.encodePathSegment(pathSegment, enc);
        }
        catch (UnsupportedEncodingException uee) {}
        return pathSegment;
    }
    
}
