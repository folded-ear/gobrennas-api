package com.brennaswitzer.cookbook.domain.envers;

import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import org.hibernate.envers.RevisionListener;
import org.springframework.context.ApplicationContext;

/**
 * Envers uses this to populate the custom {@link BfsRevisionEntity}. Due to
 * partial Spring magic, this type is instantiated by the bean factory, but
 * isn't quite a "real" bean with full wiring support. So just capture the
 * {@link ApplicationContext} during construction, and then obtain the
 * {@link UserPrincipalAccess} from it upon first need. This also happens to
 * avoid a cyclic dependency, as this listener is needed for Hibernate to start,
 * but Hibernate is needed by our implementation.
 */
public class BfsRevisionListener implements RevisionListener {

    private final ApplicationContext appCtx;
    private UserPrincipalAccess principalAccess;

    public BfsRevisionListener(ApplicationContext appCtx) {
        this.appCtx = appCtx;
    }

    private UserPrincipalAccess getPrincipalAccess() {
        if (principalAccess == null) {
            principalAccess = appCtx.getBean(UserPrincipalAccess.class);
        }
        return principalAccess;
    }

    @Override
    public void newRevision(Object revisionEntity) {
        if (revisionEntity instanceof BfsRevisionEntity rev) {
            rev.setUsername(getPrincipalAccess().getUsername());
        }
    }

}
