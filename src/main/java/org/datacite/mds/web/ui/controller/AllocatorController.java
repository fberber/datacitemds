package org.datacite.mds.web.ui.controller;

import javax.annotation.PostConstruct;

import org.datacite.mds.domain.Allocator;
import org.datacite.mds.service.MagicAuthStringService;
import org.datacite.mds.web.ui.Converters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.roo.addon.web.mvc.controller.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@RooWebScaffold(path = "allocators", formBackingObject = Allocator.class, delete = false)
@RequestMapping("/allocators")
@Controller
public class AllocatorController {
    @Autowired
    private GenericConversionService myConversionService;
    
    @Autowired
    private MagicAuthStringService magicAuthStringService;

    @PostConstruct
    void registerConverters() {
        myConversionService.addConverter(Converters.getSimpleAllocatorConverter());
        myConversionService.addConverter(Converters.getSimplePrefixConverter());
    }

    @RequestMapping(params = "find=BySymbolEquals", method = RequestMethod.GET)
    public String findAllocatorsBySymbolEquals(@RequestParam("symbol") String symbol, Model model) {
        Allocator allocator = Allocator.findAllocatorBySymbol(symbol);
        return (allocator == null) ? "allocators/show" : "redirect:/allocators/" + allocator.getId();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String show(@PathVariable("id") Long id, Model model) {
        Allocator allocator = Allocator.findAllocator(id);
        model.addAttribute("allocator", allocator);
        model.addAttribute("itemId", id);
        model.addAttribute("magicAuthString", magicAuthStringService.getCurrentAuthString(allocator));
        return "allocators/show";
    }
}
