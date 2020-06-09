package cn.wildfirechat.app.shiro;


import cn.wildfirechat.app.model.PCSession;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Set;

@Service
public class ScanCodeRealm extends AuthorizingRealm {

    private static final Logger LOG = LoggerFactory.getLogger(ScanCodeRealm.class);

    @Autowired
    AuthDataSource authDataSource;

    @Autowired
    TokenMatcher tokenMatcher;

    @PostConstruct
    private void initMatcher() {
        setCredentialsMatcher(tokenMatcher);
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        Set<String> stringSet = new HashSet<>();
        stringSet.add("user:show");
        stringSet.add("user:admin");
        info.setStringPermissions(stringSet);
        return info;
    }

    @Override
    public boolean supports(AuthenticationToken token) {
        if (token instanceof TokenAuthenticationToken)
            return true;
        return super.supports(token);
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        String token = (String) authenticationToken.getPrincipal();
        PCSession session = authDataSource.getSession(token, false);
        if (session == null) {
            authDataSource.createSession("", token, 0);
            throw new AuthenticationException("会话不存在");

        }
        LOG.debug("session会话=="+session);
        return new SimpleAuthenticationInfo(token, token, getName());

    }
}