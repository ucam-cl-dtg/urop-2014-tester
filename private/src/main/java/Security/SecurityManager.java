package Security;

import configuration.ConfigurationLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validates security tokens against the token in the config file
 * @author as2388
 */
public class SecurityManager {
    private static Logger log = LoggerFactory.getLogger(SecurityManager.class);

    public static void validateSecurityToken(String securityToken)
    {
        if (securityToken == null)
        {
            log.error("No securityToken query parameter given!");
            throw new SecurityException("No securityToken query parameter given!");
        }
        else if (ConfigurationLoader.getConfig().getSecurityToken() == null)
        {
            log.error("No securityToken configuration option set!");
            throw new SecurityException("No securityToken configuration option set!");
        }
        else if (!ConfigurationLoader.getConfig().getSecurityToken().equals(securityToken));
        {
            log.error("The given securityToken is invalid!");
            throw new SecurityException("The given securityToken is invalid!");
        }
    }
}
