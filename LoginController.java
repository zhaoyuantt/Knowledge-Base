package com.farm.wcp.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.farm.authority.FarmAuthorityService;
import com.farm.authority.service.UserServiceInter;
import com.farm.core.auth.exception.LoginUserNoExistException;
import com.farm.core.page.ViewMode;
import com.farm.doc.server.FarmDocManagerInter;
import com.farm.doc.tag.DefaultIndexPageTaget;
import com.farm.parameter.FarmParameterService;
import com.farm.wcp.util.ThemesUtil;
import com.farm.web.WebUtils;
import com.farm.web.online.OnlineUserOpImpl;
import com.farm.web.online.OnlineUserOpInter;

@RequestMapping("/login")
@Controller
public class LoginController extends WebUtils {
	
    private final static Logger log = Logger.getLogger(LoginController.class);
      
    @Value("${SESSION_TIMEOUT}")
  	private String SESSION_TIMEOUT;   //seesion过期时间
	
	@Resource
	UserServiceInter userService;
	@Resource
	private FarmDocManagerInter farmDocManager;

	/**
	 * 登录form----提交的Controller
	 * @param name
	 * @param password
	 * @param request
	 * @param response
	 * @param session
	 * @return
	 */
	@RequestMapping("/submit")
	public ModelAndView loginCommit(String name, String password, HttpServletRequest request, HttpServletResponse response, HttpSession session) {

        response.setHeader("Access-Control-Allow-Origin", "*"); //允许所有域名的脚本访问该资源
		
        //获取门户访问WCP登录接口的URL
       //String protalUrl = request.getHeader("Referer");      
        // System.out.println("========门户访问WCP登录接口的URL======="+protalUrl);
        //截取IP和端口
        //String protalIpAndPort = protalUrl.substring(protalUrl.indexOf("/")+2,protalUrl.lastIndexOf("/"));
        //System.out.println("========门户的IP和端口============"+protalIpAndPort);
        
        String temporaryProtalIpAndPort  = "172.16.200.112:7001";
        
        //生产环境
       /* String dataPortalIpAndPort = farmDocManager.ProtalIpAndPort();
        if( ! dataPortalIpAndPort.equals(protalIpAndPort)){
        	farmDocManager.updateProtalIpAndPort(protalIpAndPort);
        }*/
        
        //本地环境
        String dataPortalIpAndPort = farmDocManager.ProtalIpAndPort();
        if( ! dataPortalIpAndPort.equals(temporaryProtalIpAndPort)){
        	farmDocManager.updateProtalIpAndPort(temporaryProtalIpAndPort);
        }
       
        if( StringUtils.isEmpty(name) && StringUtils.isEmpty(password)){
			name = "sysadmin";
			password = "111111";
		}
        
		try {
			if (FarmAuthorityService.getInstance().isLegality(name, password)) {
				// 登陆成功
				// 注册session
				loginIntoSession(session, getCurrentIp(request), name, request);
				return ViewMode.getInstance().returnRedirectUrl("/frame/index.do");
			} else {
				// 登陆失败
				return ViewMode.getInstance().putAttr("message", "用户密码错误").returnModelAndView("frame/login");
			}
		} catch (LoginUserNoExistException e) {
			log.info("当前用户不存在");
			return ViewMode.getInstance().putAttr("message", "当前用户不存在").returnModelAndView("frame/login");
		}
	}
   
	
	/**
	 * 进入登录页面
	 * 
	 * @param session
	 * @return
	 */
	@RequestMapping("/webPage")
	public ModelAndView login(HttpSession session, HttpServletRequest request) {
		// 将登陆前的页面地址存入session中，为了登录后回到该页面中
		String url = request.getHeader("Referer");
		session.setAttribute(FarmParameterService.getInstance().getParameter("farm.constant.session.key.from.url"),
				url);
		return ViewMode.getInstance().returnModelAndView(ThemesUtil.getThemePath() + "/login");
	}

	/**
	 * 提交登录请求
	 * 
	 * @param session
	 * @return
	 */
	@RequestMapping("/websubmit")
	public ModelAndView webLoginCommit(String name, String password, HttpServletRequest request, HttpSession session) {
		try {
			if (FarmAuthorityService.getInstance().isLegality(name, password)) {
				// 登陆成功
				// 注册session
				loginIntoSession(session, getCurrentIp(request), name, request);
				String goUrl = null;
				if (goUrl == null) {
					// 要去哪里
					goUrl = (String) session.getAttribute(
							FarmParameterService.getInstance().getParameter("farm.constant.session.key.go.url"));
					session.removeAttribute(
							FarmParameterService.getInstance().getParameter("farm.constant.session.key.go.url"));
				}
				if (goUrl == null) {
					// 来自哪里
					goUrl = (String) session.getAttribute(
							FarmParameterService.getInstance().getParameter("farm.constant.session.key.from.url"));
				}
				if (goUrl != null && goUrl.indexOf("login/webPage") > 0) {
					// 如果返回的是登录页面，则去默认首页
					goUrl = null;
				}
				if (goUrl == null) {
					// 默认页面
					goUrl = "/" + DefaultIndexPageTaget.getDefaultIndexPage();
				}
				return ViewMode.getInstance().returnRedirectUrl(goUrl);
			} else {
				// 登陆失败
				return ViewMode.getInstance().putAttr("loginname", name).setError("用户密码错误")
						.returnModelAndView(ThemesUtil.getThemePath() + "/login");
			}
		} catch (LoginUserNoExistException e) {
			log.info("当前用户不存在");
			return ViewMode.getInstance().putAttr("loginname", name).setError("当前用户不存在")
					.returnModelAndView(ThemesUtil.getThemePath() + "/login");
		}
	}

	@RequestMapping("/webout")
	public ModelAndView weblogOut(String name, HttpSession session) {
		clearCurrentUser(session);
		return ViewMode.getInstance().returnRedirectUrl("/" + DefaultIndexPageTaget.getDefaultIndexPage());
	}

	@RequestMapping("/page")
	public ModelAndView login(String name) {
		return ViewMode.getInstance().returnModelAndView("frame/login");
	}

	@RequestMapping("/out")
	public ModelAndView logOut(String name, HttpSession session) {
		clearCurrentUser(session);
		return ViewMode.getInstance().returnRedirectUrl("/login/page.do");
	}

	/**
	 * 将登陆信息写进session
	 * 
	 * @param session
	 * @param ip
	 * @param loginName
	 */
	private void loginIntoSession(HttpSession session, String ip, String loginName,HttpServletRequest request) {
		// 开始写入session用户信息
		setCurrentUser(FarmAuthorityService.getInstance().getUserByLoginName(loginName), session);
		setLoginTime(session);
		// 开始写入session用户权限
		setCurrentUserAction(FarmAuthorityService.getInstance().getUserAuthKeys(getCurrentUser(session).getId()),
				session);
		// 开始写入session用户菜单
		setCurrentUserMenu(FarmAuthorityService.getInstance().getUserMenu(getCurrentUser(session).getId()), session);
		// 写入用户上线信息
		OnlineUserOpInter ouop = null;
		ouop = OnlineUserOpImpl.getInstance(ip, loginName, session);
		ouop.userLoginHandle(FarmAuthorityService.getInstance().getUserByLoginName(loginName));
		// 记录用户登录时间
		FarmAuthorityService.getInstance().loginHandle(getCurrentUser(session).getId());
		
		//2018-07-12 By zhaoyuan 设置seesion超时时间
		request.getSession().setMaxInactiveInterval(Integer.parseInt(SESSION_TIMEOUT));
		
	}

}
