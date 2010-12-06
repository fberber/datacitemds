// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package org.datacite.mds.web;

import java.io.UnsupportedEncodingException;
import java.lang.Long;
import java.lang.String;
import java.util.Collection;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.datacite.mds.domain.Allocator;
import org.datacite.mds.domain.Prefix;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

privileged aspect AllocatorController_Roo_Controller {
    
    @Autowired
    private GenericConversionService AllocatorController.conversionService;
    
    @RequestMapping(method = RequestMethod.POST)
    public String AllocatorController.create(@Valid Allocator allocator, BindingResult result, Model model, HttpServletRequest request) {
        if (result.hasErrors()) {
            model.addAttribute("allocator", allocator);
            return "allocators/create";
        }
        allocator.persist();
        return "redirect:/allocators/" + encodeUrlPathSegment(allocator.getId().toString(), request);
    }
    
    @RequestMapping(params = "form", method = RequestMethod.GET)
    public String AllocatorController.createForm(Model model) {
        model.addAttribute("allocator", new Allocator());
        return "allocators/create";
    }
    
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String AllocatorController.show(@PathVariable("id") Long id, Model model) {
        model.addAttribute("allocator", Allocator.findAllocator(id));
        model.addAttribute("itemId", id);
        return "allocators/show";
    }
    
    @RequestMapping(method = RequestMethod.GET)
    public String AllocatorController.list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model model) {
        if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            model.addAttribute("allocators", Allocator.findAllocatorEntries(page == null ? 0 : (page.intValue() - 1) * sizeNo, sizeNo));
            float nrOfPages = (float) Allocator.countAllocators() / sizeNo;
            model.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            model.addAttribute("allocators", Allocator.findAllAllocators());
        }
        return "allocators/list";
    }
    
    @RequestMapping(method = RequestMethod.PUT)
    public String AllocatorController.update(@Valid Allocator allocator, BindingResult result, Model model, HttpServletRequest request) {
        if (result.hasErrors()) {
            model.addAttribute("allocator", allocator);
            return "allocators/update";
        }
        allocator.merge();
        return "redirect:/allocators/" + encodeUrlPathSegment(allocator.getId().toString(), request);
    }
    
    @RequestMapping(value = "/{id}", params = "form", method = RequestMethod.GET)
    public String AllocatorController.updateForm(@PathVariable("id") Long id, Model model) {
        model.addAttribute("allocator", Allocator.findAllocator(id));
        return "allocators/update";
    }
    
    @RequestMapping(params = { "find=BySymbolEquals", "form" }, method = RequestMethod.GET)
    public String AllocatorController.findAllocatorsBySymbolEqualsForm(Model model) {
        return "allocators/findAllocatorsBySymbolEquals";
    }
    
    @RequestMapping(params = { "find=ByNameLike", "form" }, method = RequestMethod.GET)
    public String AllocatorController.findAllocatorsByNameLikeForm(Model model) {
        return "allocators/findAllocatorsByNameLike";
    }
    
    @RequestMapping(params = "find=ByNameLike", method = RequestMethod.GET)
    public String AllocatorController.findAllocatorsByNameLike(@RequestParam("name") String name, Model model) {
        model.addAttribute("allocators", Allocator.findAllocatorsByNameLike(name).getResultList());
        return "allocators/list";
    }
    
    @ModelAttribute("prefixes")
    public Collection<Prefix> AllocatorController.populatePrefixes() {
        return Prefix.findAllPrefixes();
    }
    
    private String AllocatorController.encodeUrlPathSegment(String pathSegment, HttpServletRequest request) {
        String enc = request.getCharacterEncoding();
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
