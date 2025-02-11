package de.mpg.imeji.presentation.security;

import static de.mpg.imeji.logic.util.StringHelper.isNullOrEmptyTrim;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import de.mpg.imeji.presentation.rewrite.RequestHelper;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import de.mpg.imeji.exceptions.AuthenticationError;
import de.mpg.imeji.exceptions.InactiveAuthenticationError;
import de.mpg.imeji.logic.config.Imeji;
import de.mpg.imeji.logic.model.User;
import de.mpg.imeji.logic.security.authentication.Authentication;
import de.mpg.imeji.logic.security.authentication.factory.AuthenticationFactory;
import de.mpg.imeji.logic.util.StringHelper;
import de.mpg.imeji.logic.util.UrlHelper;
import de.mpg.imeji.presentation.beans.SuperBean;
import de.mpg.imeji.presentation.session.BeanHelper;
import de.mpg.imeji.presentation.session.SessionBean;

/**
 * Bean for login features
 *
 * @author saquet (initial creation)
 * @author $Author$ (last modification)
 * @version $Revision$ $LastChangedDate$
 */
@ManagedBean(name = "LoginBean")
@ViewScoped
public class LoginBean extends SuperBean {
  private static final long serialVersionUID = 3597358452256592426L;
  private String login;
  private String passwd;
  @ManagedProperty(value = "#{SessionBean}")
  private SessionBean sessionBean;
  private String redirect = null;
  private static final Logger LOGGER = LogManager.getLogger(LoginBean.class);
  private String requestUrl;

  /**
   * Constructor
   */
  public LoginBean() {

  }

  @PostConstruct
  public void init() {
    initRequestUrl();
    try {
      final String login = UrlHelper.getParameterValue("login");
      if (!isNullOrEmptyTrim(login)) {
        setLogin(login);
      }
      if (!isNullOrEmptyTrim(UrlHelper.getParameterValue("redirect"))) {
        this.redirect = URLDecoder.decode(UrlHelper.getParameterValue("redirect"), "UTF-8");
      }
      if (getSessionUser() != null) {
        redirect(redirect != null ? redirect : getNavigation().getHomeUrl());
      }
    } catch (final Exception e) {
      LOGGER.error("Error initializing LoginBean", e);
    }
  }

  public void setLogin(String login) {
    this.login = login.trim();
  }

  public String getLogin() {
    return login;
  }

  public void setPasswd(String passwd) {
    this.passwd = passwd.trim();
  }

  public String getPasswd() {
    return passwd;
  }

  public void doLogin() {
    BeanHelper.cleanMessages();
    final String instanceName = Imeji.CONFIG.getInstanceName();
    if (StringHelper.isNullOrEmptyTrim(getLogin())) {
      return;
    }
    final Authentication auth = AuthenticationFactory.factory(getLogin(), getPasswd());
    try {
      final User user = auth.doLogin();
      sessionBean.setUser(user);
      BeanHelper.info(Imeji.RESOURCE_BUNDLE.getMessage("success_log_in", getLocale()));
      redirectAfterLogin();
    } catch (final InactiveAuthenticationError e) {
      BeanHelper.error(Imeji.RESOURCE_BUNDLE.getMessage("error_log_in_inactive", getLocale()));
    } catch (final AuthenticationError e) {
      BeanHelper.error(Imeji.RESOURCE_BUNDLE.getMessage("error_log_in", getLocale()).replace("XXX_INSTANCE_NAME_XXX", instanceName));
    }
  }

  private void redirectAfterLogin() {
    //if no redirect param is set use internal history or home url
    if (isNullOrEmptyTrim(redirect)) {
      // HistoryPage current = getHistory().getCurrentPage();
      if (!requestUrl.equals(getNavigation().getRegistrationUrl()) && !requestUrl.equals(getNavigation().getLoginUrl())) {
        redirect = requestUrl;
      } else {
        redirect = getNavigation().getHomeUrl();
      }
    }

    //if the redirect url is another website, use internal home url for security reasons
    if (!redirect.startsWith(getNavigation().getApplicationUri())) {
      redirect = getNavigation().getHomeUrl();
    }

    try {
      redirect(redirect);
    } catch (IOException e) {
      LOGGER.error("Error redirect after login", e);
    }
  }

  private void initRequestUrl() {
    //LOGGER.info("PrettyContext: " + PrettyContext.getCurrentInstance().getRequestURL().toURL());
    //LOGGER.info("RequestHelper pretty: " + RequestHelper.getCurrentInstance().getPrettyRequestURL().toString());
    //LOGGER.info("RequestHelper original: " + RequestHelper.getCurrentInstance().getOriginalRequestURL().toString());
    //LOGGER.info(
    //    "HttpRequestUri: " + ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).getRequestURI());
    //LOGGER.info("HttpRequestAttribute: " + ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest())
    //    .getAttribute("javax.servlet.forward.request_uri"));
    //LOGGER.info("PrettyContext: " + PrettyContext.getCurrentInstance().getRequestQueryString().toQueryString());
    //LOGGER.info(((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).getQueryString());
    //LOGGER.info("RequestHelper: " + RequestHelper.getCurrentInstance().getRequestQueryString());

    this.requestUrl = getNavigation().getApplicationUri() + RequestHelper.getCurrentInstance().getPrettyRequestURL().toString()
        + RequestHelper.getCurrentInstance().getRequestQueryString();
  }

  public SessionBean getSessionBean() {
    return sessionBean;
  }

  public void setSessionBean(SessionBean sessionBean) {
    this.sessionBean = sessionBean;
  }

  public String getRedirect() {
    return redirect;
  }

  public void setRedirect(String redirect) {
    this.redirect = redirect;
  }

  public String getEncodedRedirect() throws UnsupportedEncodingException {
    String redirect = UrlHelper.getParameterValue("redirect");
    if (redirect == null) {
      return "";
    }
    return URLEncoder.encode(UrlHelper.getParameterValue("redirect"), "UTF-8");
  }

}
