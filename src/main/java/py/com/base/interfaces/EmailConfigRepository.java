package py.com.base.interfaces;

import py.com.base.clases.EmailConfig;

public interface EmailConfigRepository {
    EmailConfig findByConnectionId(String connectionId);
    EmailConfig findFromProperties();
}
