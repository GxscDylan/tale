package com.tale.controller;


import com.blade.Environment;
import com.blade.ioc.annotation.Inject;
import com.blade.mvc.annotation.GetRoute;
import com.blade.mvc.annotation.JSON;
import com.blade.mvc.annotation.Path;
import com.blade.mvc.annotation.PostRoute;
import com.blade.mvc.http.Request;
import com.blade.mvc.ui.RestResponse;
import com.tale.bootstrap.TaleConst;
import com.tale.model.entity.Users;
import com.tale.model.params.InstallParam;
import com.tale.service.OptionsService;
import com.tale.service.SiteService;
import com.tale.utils.TaleUtils;
import com.tale.validators.CommonValidator;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Path("register")
public class RegisterController extends BaseController {

    @Inject
    private SiteService siteService;

    @Inject
    private OptionsService optionsService;

    /**
     * 注册页
     */
    @GetRoute
    public String register(Request request) {
        return "admin/register";
    }

    @PostRoute
    @JSON
    public RestResponse<?> doRegister(InstallParam installParam) {

        CommonValidator.valid(installParam);
        Users temp = new Users();
        temp.setUsername(installParam.getAdminUser());
        temp.setPassword(installParam.getAdminPwd());
        temp.setEmail(installParam.getAdminEmail());

        siteService.initSite(temp);

        String siteUrl = TaleUtils.buildURL(installParam.getSiteUrl());
        optionsService.saveOption("site_title", installParam.getSiteTitle());
        optionsService.saveOption("site_url", siteUrl);

        TaleConst.OPTIONS = Environment.of(optionsService.getOptions());

        return RestResponse.ok();
    }
}
