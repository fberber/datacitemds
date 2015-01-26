package org.datacite.mds.web.ui.controller;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import org.datacite.mds.domain.Prefix;
import org.datacite.mds.util.Utils;
import org.datacite.mds.validation.ValidationHelper;
import org.datacite.mds.web.ui.UiController;
import org.datacite.mds.web.ui.model.PrefixCreateModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

@RequestMapping("/prefixes")
@Controller
public class PrefixController implements UiController {
    
    @Autowired
    ValidationHelper validationHelper;
    
    @RequestMapping(method = RequestMethod.GET)
    public String list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        int sizeNo = size == null ? LIST_DEFAULT_SIZE : Math.min(size.intValue(), LIST_MAX_SIZE);
        uiModel.addAttribute("prefixes", Prefix.findPrefixEntries(page == null ? 0 : (page.intValue() - 1) * sizeNo, sizeNo));
        float nrOfPages = (float) Prefix.countPrefixes() / sizeNo;
        uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        uiModel.addAttribute("size", sizeNo);
        return "prefixes/list";
    }
    
    @RequestMapping(method = RequestMethod.POST)
    public String create(@Valid PrefixCreateModel createModel, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        Collection<String> prefixesStr = Utils.csvToList(Utils.normalizeCsvStandard(createModel.getPrefixes()));
        prefixesStr = new HashSet<String>(prefixesStr);

        ArrayList<Prefix> prefixes = new ArrayList<Prefix>();
        for (String prefixStr : prefixesStr) {
            Prefix prefix = new Prefix();
            prefix.setPrefix(prefixStr);
            prefixes.add(prefix);
        }
        
        try {
            validationHelper.validate(prefixes.toArray());
        } catch (ConstraintViolationException e) {
            for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
                String msg = ((Prefix) violation.getRootBean()).getPrefix() + ": " + violation.getMessage();
                bindingResult.rejectValue("prefixes", null, msg);
            }
        }
        
        if (bindingResult.hasErrors()) {
            uiModel.addAttribute("prefixCreateModel", createModel);
            return "prefixes/create";
        }
        
        for (Prefix prefix : prefixes)
            prefix.persist();
       
        uiModel.asMap().clear();
        return "redirect:/prefixes/";
    }
    
    @RequestMapping(params = "form", method = RequestMethod.GET)
    public String createForm(Model uiModel) {
        uiModel.addAttribute("prefixCreateModel", new PrefixCreateModel());
        return "prefixes/create";
    }

	@RequestMapping(params = { "find=ByPrefixLike", "form" }, method = RequestMethod.GET)
    public String findPrefixesByPrefixLikeForm(Model uiModel) {
        return "prefixes/findPrefixesByPrefixLike";
    }

	@RequestMapping(params = "find=ByPrefixLike", method = RequestMethod.GET)
    public String findPrefixesByPrefixLike(@RequestParam("prefix") String prefix, Model uiModel) {
        uiModel.addAttribute("prefixes", Prefix.findPrefixesByPrefixLike(prefix).getResultList());
        return "prefixes/list";
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String show(@PathVariable("id") Long id, Model uiModel) {
        uiModel.addAttribute("prefix", Prefix.findPrefix(id));
        uiModel.addAttribute("itemId", id);
        return "prefixes/show";
    }

	@RequestMapping(method = RequestMethod.PUT)
    public String update(@Valid Prefix prefix, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            uiModel.addAttribute("prefix", prefix);
            return "prefixes/update";
        }
        uiModel.asMap().clear();
        prefix.merge();
        return "redirect:/prefixes/" + encodeUrlPathSegment(prefix.getId().toString(), httpServletRequest);
    }

	@RequestMapping(value = "/{id}", params = "form", method = RequestMethod.GET)
    public String updateForm(@PathVariable("id") Long id, Model uiModel) {
        uiModel.addAttribute("prefix", Prefix.findPrefix(id));
        return "prefixes/update";
    }

	String encodeUrlPathSegment(String pathSegment, HttpServletRequest httpServletRequest) {
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
